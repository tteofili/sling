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
package org.apache.sling.replication.queue.impl.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import org.apache.sling.replication.agent.ReplicationAgent;
import org.apache.sling.replication.queue.ReplicationQueue;
import org.apache.sling.replication.queue.ReplicationQueueItemState;
import org.apache.sling.replication.serialization.ReplicationPackage;

/**
 * Testcase for {@link SimpleReplicationQueue}
 */
public class SimpleReplicationQueueTest {

    @Test
    public void testPackageAddition() throws Exception {
        ReplicationAgent agent = mock(ReplicationAgent.class);
        ReplicationQueue queue = new SimpleReplicationQueue(agent);
        ReplicationPackage pkg = mock(ReplicationPackage.class);
        assertTrue(queue.add(pkg));
    }

    @Test
    public void testPackageAdditionAndRemoval() throws Exception {
        ReplicationAgent agent = mock(ReplicationAgent.class);
        ReplicationQueue queue = new SimpleReplicationQueue(agent);
        ReplicationPackage pkg = mock(ReplicationPackage.class);
        assertTrue(queue.add(pkg));
        queue.removeHead();
        ReplicationQueueItemState status = queue.getStatus(pkg);
        assertNotNull(status);
        assertTrue(status.isSuccessfull());
    }

    @Test
    public void testPackageAdditionRetrievalAndRemoval() throws Exception {
        ReplicationAgent agent = mock(ReplicationAgent.class);
        ReplicationQueue queue = new SimpleReplicationQueue(agent);
        ReplicationPackage pkg = mock(ReplicationPackage.class);
        assertTrue(queue.add(pkg));
        assertEquals(pkg, queue.getHead());
        queue.removeHead();
        ReplicationQueueItemState status = queue.getStatus(pkg);
        assertNotNull(status);
        assertTrue(status.isSuccessfull());
        assertEquals(1, status.getAttempts());
    }

}
