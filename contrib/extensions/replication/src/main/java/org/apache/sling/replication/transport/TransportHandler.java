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
package org.apache.sling.replication.transport;

import org.apache.sling.replication.transport.authentication.AuthenticationHandler;
import org.apache.sling.replication.communication.ReplicationEndpoint;
import org.apache.sling.replication.serialization.ReplicationPackage;

/**
 * A <code>TransportHandler</code> is responsible for implementing the transport of a
 * {@link ReplicationPackage}s to / from another instance described by a {@link ReplicationEndpoint}
 */
public interface TransportHandler {

    void transport(ReplicationPackage replicationPackage, ReplicationEndpoint replicationEndpoint,
                    AuthenticationHandler<?, ?> authenticationHandler)
                    throws ReplicationTransportException;

    boolean supportsAuthenticationHandler(AuthenticationHandler<?, ?> authenticationHandler);

}