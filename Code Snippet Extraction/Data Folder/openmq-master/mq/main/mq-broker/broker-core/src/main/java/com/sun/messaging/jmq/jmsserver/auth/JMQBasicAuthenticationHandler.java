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
 * @(#)JMQBasicAuthenticationHandler.java	1.21 06/28/07
 */ 

package com.sun.messaging.jmq.jmsserver.auth;

import java.io.*;
import java.util.*;
import javax.security.auth.Subject;
import javax.security.auth.Refreshable;
import javax.security.auth.login.LoginException;
import com.sun.messaging.jmq.auth.api.FailedLoginException;
import com.sun.messaging.jmq.util.BASE64Decoder;
import com.sun.messaging.jmq.auth.jaas.MQUser;
import com.sun.messaging.jmq.auth.api.server.*;
import com.sun.messaging.jmq.auth.api.server.model.UserRepository;
import com.sun.messaging.jmq.jmsserver.resources.BrokerResources;
import com.sun.messaging.jmq.jmsserver.Globals;

/**
 * "basic" authentication handler
 */
public class JMQBasicAuthenticationHandler implements AuthenticationProtocolHandler {

    private AccessControlContext acc = null;
    private Properties authProps;
    private Refreshable cacheData = null;
    private UserRepository repository = null;
    private boolean logout = false;

    public String getType() {
        return AccessController.AUTHTYPE_BASIC;
    }

    /**
     * This method is called once before any handleReponse() calls
     *
     * @param sequence packet sequence number 
     * @param authProperties authentication properties
     * @param cacheData the cacheData 
     *
     * @return initial authentication request data if any
     */
    public byte[] init(int sequence, Properties authProperties,
                       Refreshable cacheData) throws LoginException {
       this.authProps = authProperties;
       this.cacheData = cacheData;
       return null;
    }

    /**
     * @param authResponse the authentication response data.
     *                     This is the AUTHENCATE_RESPONSE packet body.
     * @param sequence packet sequence number
     *
     * @return next request data if any; null if no more request.
     *  The request data will be sent as packet body in AUTHENTICATE_REQUEST
     *                 
     * @exception LoginException 
     */
    public synchronized byte[] handleResponse(byte[] authResponse, int sequence) throws LoginException {
        if (repository == null && logout) {
            throw new LoginException(Globals.getBrokerResources().getKString(
                BrokerResources.X_CONNECTION_LOGGEDOUT));
        }
        if (repository != null)  repository.close();

        Subject subject = null;
        acc = null;

        try {
        ByteArrayInputStream bis = new ByteArrayInputStream(authResponse);
        DataInputStream dis = new DataInputStream(bis);

        String username = dis.readUTF();

        BASE64Decoder decoder = new BASE64Decoder();
		String pass = dis.readUTF();
        String password = new String(decoder.decodeBuffer(pass), "UTF8");
        dis.close();

        String rep = authProps.getProperty(
            AccessController.PROP_AUTHENTICATION_PREFIX+ getType()+
                       AccessController.PROP_USER_REPOSITORY_SUFFIX);
        if (rep == null || rep.trim().equals("")) {
            throw new LoginException(
			    Globals.getBrokerResources().getKString(
                  BrokerResources.X_USER_REPOSITORY_NOT_DEFINED, getType()));
        }
        String className = authProps.getProperty(
                     AccessController.PROP_USER_REPOSITORY_PREFIX+ rep +".class");
        if (className == null) {
            throw new LoginException(
            Globals.getBrokerResources().getKString(
            BrokerResources.X_USER_REPOSITORY_CLASS_NOT_DEFINED, rep, getType()));
        }
        repository =  (UserRepository)Class.forName(className).newInstance();
        repository.open(getType(), authProps, cacheData);
        subject = repository.findMatch(username, password, null, getMatchType());
        cacheData = repository.getCacheData();
        if (subject == null) {
            FailedLoginException ex = new FailedLoginException(
			    Globals.getBrokerResources().getKString(
                    BrokerResources.X_FORBIDDEN, username));
	    ex.setUser(username);
	    throw ex;
        }

        acc = new JMQAccessControlContext(new MQUser(username), subject, authProps);
        return null;

        } catch (ClassNotFoundException e) {
            throw new LoginException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"ClassNotFoundException: "+e.getMessage()));
        } catch (IOException e) {
            throw new LoginException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"IOException: "+e.getMessage()));
        } catch (InstantiationException e) {
            throw new LoginException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"InstantiationException: "+e.getMessage()));
        } catch (IllegalAccessException e) {
            throw new LoginException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"IllegalAccessException: "+e.getMessage()));
        } catch (ClassCastException e) {
            throw new LoginException(Globals.getBrokerResources().getString(
               BrokerResources.X_INTERNAL_EXCEPTION,"ClassCastException: "+e.getMessage()));
        }
    }
    
    public AccessControlContext getAccessControlContext() {
        return acc;
    }

    public Refreshable getCacheData() {
        return cacheData;
    }

    public synchronized void logout() throws LoginException { 
        logout = true;
        if (repository != null) repository.close();
    }

    public String getMatchType() {
        return getType();
    }
}
