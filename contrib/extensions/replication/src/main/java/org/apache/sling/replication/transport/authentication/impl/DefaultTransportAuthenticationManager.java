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

import java.util.HashMap;
import java.util.Map;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.ReferencePolicy;
import org.apache.felix.scr.annotations.References;
import org.apache.sling.replication.transport.authentication.TransportAuthenticationManager;
import org.apache.sling.replication.transport.authentication.TransportAuthenticationProvider;
import org.apache.sling.replication.transport.authentication.TransportAuthenticationProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@References({ 
    @Reference(name = "authenticationHandlerFactory", 
                    referenceInterface = TransportAuthenticationProviderFactory.class,
                    cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE, 
                    policy = ReferencePolicy.DYNAMIC, 
                    bind = "bindAuthenticationHandlerFactory", 
                    unbind = "unbindAuthenticationHandlerFactory")
    })
public class DefaultTransportAuthenticationManager implements TransportAuthenticationManager {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, TransportAuthenticationProviderFactory> authenticationHandlerFactories = new HashMap<String, TransportAuthenticationProviderFactory>();

    @Deactivate
    protected void deactivate() {
        authenticationHandlerFactories.clear();
    }

    public TransportAuthenticationProvider<?, ?> getAuthenticationProvider(String type,
                                                                           Map<String, String> properties) {
        TransportAuthenticationProvider<?, ?> transportAuthenticationProvider = null;
        TransportAuthenticationProviderFactory transportAuthenticationProviderFactory = authenticationHandlerFactories
                        .get(type);
        if (transportAuthenticationProviderFactory != null) {
            transportAuthenticationProvider = transportAuthenticationProviderFactory
                            .createAuthenticationProvider(properties);
        }
        return transportAuthenticationProvider;
    }

    public void bindAuthenticationHandlerFactory(
                    TransportAuthenticationProviderFactory transportAuthenticationProviderFactory) {
        synchronized (authenticationHandlerFactories) {
            authenticationHandlerFactories.put(transportAuthenticationProviderFactory.getType(),
                    transportAuthenticationProviderFactory);
        }
        if (log.isInfoEnabled()) {
            log.info("Registered AuthenticationHandlerFactory {}", transportAuthenticationProviderFactory);
        }
    }

    public void unbindAuthenticationHandlerFactory(
                    TransportAuthenticationProviderFactory transportAuthenticationProviderFactory) {
        synchronized (authenticationHandlerFactories) {
            authenticationHandlerFactories.remove(transportAuthenticationProviderFactory.getType());
        }
        if (log.isInfoEnabled()) {
            log.info("Unregistered AuthenticationHandlerFactory {}", transportAuthenticationProviderFactory);
        }
    }

}
