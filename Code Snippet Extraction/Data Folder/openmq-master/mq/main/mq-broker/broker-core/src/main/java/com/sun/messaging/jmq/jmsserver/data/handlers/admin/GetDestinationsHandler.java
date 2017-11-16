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
 * @(#)GetDestinationsHandler.java	1.39 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import java.util.Hashtable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedHashMap;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.jmsserver.data.PacketRouter;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.util.SizeString;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.admin.ConnectionInfo;
import com.sun.messaging.jmq.util.admin.DestinationInfo;
import com.sun.messaging.jmq.util.admin.ConsumerInfo;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;
import com.sun.messaging.jmq.jmsserver.persist.api.PartitionedStore;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.Consumer;
import com.sun.messaging.jmq.jmsserver.core.Producer;
import com.sun.messaging.jmq.jmsserver.core.DestinationUID;

public class GetDestinationsHandler extends AdminCmdHandler
{
    private static boolean DEBUG = getDEBUG();

    public GetDestinationsHandler(AdminDataHandler parent) {
    super(parent);
    }

    /**
     * Handle the incomming administration message.
     *
     * @param con    The Connection the message came in on.
     * @param cmd_msg    The administration message
     * @param cmd_props The properties from the administration message
     */
    public boolean handle(IMQConnection con, Packet cmd_msg,
                       Hashtable cmd_props) {

        if (DEBUG) {
            logger.log(Logger.INFO, "GetDestiantionsHandler: " + cmd_props);
        }

        Vector v = new Vector();
        int status = Status.OK;
        String errMsg = null;

        String destination = (String)cmd_props.get(MessageType.JMQ_DESTINATION);
        Integer destType = (Integer)cmd_props.get(MessageType.JMQ_DEST_TYPE);
        Boolean val = (Boolean)cmd_props.get(MessageType.JMQ_SHOW_PARTITION);
        boolean showpartition = (val == null ? false:val.booleanValue());
        val = (Boolean)cmd_props.get(MessageType.JMQ_LOAD_DESTINATION);
        boolean load = (val == null ? false:val.booleanValue());

        assert destination == null || destType != null;

        if (destination != null) {
            try {
                Destination[] ds = DL.getDestination(null, destination,
                          DestType.isQueue(destType.intValue()));
                Destination d = null;
                DestinationInfo dinfo = null;
                for (int i = 0; i < ds.length; i++) {
                    d = ds[i];
                    if (d != null) {
                        if (DEBUG) {
                            d.debug();
                        }
                        if (load) {
                            d.load();
                        }
                        dinfo = getDestinationInfo(d, dinfo, showpartition);
                        if (showpartition) {
                            v.add(dinfo);
                        }
                    } 
                }
                if (dinfo == null) {
                    throw new BrokerException(
                        rb.getString(rb.X_DESTINATION_NOT_FOUND,
                            destination), Status.NOT_FOUND);
                }
                if (!showpartition) {
                    v.add(dinfo);
                }
            } catch (Exception ex) {
                status = Status.ERROR;
                errMsg= ex.getMessage();
                if (ex instanceof BrokerException) {
                    status = ((BrokerException)ex).getStatusCode();
                }
                logger.logStack(Logger.ERROR, errMsg, ex);
            }
        } else {
                // Get info on ALL destinations
    
            try {

            LinkedHashMap<DestinationUID, DestinationInfo> map = 
                          new LinkedHashMap<DestinationUID, DestinationInfo>();
            Iterator[] itrs = DL.getAllDestinations(null);
            int cnt = itrs.length;
            DestinationInfo dinfo = null;
            DestinationUID duid = null;
            Destination d = null;
            for (int i = 0; i < cnt; i++) {
                 Iterator itr = itrs[i];
                 while (itr.hasNext()) {
                     d = (Destination)itr.next();
                     if (load) {
                         d.load();
                     }
                     duid = d.getDestinationUID();
                     dinfo = map.get(d.getDestinationUID());
                     dinfo = getDestinationInfo(d, dinfo, showpartition);
                     map.put(duid, dinfo);
                     if (showpartition) {
                         v.add(dinfo);
                     } 
                }  
            }
            if (!showpartition) {
                Iterator<DestinationInfo> itr = map.values().iterator();
                while (itr.hasNext()) {
                    v.add(itr.next());
                }
            }

            } catch (Exception ex) {
            status = Status.ERROR;
            errMsg= ex.getMessage();
            if (ex instanceof BrokerException) {
                status = ((BrokerException)ex).getStatusCode();
            }
            logger.logStack(Logger.ERROR, errMsg, ex);
            }
        }
        // Send reply
        Packet reply = new Packet(con.useDirectBuffers());
        reply.setPacketType(PacketType.OBJECT_MESSAGE);
   
        setProperties(reply, MessageType.GET_DESTINATIONS_REPLY, status, errMsg);
   
        setBodyObject(reply, v);
        parent.sendReply(con, cmd_msg, reply);
 
        return true;
    }

    public static DestinationInfo getDestinationInfo(Destination d) {
        return getDestinationInfo(d, null, false);
    }

    private static DestinationInfo getDestinationInfo(
        Destination d, DestinationInfo dinfo, boolean showpartition) {

        DestinationInfo di = dinfo;
        if (di == null || showpartition) {
            di = new DestinationInfo();
        }
        d.getSizeInfo(di);
        if (dinfo == null || showpartition) {
            di.nConsumers = d.getConsumerCount();
            di.nfConsumers = d.getFailoverConsumerCount();
            di.naConsumers = d.getActiveConsumerCount();
        }
        di.nProducers += d.getProducerCount();
        if (dinfo == null || showpartition) {
            di.autocreated= (d.isAutoCreated() || d.isInternal() || d.isDMQ()
                             || d.isAdmin());
        }
        if (dinfo == null || showpartition) {
            di.destState = d.getState();
        }
        if (d.isAdmin() || !showpartition) {
            di.name=d.getDestinationName();
        } else {
            PartitionedStore pstore = d.getPartitionedStore();
            di.name=d.getDestinationName()+
                "["+pstore.getPartitionID()+(pstore.isPrimaryPartition() ? "*]":"]");
        }
        if (dinfo == null || showpartition) {
            di.type = d.getType() &
                ~(DestType.DEST_INTERNAL | DestType.DEST_AUTO | DestType.DEST_ADMIN);
            di.fulltype = d.getType();
        }

        di.maxMessages += d.getCapacity();
        if (di.maxMessages < 0) {
            di.maxMessages = 0;
        }
        SizeString bc = d.getByteCapacity();
        di.maxMessageBytes += (bc == null ? 0 : bc.getBytes());
        if (di.maxMessageBytes < 0) {
            di.maxMessageBytes = 0;
        }
        bc = d.getMaxByteSize();
        di.maxMessageSize += (bc == null ? 0 : bc.getBytes());
        if (di.maxMessageSize < 0) {
            di.maxMessageSize = 0;
        }
        if (dinfo == null || showpartition) {
            di.destScope = d.getScope();
            di.destLimitBehavior = d.getLimitBehavior();
            di.maxPrefetch = d.getMaxPrefetch();
            di.destCDP = d.getClusterDeliveryPolicy();
            di.maxActiveConsumers = d.getMaxActiveConsumers();
            di.maxFailoverConsumers = d.getMaxFailoverConsumers();
            di.maxProducers = d.getMaxProducers();
            di.maxNumSharedConsumers = d.getMaxNumSharedConsumers();
            di.sharedConsumerFlowLimit = d.getSharedConsumerFlowLimit();
            di.useDMQ = d.getUseDMQ();
            di.validateXMLSchemaEnabled = d.validateXMLSchemaEnabled();
            di.XMLSchemaUriList = d.getXMLSchemaUriList();
            di.reloadXMLSchemaOnFailure = d.reloadXMLSchemaOnFailure();
        }

	if (!d.isQueue())  {
	    Hashtable<String, Integer> h = new Hashtable<String, Integer>();

            if (dinfo == null || showpartition) {

	    if (di.nConsumers > 0)  {
		Iterator consumers = d.getConsumers();

		while (consumers.hasNext())  {
		    Consumer oneCon = (Consumer)consumers.next();

		    if (oneCon.isWildcard())  {
			DestinationUID id = oneCon.getDestinationUID();
			String wildcard = id.getName();

			Integer count = h.get(wildcard), newCount;

			if (count == null)  {
			    newCount = Integer.valueOf(1);
			} else  {
			    newCount = Integer.valueOf(count.intValue() + 1);
			}
			h.put(wildcard, newCount);
		    }
		}
	    }
	    if (h.size() > 0)  {
	        di.consumerWildcards = h;
	    }
            }

	    h = di.producerWildcards; 
            if (h == null) {
                h = new Hashtable<String, Integer>();
            }
	    if (di.nProducers > 0)  {
		Iterator producers = d.getProducers();

		while (producers.hasNext())  {
		    Producer oneProd = (Producer)producers.next();

		    if (oneProd.isWildcard())  {
			DestinationUID id = oneProd.getDestinationUID();
			String wildcard = id.getName();

			Integer count = h.get(wildcard), newCount;

			if (count == null)  {
			    newCount = Integer.valueOf(1);
			} else  {
			    newCount = Integer.valueOf(count.intValue() + 1);
			}
			h.put(wildcard, newCount);
		    }
		}
	    }

	    if (h.size() > 0)  {
	        di.producerWildcards = h;
	    }
	}
 
        return di;
        
    }
}
