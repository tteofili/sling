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
package org.apache.sling.replication.servlet;

import java.io.IOException;
import java.util.SortedSet;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.sling.replication.agent.AgentReplicationException;
import org.apache.sling.replication.agent.ReplicationAgent;
import org.apache.sling.replication.agent.ReplicationAgentsManager;
import org.apache.sling.replication.communication.ReplicationActionType;
import org.apache.sling.replication.communication.ReplicationRequest;

/**
 * Servlet for aggregate replication on all agents
 */
@SuppressWarnings("serial")
@Component(metatype = false)
@Service(value = Servlet.class)
@Properties({
    @Property(name = "sling.servlet.paths", value = "/system/replication/agents"),
    @Property(name = "sling.servlet.methods", value = "POST")
})
public class AggregateReplicationServlet extends SlingAllMethodsServlet {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String PATH_PARAMETER = "path";

    private static final String ACTION_PARAMETER = "action";

    @Reference
    private ReplicationAgentsManager replicationAgentsManager;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response)
                    throws ServletException, IOException {

        String a = request.getParameter(ACTION_PARAMETER);
        String[] paths = request.getParameterValues(PATH_PARAMETER);

        ReplicationActionType action = ReplicationActionType.fromName(a);
        SortedSet<ReplicationAgent> agents = replicationAgentsManager.getAgentsFor(action, paths);

        ReplicationRequest replicationRequest = new ReplicationRequest(System.currentTimeMillis(),
                        action, paths);

        boolean failed = false;
        for (ReplicationAgent agent : agents) {
            try {
                agent.send(replicationRequest);
            } catch (AgentReplicationException e) {
                if (log.isWarnEnabled()) {
                    log.warn("agent {} failed", agent.getName(), e);
                }
                response.getWriter().append("error :'").append(e.toString()).append("'");
                if (!failed) {
                    failed = true;
                }
            }
        }
        if (failed) {
            response.setStatus(503);
            response.getWriter().append("status : ").append("503");
        }
        else {
            response.setStatus(200);
        }
    }
}
