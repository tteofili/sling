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
package org.apache.sling.replication.agent.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import org.apache.sling.replication.agent.AgentConfigurationException;
import org.apache.sling.replication.agent.ReplicationAgent;
import org.apache.sling.replication.agent.ReplicationAgentConfiguration;
import org.apache.sling.replication.agent.ReplicationAgentConfigurationManager;

/**
 * Testcase for {@link ReplicationAgentResourceProvider}
 */
public class ReplicationAgentResourceProviderTest {

    @Test
    public void testAgentResolutionWithoutSpecifiedAgentService() throws Exception {
        BundleContext context = mock(BundleContext.class);
        ReplicationAgentResourceProvider agentResourceProvider = new ReplicationAgentResourceProvider();
        agentResourceProvider.activate(context);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);
        String path = "publish";
        Resource resource = agentResourceProvider.getResource(resourceResolver, path);
        assertNull(resource);
    }

    @Test
    public void testAgentResolutionWithSpecifiedAgentService() throws Exception {
        String path = "publish";
        BundleContext context = mock(BundleContext.class);
        createMockedReplicationAgent(path, context);
        ReplicationAgentResourceProvider agentResourceProvider = new ReplicationAgentResourceProvider();
        agentResourceProvider.activate(context);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);

        Resource resource = agentResourceProvider.getResource(resourceResolver, path);
        assertNotNull(resource);
        assertEquals(ReplicationAgentResource.RESOURCE_TYPE, resource.getResourceType());
    }

    @Test
    public void testAgentConfigurationResolutionWithSpecifiedAgentService() throws Exception {
        String path = "publish";
        BundleContext context = mock(BundleContext.class);
        ReplicationAgent replicationAgent = createMockedReplicationAgent(path, context);
        mockReplicationAgentConfiguration(context, replicationAgent);
        ReplicationAgentResourceProvider agentResourceProvider = new ReplicationAgentResourceProvider();
        agentResourceProvider.activate(context);
        ResourceResolver resourceResolver = mock(ResourceResolver.class);

        Resource resource = agentResourceProvider.getResource(resourceResolver, path
                        + "/configuration");
        assertNotNull(resource);
        assertEquals(ReplicationAgentConfigurationResource.RESOURCE_TYPE, resource.getResourceType());
    }

    private void mockReplicationAgentConfiguration(BundleContext context,
                    ReplicationAgent replicationAgent) throws AgentConfigurationException {
        ServiceReference configurationManagerServiceReference = mock(ServiceReference.class);
        when(context.getServiceReference(ReplicationAgentConfigurationManager.class.getName()))
                        .thenReturn(configurationManagerServiceReference);
        ReplicationAgentConfigurationManager configurationManager = mock(ReplicationAgentConfigurationManager.class);
        when(context.getService(configurationManagerServiceReference)).thenReturn(
                        configurationManager);
        ReplicationAgentConfiguration configuration = mock(ReplicationAgentConfiguration.class);
        when(configurationManager.getConfiguration(replicationAgent)).thenReturn(configuration);
    }

    private SimpleReplicationAgentImpl createMockedReplicationAgent(String path,
                    BundleContext context) throws InvalidSyntaxException {
        ServiceReference serviceReference = mock(ServiceReference.class);
        ServiceReference[] agentServiceReferences = new ServiceReference[] { serviceReference };
        String filter = new StringBuilder("(name=").append(path).append(")").toString();
        when(context.getServiceReferences(ReplicationAgent.class.getName(), filter)).thenReturn(
                        agentServiceReferences);
        SimpleReplicationAgentImpl replicationAgent = new SimpleReplicationAgentImpl(path, null,
                        null, null, null, null, null);
        when(context.getService(serviceReference)).thenReturn(replicationAgent);
        return replicationAgent;
    }

}
