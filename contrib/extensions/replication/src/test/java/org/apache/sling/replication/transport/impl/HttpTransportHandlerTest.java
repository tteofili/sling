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
package org.apache.sling.replication.transport.impl;

import java.net.URI;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.sling.replication.communication.ReplicationActionType;
import org.apache.sling.replication.communication.ReplicationEndpoint;
import org.apache.sling.replication.serialization.ReplicationPackage;
import org.apache.sling.replication.transport.authentication.AuthenticationContext;
import org.apache.sling.replication.transport.authentication.AuthenticationHandler;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testcase for {@link HttpTransportHandler}
 */
public class HttpTransportHandlerTest {
    @Test
    public void testHttpTransport() throws Exception {
        HttpTransportHandler httpTransportHandler = new HttpTransportHandler();
        ReplicationPackage replicationPackage = mock(ReplicationPackage.class);
        when(replicationPackage.getAction()).thenReturn(ReplicationActionType.ACTIVATE.toString());
        when(replicationPackage.getType()).thenReturn("test");
        when(replicationPackage.getPaths()).thenReturn(new String[]{"/"});
        ReplicationEndpoint replicationEndpoint = new ReplicationEndpoint(new URI("http://localhost:8080/system/replication/receive"));
        AuthenticationHandler<Executor, Executor> authenticationHandler = mock(AuthenticationHandler.class);
        Executor executor = mock(Executor.class);
        Response response = mock(Response.class);
        Content content = mock(Content.class);
        when(response.returnContent()).thenReturn(content);
        when(executor.execute(any(Request.class))).thenReturn(response);
        when(authenticationHandler.authenticate(any(Executor.class), any(AuthenticationContext.class))).thenReturn(executor);
        httpTransportHandler.transport(replicationPackage, replicationEndpoint, authenticationHandler);
    }
}
