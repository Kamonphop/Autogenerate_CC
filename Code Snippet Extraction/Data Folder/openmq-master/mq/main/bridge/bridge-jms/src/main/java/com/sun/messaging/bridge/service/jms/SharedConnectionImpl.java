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

package com.sun.messaging.bridge.service.jms;

import javax.jms.*;

/**
 *
 * @author amyk
 */
public class SharedConnectionImpl extends SharedConnection 
                                   implements Connection {
    
    public SharedConnectionImpl(Connection conn) {
        super(conn);
    }

    public Session
    createSession(boolean transacted,
                  int acknowledgeMode) throws JMSException {
    return _conn.createSession(transacted, acknowledgeMode);
    }
    
	@Override
	public Session createSession(int sessionMode) throws JMSException {
		return _conn.createSession(sessionMode);
	}

	@Override
	public Session createSession() throws JMSException {
		return _conn.createSession();
	}

    public String
    getClientID() throws JMSException {
    return _conn.getClientID();
    }

    public void
    setClientID(String clientID) throws JMSException {
    _conn.setClientID(clientID);
    }

    public ConnectionMetaData
    getMetaData() throws JMSException {
    return _conn.getMetaData();
    }

    public ExceptionListener 
    getExceptionListener() throws JMSException {
    return _conn.getExceptionListener();
    }

    public void 
    setExceptionListener(ExceptionListener listener) throws JMSException {
    _conn.setExceptionListener(listener);
    }

    public void
    start() throws JMSException {
    _conn.start();
    }

    public void
    stop() throws JMSException {
    _conn.stop();
    }

    public void 
    close() throws JMSException {
    _conn.close();
    }
    
	@Override
    public ConnectionConsumer
    createConnectionConsumer(Destination destination,
                             String messageSelector,
                             ServerSessionPool sessionPool,
			     int maxMessages)
			     throws JMSException {
    return _conn.createConnectionConsumer(destination, messageSelector,
                                          sessionPool, maxMessages);
    }

	@Override
	public ConnectionConsumer
    createDurableConnectionConsumer(Topic topic,
                                    String subscriptionName,
                                    String messageSelector,
                                    ServerSessionPool sessionPool,
                                    int maxMessages)
                                    throws JMSException {
    return _conn.createDurableConnectionConsumer(topic, subscriptionName, 
                                messageSelector, sessionPool, maxMessages);
    }
    
	@Override
	public ConnectionConsumer createSharedConnectionConsumer(Topic topic, String subscriptionName,
			String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
		return _conn.createSharedConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
	}

	@Override
	public ConnectionConsumer createSharedDurableConnectionConsumer(Topic topic, String subscriptionName,
			String messageSelector, ServerSessionPool sessionPool, int maxMessages) throws JMSException {
		return _conn.createSharedDurableConnectionConsumer(topic, subscriptionName, messageSelector, sessionPool, maxMessages);
	}
}
