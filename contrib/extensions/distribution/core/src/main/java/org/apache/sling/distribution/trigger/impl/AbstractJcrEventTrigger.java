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
package org.apache.sling.distribution.trigger.impl;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.sling.distribution.DistributionRequest;
import org.apache.sling.distribution.DistributionRequestType;
import org.apache.sling.distribution.SimpleDistributionRequest;
import org.apache.sling.distribution.trigger.DistributionRequestHandler;
import org.apache.sling.distribution.trigger.DistributionTrigger;
import org.apache.sling.distribution.trigger.DistributionTriggerException;
import org.apache.sling.distribution.util.DistributionJcrUtils;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a {@link org.apache.sling.distribution.trigger.DistributionTrigger} that listens for 'safe'
 * events and triggers a {@link org.apache.sling.distribution.DistributionRequest} from that.
 */
public abstract class AbstractJcrEventTrigger implements DistributionTrigger {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Map<String, JcrEventDistributionTriggerListener> registeredListeners = new ConcurrentHashMap<String, JcrEventDistributionTriggerListener>();

    private final String path;
    private final String serviceUser;

    private final SlingRepository repository;

    private Session cachedSession;

    AbstractJcrEventTrigger(SlingRepository repository, String path, String serviceUser) {
        if (path == null || serviceUser == null) {
            throw new IllegalArgumentException("path and service are required");
        }
        this.repository = repository;
        this.path = path;
        this.serviceUser = serviceUser;
    }

    public void register(@Nonnull DistributionRequestHandler requestHandler) throws DistributionTriggerException {
        Session session;
        try {
            session = getSession();
            JcrEventDistributionTriggerListener listener = new JcrEventDistributionTriggerListener(requestHandler);
            registeredListeners.put(requestHandler.toString(), listener);
            session.getWorkspace().getObservationManager().addEventListener(
                    listener, getEventTypes(), path, true, null, null, false);
        } catch (RepositoryException e) {
            throw new DistributionTriggerException("unable to register handler " + requestHandler, e);
        }
    }

    public void unregister(@Nonnull DistributionRequestHandler requestHandler) throws DistributionTriggerException {
        JcrEventDistributionTriggerListener listener = registeredListeners.get(requestHandler.toString());
        if (listener != null) {
            Session session;
            try {
                session = getSession();
                session.getWorkspace().getObservationManager().removeEventListener(listener);
            } catch (RepositoryException e) {
                throw new DistributionTriggerException("unable to unregister handler " + requestHandler, e);
            }
        }
    }

    private class JcrEventDistributionTriggerListener implements EventListener {
        private final DistributionRequestHandler requestHandler;

        public JcrEventDistributionTriggerListener(DistributionRequestHandler requestHandler) {
            this.requestHandler = requestHandler;
        }

        public void onEvent(EventIterator eventIterator) {
            log.info("handling event {}");
            List<DistributionRequest> requestList = new ArrayList<DistributionRequest>();

            while (eventIterator.hasNext()) {
                Event event = eventIterator.nextEvent();
                try {
                    if (DistributionJcrUtils.isSafe(event)) {
                        DistributionRequest request = processEvent(event);
                        if (request != null) {
                            addToList(request, requestList);
                        }

                    }
                } catch (RepositoryException e) {
                    log.error("Error while handling event {}", event, e);
                }
            }

            for (DistributionRequest request: requestList) {
                requestHandler.handle(request);
            }
        }
    }

    private void addToList(DistributionRequest request, List<DistributionRequest> requestList) {
        DistributionRequest lastRequest = requestList.isEmpty()? null : requestList.get(requestList.size() - 1);

        if (lastRequest == null || lastRequest.getRequestType() == null || !lastRequest.getRequestType().equals(request.getRequestType())) {
            requestList.add(request);
        } else if (hasDeepPaths(request) || hasDeepPaths(lastRequest)) {
            requestList.add(request);
        } else {
            Set<String> allPaths = new TreeSet<String>();
            allPaths.addAll(Arrays.asList(lastRequest.getPaths()));
            allPaths.addAll(Arrays.asList(request.getPaths()));
            lastRequest = new SimpleDistributionRequest(lastRequest.getRequestType(), allPaths.toArray(new String[0]));
            requestList.set(requestList.size() - 1, lastRequest);
        }
    }

    public void enable() {

    }

    public void disable() {
        for (JcrEventDistributionTriggerListener listener: registeredListeners.values()) {
            Session session;
            try {
                session = getSession();
                session.getWorkspace().getObservationManager().removeEventListener(listener);
            } catch (RepositoryException e) {
                log.error("unable to unregister handler {}", listener, e);
            }
        }
        registeredListeners.clear();

        if (cachedSession != null) {
            cachedSession.logout();
            cachedSession = null;
        }
    }

    /**
     * process the received event and generates a distribution request
     *
     * @param event an {@link javax.jcr.observation.Event} to be processed
     * @return the {@link org.apache.sling.distribution.DistributionRequest} originated by processing the event,
     * or <code>null</code> if no request could be generated
     * @throws RepositoryException
     */
    protected abstract DistributionRequest processEvent(Event event) throws RepositoryException;

    /**
     * get the binary int event types to be handled by this JCR event listener
     *
     * @return a <code>int</code> as generated by e.g. <code>Event.NODE_ADDED | Event.NODE_REMOVED</code>
     */
    int getEventTypes() {
        return Event.NODE_ADDED | Event.NODE_REMOVED | Event.PROPERTY_CHANGED |
                Event.PROPERTY_ADDED | Event.PROPERTY_REMOVED;
    }

    /**
     * return a newly initiated JCR session to register the {@link javax.jcr.observation.EventListener}
     *
     * @return a {@link javax.jcr.Session}
     * @throws RepositoryException
     */
    Session getSession() throws RepositoryException {
        return cachedSession != null ? cachedSession
                : (cachedSession = repository.loginAdministrative(null)); // TODO: change after SLING-4312
                // : (cachedSession = repository.loginService(serviceUser, null));
    }


    protected String getNodePathFromEvent(Event event) throws RepositoryException {
        String eventPath = event.getPath();
        int type = event.getType();

        if (eventPath == null) {
            return null;
        }

        if (Event.PROPERTY_REMOVED == type || Event.PROPERTY_CHANGED == type || Event.PROPERTY_ADDED == type) {
            eventPath = eventPath.substring(0, eventPath.lastIndexOf('/'));
        }

        return eventPath;
    }


    boolean hasDeepPaths(DistributionRequest distributionRequest) {
        if (!DistributionRequestType.ADD.equals(distributionRequest.getRequestType())) {
            return false;
        }

        for (String path: distributionRequest.getPaths()) {
            if (distributionRequest.isDeep(path)) {
                return true;
            }
        }
        return false;
    }


}
