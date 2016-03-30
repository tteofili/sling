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
package org.apache.sling.distribution.serialization.impl;

import javax.annotation.Nonnull;
import javax.jcr.RepositoryException;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.distribution.DistributionRequestType;
import org.apache.sling.distribution.serialization.DistributionPackage;
import org.apache.sling.distribution.serialization.DistributionPackageInfo;

/**
 * {@link Resource} based {@link DistributionPackage}
 */
public class ResourceDistributionPackage implements DistributionPackage {

    private final String type;
    private final Resource resource;
    private final ResourceResolver resourceResolver;
    private final DistributionPackageInfo info;

    ResourceDistributionPackage(Resource resource, String type, ResourceResolver resourceResolver) {
        this.info = new DistributionPackageInfo(type);
        this.resourceResolver = resourceResolver;
        this.type = type;
        ValueMap valueMap = resource.getValueMap();
        assert type.equals(valueMap.get("type")) : "wrong resource type";
        this.resource = resource;

        this.getInfo().put(DistributionPackageInfo.PROPERTY_REQUEST_TYPE, DistributionRequestType.ADD);
        //this.getInfo().put(DistributionPackageInfo.PROPERTY_REQUEST_PATHS, paths);
    }

    @Nonnull
    @Override
    public String getId() {
        return resource.getPath();
    }

    @Nonnull
    @Override
    public String getType() {
        return type;
    }

    @Nonnull
    @Override
    public InputStream createInputStream() throws IOException {
        try {
            return new BufferedInputStream(PackageUtils.getStream(resource));
        } catch (RepositoryException e) {
            throw new IOException("Cannot create stream", e);
        }
    }

    @Override
    public long getSize() {
        Object size = resource.getValueMap().get("size");
        return size == null ? -1 : Long.parseLong(size.toString());
    }

    @Override
    public void close() {
        // do nothing
    }

    @Override
    public void delete() {
        try {
            resourceResolver.delete(resource);
            resourceResolver.commit();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    @Override
    public DistributionPackageInfo getInfo() {
        return info;
    }
}
