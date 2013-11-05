/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.replication.transport.authentication.impl;

import java.util.Map;
import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.replication.transport.authentication.AuthenticationHandler;
import org.apache.sling.replication.transport.authentication.AuthenticationHandlerFactory;


@Component(immediate = true)
@Service(value = AuthenticationHandlerFactory.class)
@Property(name = "name", value = RepositoryAuthenticationHandlerFactory.TYPE)
public class RepositoryAuthenticationHandlerFactory implements AuthenticationHandlerFactory {
    public static final String TYPE = "repo-user";

    public AuthenticationHandler<SlingRepository, Session> createAuthenticationHandler(Map<String, String> properties) {
        String user = null;
        Object userProp = properties.get("user");
        if (userProp != null) {
            user = String.valueOf(userProp);
        }
        String password = null;
        Object passwordProp = properties.get("password");
        if (passwordProp != null) {
            password = String.valueOf(passwordProp);
        }
        return new RepositoryAuthenticationHandler(user, password);
    }

    public String getType() {
        return TYPE;
    }

}
