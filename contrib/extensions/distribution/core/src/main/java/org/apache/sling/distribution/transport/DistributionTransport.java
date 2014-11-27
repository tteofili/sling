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
package org.apache.sling.distribution.transport;

import javax.annotation.Nonnull;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.distribution.communication.DistributionRequest;
import org.apache.sling.distribution.packaging.DistributionPackage;

/**
 * A transport layer implementation to transport data between two (or more, eventually) Sling instances.
 * <p/>
 * Each implementation is meant to be stateful in the sense that it will hide the details about the sending / receiving
 * endpoints of the transport.
 */
public interface DistributionTransport {

    @Nonnull
    DistributionTransportResult deliverPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionPackage
            distributionPackage, @Nonnull DistributionTransportSecret secret) throws DistributionTransportException;

    @Nonnull
    Iterable<DistributionPackage> retrievePackages(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionRequest
            request, @Nonnull DistributionTransportSecret secret) throws DistributionTransportException;

}
