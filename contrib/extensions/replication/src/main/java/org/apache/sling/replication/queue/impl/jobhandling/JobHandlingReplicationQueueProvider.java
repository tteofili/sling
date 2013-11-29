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

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.event.impl.jobs.config.ConfigurationConstants;
import org.apache.sling.event.jobs.JobManager;
import org.apache.sling.event.jobs.Queue;
import org.apache.sling.event.jobs.QueueConfiguration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

import org.apache.sling.replication.agent.ReplicationAgent;
import org.apache.sling.replication.queue.ReplicationQueue;
import org.apache.sling.replication.queue.ReplicationQueueException;
import org.apache.sling.replication.queue.ReplicationQueueProvider;
import org.apache.sling.replication.queue.impl.AbstractReplicationQueueProvider;

@Component(metatype = false)
@Service(value = ReplicationQueueProvider.class)
@Property(name = "name", value = JobHandlingReplicationQueueProvider.NAME)
public class JobHandlingReplicationQueueProvider extends AbstractReplicationQueueProvider
                implements ReplicationQueueProvider {

    public static final String NAME = "sjh";

    @Reference
    private JobManager jobManager;

    @Reference
    private ConfigurationAdmin configAdmin;

    @Override
    protected ReplicationQueue getOrCreateQueue(ReplicationAgent agent, String queueName)
                    throws ReplicationQueueException {
        try {
            String name = agent.getName() + queueName;
            String topic = JobHandlingReplicationQueue.REPLICATION_QUEUE_TOPIC + '/' + agent.getName() + queueName;
            if (jobManager.getQueue(name) == null) {
                Configuration config = configAdmin.createFactoryConfiguration(
                                QueueConfiguration.class.getName(), null);
                Dictionary<String, Object> props = new Hashtable<String, Object>();
                props.put(ConfigurationConstants.PROP_NAME, name);
                props.put(ConfigurationConstants.PROP_TYPE, QueueConfiguration.Type.ORDERED.name());
                props.put(ConfigurationConstants.PROP_TOPICS, new String[] { topic });
                props.put(ConfigurationConstants.PROP_RETRIES, -1);
                props.put(ConfigurationConstants.PROP_RETRY_DELAY, 2000L);
                props.put(ConfigurationConstants.PROP_KEEP_JOBS, true);
                props.put(ConfigurationConstants.PROP_PRIORITY, "MAX");
                config.update(props);
            }
            return new JobHandlingReplicationQueue(name, topic, jobManager);
        } catch (IOException e) {
            throw new ReplicationQueueException("could not create a queue", e);
        }
    }

    @Override
    protected void deleteQueue(ReplicationQueue queue) throws ReplicationQueueException {
        Queue q = jobManager.getQueue(queue.getAgent().getName());
        q.removeAll(); // need to check if this is correct
    }

}
