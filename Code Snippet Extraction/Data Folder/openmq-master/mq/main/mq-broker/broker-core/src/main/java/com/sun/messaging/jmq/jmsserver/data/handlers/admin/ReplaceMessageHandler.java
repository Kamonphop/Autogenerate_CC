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
 * @(#)ReplaceMessageHandler.java	1.5 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.data.handlers.admin;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;
import java.util.Hashtable;
import java.util.HashMap;
import java.nio.ByteBuffer;

import com.sun.messaging.jmq.io.Packet;
import com.sun.messaging.jmq.jmsserver.service.imq.IMQConnection;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.io.*;
import com.sun.messaging.jmq.util.admin.MessageType;
import com.sun.messaging.jmq.util.log.Logger;
import com.sun.messaging.jmq.jmsserver.Globals;
import com.sun.messaging.jmq.jmsserver.core.Destination;
import com.sun.messaging.jmq.jmsserver.core.PacketReference;
import com.sun.messaging.jmq.jmsserver.util.BrokerException;

public class ReplaceMessageHandler extends AdminCmdHandler  {
    private static boolean DEBUG = getDEBUG();

    public ReplaceMessageHandler(AdminDataHandler parent) {
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

        if ( DEBUG ) {
            logger.log(Logger.DEBUG, this.getClass().getName() + ": " +
                            "Replace message: " + cmd_props);
        }

        int status = Status.OK;
        String errMsg = null;

        String destination = (String)cmd_props.get(MessageType.JMQ_DESTINATION);
        Integer destType = (Integer)cmd_props.get(MessageType.JMQ_DEST_TYPE);
        String msgID = (String)cmd_props.get(MessageType.JMQ_MESSAGE_ID);
        Object body = null;

	if (destType == null)  {
            errMsg = "REPLACE_MESSAGE: destination type not specified";
            logger.log(Logger.ERROR, errMsg);
            status = Status.ERROR;
	}
        body = getBodyObject(cmd_msg);

        String newMsgID = null;
        Hashtable newMsgIDHash = null;

        if (status == Status.OK) {
            try {
                newMsgID = replaceMessage(msgID, destination, body, 
                                DestType.isQueue(destType.intValue()));
                newMsgIDHash = new Hashtable();
                newMsgIDHash.put(MessageType.JMQ_MESSAGE_ID, newMsgID);
            } catch (Exception e) {
                status = Status.ERROR;
		errMsg = e.getMessage();
                boolean logstack = true;
                if (e instanceof BrokerException) {
                    status = ((BrokerException)e).getStatusCode();
                    if (status == Status.NOT_ALLOWED || 
                        status == Status.NOT_FOUND  ||
                        status == Status.BAD_REQUEST ||
                        status == Status.CONFLICT) {
                        logstack = false;
                    }
                }
                Object[] args = { msgID, ""+destination, e.getMessage() };
                errMsg = rb.getKString(rb.X_ADMIN_REPLACE_MSG, args);
                if (logstack) {
                    logger.logStack(Logger.ERROR, errMsg, e);
                } else {
                    logger.log(Logger.ERROR, errMsg,  e);
                }
            }
        }

        Packet reply = new Packet(con.useDirectBuffers());
        reply.setPacketType(PacketType.OBJECT_MESSAGE);
        setProperties(reply, MessageType.REPLACE_MESSAGE_REPLY,
                      status, errMsg, newMsgIDHash);
        parent.sendReply(con, cmd_msg, reply);
        return true;
    }

    /**
     * @return new message id,  not null;
     */
    public String replaceMessage(String msgID, String destination,
        Object body, boolean isQueue)
        throws BrokerException, IOException {

	if (msgID == null)  {
            String emsg = "REPLACE_MESSAGE: Message ID not specified";
            throw  new BrokerException(emsg, Status.BAD_REQUEST);
	}
	if (destination == null)  {
            String emsg = "REPLACE_MESSAGE: Message ID not specified";
            throw  new BrokerException(emsg, Status.BAD_REQUEST);
	}
	if (body == null || !(body instanceof HashMap))  {
            String emsg = "REPLACE_MESSAGE: New message body specified or is of incorrect type";
            throw  new BrokerException(emsg, Status.BAD_REQUEST);
	} 
        HashMap hashMapBody = (HashMap)body;
        Destination[] ds= DL.getDestination(null, destination, isQueue);
        Destination d = ds[0]; //PART
        if (d == null) {
            String emsg = "REPLACE_MESSAGE: "+rb.getString(rb.X_DESTINATION_NOT_FOUND, destination);
            throw new BrokerException(emsg, Status.NOT_FOUND);
        }
        if (DEBUG) {
            d.debug();
        }
        d.load();

        SysMessageID sysMsgID = SysMessageID.get(msgID);
        PacketReference pr = getPacketReference(sysMsgID);
        if (pr == null)  {
            String emsg = "REPLACE_MESSAGE: Could not locate message "+msgID+ 
                          " in destination " + destination;
            throw new BrokerException(emsg, Status.NOT_FOUND);
        }

        logger.log(Logger.INFO, rb.getKString(
            rb.I_ADMIN_REPLACE_MESSAGE, msgID, d.getDestinationUID()));

        if (!pr.isLocal()) {
            Object[] args = { msgID, d.getDestinationUID(), pr.getBrokerAddress() };
            String emsg = rb.getKString(rb.E_ADMIN_REPLACE_REMOTE_MSG, args);
            throw new BrokerException(emsg, Status.NOT_ALLOWED);
        }

        int oldPacketType = pr.getPacket().getPacketType();
        Integer newPacketType = (Integer)hashMapBody.get("MessageBodyType");
        if (newPacketType == null) {
            String emsg = "REPLACE_MESSAGE: MessageBodyType not specified"; 
            throw new BrokerException(emsg, Status.BAD_REQUEST);
        } 

        if (oldPacketType != newPacketType.intValue())  {
            String emsg = "REPLACE_MESSAGE: Existing message and new message types do not match.";
            throw new BrokerException(emsg, Status.BAD_REQUEST);
        }

        byte[] bytes = getBytesFromMessage(hashMapBody);
        String newMsgID = d.replaceMessageString(sysMsgID, null, bytes);
        if (newMsgID == null)  {
            String emsg = "REPLACE_MESSAGE: New message ID not returned as expected.";
            throw new BrokerException(emsg);
        }
        Object[] args = { msgID, newMsgID, d.getDestinationUID() }; 
        logger.log(logger.INFO, rb.getKString(rb.I_ADMIN_REPLACED_MESSAGE, args));
        return newMsgID;
    }

    public static byte[] getBytesFromMessage(HashMap h) 
    throws BrokerException {
        byte ba[] = null;

	Object msgBody = h.get("MessageBody");
	Integer msgType = (Integer)h.get("MessageBodyType");

        switch (msgType.intValue())  {
        case PacketType.TEXT_MESSAGE:
	    try  {
                String textMsg = (String)msgBody;
	        ba = textMsg.getBytes("UTF8");
	    } catch(Exception e)  {
                String emsg = "Caught exception while creating text message body";
                throw new BrokerException(emsg, e);
	    }
        break;

        case PacketType.BYTES_MESSAGE:
        case PacketType.STREAM_MESSAGE:
	    ba = (byte[])msgBody;
        break;

        case PacketType.MAP_MESSAGE:
            try  {
                ByteArrayOutputStream byteArrayOutputStream = 
	                            new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = 
	                            new ObjectOutputStream(byteArrayOutputStream);

	        HashMap map = (HashMap)msgBody;
	        objectOutputStream.writeObject(map);
	        objectOutputStream.flush();

	        ba = byteArrayOutputStream.toByteArray();
            } catch(Exception e)  {
                String emsg = "Caught exception while creating map message body";
                throw new BrokerException(emsg, e);
            }
        break;

        case PacketType.OBJECT_MESSAGE:
            try  {
                ByteArrayOutputStream byteArrayOutputStream = 
	                            new ByteArrayOutputStream();
                ObjectOutputStream objectOutputStream = 
	                            new ObjectOutputStream(byteArrayOutputStream);

	        objectOutputStream.writeObject(msgBody);
	        objectOutputStream.flush();

	        ba = byteArrayOutputStream.toByteArray();
	    } catch(Exception e)  {
                String emsg = "Caught exception while creating object message body";
                throw new BrokerException(emsg, e);
            }
        break;

	default:
            String emsg = "Unsupported message type for admin REPLACE_MESSAGE handler: "+
                           msgType.intValue();
            throw new BrokerException(emsg, Status.BAD_REQUEST);
        }

        return (ba);
    }

    public static PacketReference getPacketReference(SysMessageID sysMsgID)  {
        return Globals.getDestinationList().get(null, sysMsgID);
    }
}
