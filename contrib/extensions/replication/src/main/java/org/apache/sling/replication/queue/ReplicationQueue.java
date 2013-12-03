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
package org.apache.sling.replication.queue;

import org.apache.sling.replication.agent.ReplicationAgent;
import org.apache.sling.replication.serialization.ReplicationPackage;

/**
 * a queue for handling {@link org.apache.sling.replication.agent.ReplicationAgent}s' requests
 */
public interface ReplicationQueue {

    /**
     * get this queue name
     * @return queue name as a <code>String</code>
     */
    String getName();

    /**
     * add a replication package to this queue
     *
     * @param replicationPackage
     *            a replication package to replicate
     * @return <code>true</code> if the replication package was added correctly to the queue,
     *         <code>false</code otherwise
     * @throws ReplicationQueueException
     */
    boolean add(ReplicationPackage replicationPackage) throws ReplicationQueueException;

    /**
     * get the status of a certain package in the queue
     *
     * @param replicationPackage
     *            the replication package to get the status for
     * @return the item status in the queue
     * @throws ReplicationQueueException
     */
    ReplicationQueueItemState getStatus(ReplicationPackage replicationPackage)
                    throws ReplicationQueueException;

    /**
     * get the agent this queue is used for
     *
     * @return a replication agent
     */
    ReplicationAgent getAgent();

    /**
     * get the first item (FIFO wise, the next to be processed) into the queue
     *
     * @return the first replication package into the queue
     */
    ReplicationPackage getHead();

    /**
     * remove the first package into the queue from it
     *
     * @throws ReplicationQueueException
     */
    void removeHead();

    /**
     * check if the queue is empty
     *
     * @return <code>true</code> if the queue is empty, <code>false</code> otherwise
     */
    boolean isEmpty();
}
