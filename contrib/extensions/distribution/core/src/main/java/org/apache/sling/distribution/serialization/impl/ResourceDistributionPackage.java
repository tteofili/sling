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
    private final String id;
    private final long size;
    private final Resource resource;
    private final ResourceResolver resourceResolver;
    private final DistributionPackageInfo info;

    public ResourceDistributionPackage(Resource resource, String type, ResourceResolver resourceResolver) {
        this.info = new DistributionPackageInfo(type);
        this.resourceResolver = resourceResolver;
        this.type = type;
        ValueMap valueMap = resource.getValueMap();
        assert type.equals(valueMap.get("type")) : "wrong resource type";
        Object size = valueMap.get("size");
        this.size = size == null ? -1 : Long.parseLong(size.toString());
        this.resource = resource;
        this.id = resource.getPath();
    }

    public ResourceDistributionPackage(InputStream stream, long size, String type, ResourceResolver resourceResolver,
                                       String packagesPath) {
        this.info = new DistributionPackageInfo(type);
        this.resourceResolver = resourceResolver;
        this.type = type;
        this.size = size;
        Resource packagesResource;
        try {
            packagesResource = ResourceUtil.getOrCreateResource(resourceResolver, packagesPath, "sling:Folder",
                    "sling:Folder", true);
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }
        String name = type + "-" + System.currentTimeMillis();
        this.id = packagesPath + "/" + name;
        Map<String, Object> props = new HashMap<String, Object>();
        props.put("type", type);
        props.put("bin", stream);
        if (size != -1) {
            props.put("size", size);
        }

        try {
            this.resource = resourceResolver.create(packagesResource, name, props);
            if (resource == null) {
                throw new RuntimeException("could not persist resource " + name + " : " + props);
            }
            resourceResolver.commit();
        } catch (PersistenceException e) {
            throw new RuntimeException(e);
        }

    }

    @Nonnull
    @Override
    public String getId() {
        return id;
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
        return size;
    }

    @Override
    public void close() {
        resourceResolver.close();
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