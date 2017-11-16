/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2000-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

/*
 * @(#)FlowControl.cpp	1.4 06/26/07
 */ 

#include "FlowControl.hpp"
#include "Connection.hpp"
#include "../util/LogUtils.hpp"

/*
 *
 */
FlowControl::FlowControl(Connection * const connectionArg)
{
  CHECK_OBJECT_VALIDITY();

  ASSERT( connectionArg != NULL );
  this->connection = connectionArg;
  this->unDeliveredMsgCount = 0;
  this->resumeRequested = PR_FALSE;
}

/*
 *
 */
FlowControl::~FlowControl()
{
  CHECK_OBJECT_VALIDITY();

  this->connection = NULL;
  this->unDeliveredMsgCount = 0;
  this->resumeRequested = PR_FALSE;
}

/*
 *
 */
void
FlowControl::messageReceived()
{
  CHECK_OBJECT_VALIDITY();

  monitor.enter();
    this->unDeliveredMsgCount++;

    LOG_FINEST(( CODELOC, FLOW_CONTROL_LOG_MASK, connection->id(), IMQ_SUCCESS,
                 "FlowControl::messageReceived().  msgs/watermark = %d/%d",
                 this->unDeliveredMsgCount, 
                 connection->getFlowControlWaterMark() ));
    
  monitor.exit();
}


/*
 *
 */
void
FlowControl::messageDelivered()
{
  CHECK_OBJECT_VALIDITY();

  monitor.enter();
    this->unDeliveredMsgCount--;

    LOG_FINEST(( CODELOC, FLOW_CONTROL_LOG_MASK, connection->id(), IMQ_SUCCESS,
                 "FlowControl::messageDelivered().  msgs/watermark = %d/%d.  "
                 "Trying to resume the flow.",
                 this->unDeliveredMsgCount, 
                 connection->getFlowControlWaterMark() ));

    this->tryResume();
  monitor.exit();
}


/*
 *
 */
void
FlowControl::requestResume()
{
  CHECK_OBJECT_VALIDITY();

  monitor.enter();
    this->resumeRequested = PR_TRUE;

    LOG_FINEST(( CODELOC, FLOW_CONTROL_LOG_MASK, connection->id(), IMQ_SUCCESS,
                 "FlowControl::requestResume().  msgs/watermark = %d/%d.  "
                 "Trying to resume the flow.",
                 this->unDeliveredMsgCount,
                 connection->getFlowControlWaterMark() ));

    this->tryResume();
  monitor.exit();
}


// This method does not have to be syncrhonized because it is only
// called from synchronized methods.
void
FlowControl::tryResume()
{
  CHECK_OBJECT_VALIDITY();

  iMQError errorCode = IMQ_SUCCESS;

  if (!shouldResume()) {
    return;
  }

  errorCode = connection->resumeFlow();
  if (errorCode == IMQ_SUCCESS) {
    resumeRequested = PR_FALSE;
  }
}


// This method does not have to be syncrhonized because it is only
// called from synchronized methods.
PRBool
FlowControl::shouldResume()
{
  CHECK_OBJECT_VALIDITY();

  PRBool doResume = PR_FALSE;
  
  // If the broker hasn't stopped sending messages, then we shouldn't
  // request it to resume sending messages.
  if (!this->resumeRequested) {
    doResume = PR_FALSE;
  }
  
  // If the connection is not flow control limited, then always
    // resume the flow.
  else if (!connection->getFlowControlIsLimited()) {
    doResume = PR_TRUE;
  }
  
  // Otherwise, resume if the number of undelivered messages is below the
  // watermark
  else {
    doResume = 
      this->unDeliveredMsgCount < connection->getFlowControlWaterMark();
  }
  
  return doResume;
}


