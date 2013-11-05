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
import java.util.Arrays;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.entity.ContentType;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.sling.replication.agent.ReplicationAgent;
import org.apache.sling.replication.agent.impl.ReplicationAgentResource;
import org.apache.sling.replication.queue.ReplicationQueue;
import org.apache.sling.replication.serialization.ReplicationPackage;

@Component(metatype = false)
@Service(value = Servlet.class)
@Properties({
        @Property(name = "sling.servlet.resourceTypes", value = ReplicationAgentResource.RESOURCE_TYPE),
        @Property(name = "sling.servlet.methods", value = "GET") })
public class ReplicationAgentPollServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response)
                    throws ServletException, IOException {

        response.setContentType(ContentType.APPLICATION_OCTET_STREAM.toString());

        String queueName = request.getParameter("queue");

        ReplicationAgent agent = request.getResource().adaptTo(ReplicationAgent.class);

        if (agent != null) {
            try {
                // TODO : consider using queue distribution strategy some way and validating who's making this request
                ReplicationQueue queue = agent.getQueue(queueName);
                // get first item
                ReplicationPackage head = queue.getHead();
                if (head != null) {
                    int bytesCopied = IOUtils.copy(head.getInputStream(),
                                    response.getOutputStream());
                    response.setHeader("type", head.getType());
                    response.setHeader("action", head.getAction().toString());
                    response.setHeader("path", Arrays.toString(head.getPaths()));
                    if (log.isInfoEnabled()) {
                        log.info("{} bytes written into the response", bytesCopied);
                    }
                } else {
                    if (log.isInfoEnabled()) {
                        log.info("nothing to fetch");
                    }
                }
            } catch (Exception e) {
                response.setStatus(503);
                if (log.isErrorEnabled()) {
                    log.error("error while reverse replicating from agent", e);
                }
            }
            // everything ok
            response.setStatus(200);
        } else {
            response.setStatus(404);
        }
    }

}
