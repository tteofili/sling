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
package org.apache.sling.replication.queue.impl.jobhandling;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.sling.event.jobs.JobBuilder;
import org.apache.sling.event.jobs.JobManager;
import org.junit.Test;

import org.apache.sling.replication.queue.ReplicationQueue;
import org.apache.sling.replication.queue.ReplicationQueueItemState;
import org.apache.sling.replication.queue.ReplicationQueueItemState.ItemState;
import org.apache.sling.replication.serialization.ReplicationPackage;

/**
 * Testcase for {@link JobHandlingReplicationQueue}
 */
public class JobHandlingReplicationQueueTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testPackageAddition() throws Exception {
        JobManager jobManager = mock(JobManager.class);
        JobBuilder builder = mock(JobBuilder.class);
        String topic = JobHandlingReplicationQueue.REPLICATION_QUEUE_TOPIC + "/aname";
        when(jobManager.createJob(topic)).thenReturn(builder);
        when(builder.properties(any(Map.class))).thenReturn(builder);
        ReplicationQueue queue = new JobHandlingReplicationQueue("aname", topic, jobManager);
        ReplicationPackage pkg = mock(ReplicationPackage.class);
        InputStream stream = new ByteArrayInputStream("rep".getBytes());
        when(pkg.getInputStream()).thenReturn(stream);
        assertTrue(queue.add(pkg));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPackageAdditionAndStatusCheck() throws Exception {
        JobManager jobManager = mock(JobManager.class);
        JobBuilder builder = mock(JobBuilder.class);
        String topic = JobHandlingReplicationQueue.REPLICATION_QUEUE_TOPIC + "/aname";
        when(jobManager.createJob(topic)).thenReturn(builder);
        when(builder.properties(any(Map.class))).thenReturn(builder);
        ReplicationQueue queue = new JobHandlingReplicationQueue("aname", topic, jobManager);
        ReplicationPackage pkg = mock(ReplicationPackage.class);
        InputStream stream = new ByteArrayInputStream("rep".getBytes());
        when(pkg.getInputStream()).thenReturn(stream);
        assertTrue(queue.add(pkg));
        ReplicationQueueItemState status = queue.getStatus(pkg);
        assertNotNull(status);
        assertFalse(status.isSuccessful());
        assertEquals(ItemState.DROPPED, status.getItemState());
    }

}
