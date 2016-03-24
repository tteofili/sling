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
    }

    static ResourceDistributionPackage fromStream(InputStream stream, long size, String type, ResourceResolver resourceResolver,
                                                         String packagesPath) throws PersistenceException {
        Resource packagesResource = ResourceUtil.getOrCreateResource(resourceResolver, packagesPath, "sling:Folder",
                "sling:Folder", true);
        String name = type + "-" + System.currentTimeMillis();
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("type", type);
        props.put("bin", stream);
        if (size != -1) {
            props.put("size", size);
        }

        Resource resource = resourceResolver.create(packagesResource, name, props);
        if (resource == null) {
            throw new PersistenceException("could not persist resource " + name + " : " + props);
        }
        resourceResolver.commit();

        return new ResourceDistributionPackage(resource, type, resourceResolver);
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
        Object bin = resource.getValueMap().get("bin");
        if (bin instanceof byte[]) {
            bin = new ByteArrayInputStream((byte[]) bin);
        }
        return (InputStream) bin;
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