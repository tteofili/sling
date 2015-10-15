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
package org.apache.sling.distribution.serialization.impl.kryo;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.distribution.DistributionRequest;
import org.apache.sling.distribution.packaging.DistributionPackage;
import org.apache.sling.distribution.serialization.DistributionPackageBuilder;
import org.apache.sling.distribution.serialization.DistributionPackageBuildingException;
import org.apache.sling.distribution.serialization.DistributionPackageReadingException;
import org.apache.sling.distribution.serialization.impl.FileDistributionPackage;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link org.apache.sling.distribution.serialization.DistributionPackageBuilder}
 * based on {@link Kryo} (de)serializer for Sling {@link Resource}s.
 */
public class KryoResourceDistributionPackageBuilder implements DistributionPackageBuilder {

    private final Kryo kryo = new Kryo();

    private final Logger log = LoggerFactory.getLogger(getClass());

    public String getType() {
        return "kryo-resource";
    }

    public KryoResourceDistributionPackageBuilder() {
        kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        kryo.addDefaultSerializer(Resource.class, new ResourceSerializer());
//        kryo.setReferences(false);
//        kryo.addDefaultSerializer(ValueMap.class, new ValueMapSerializer());
//        kryo.register(Resource.class, new ResourceSerializer(), 1);
//        kryo.register(ValueMap.class, new ValueMapSerializer(), 2);
    }

    public DistributionPackage createPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionRequest request) throws DistributionPackageBuildingException {
        DistributionPackage distributionPackage;
        try {
            String[] paths = request.getPaths();
            File file = File.createTempFile("dp_" + System.nanoTime(), ".kryo");
            Output output = new Output(new FileOutputStream(file));
            LinkedList<Resource> resources = new LinkedList<Resource>();
            for (String p : paths) {
                Resource resource = resourceResolver.getResource(p);
                if (resource != null) {
                    resources.add(resource);
                    for (Resource child : resource.getChildren()) {
                        if (request.isDeep(child.getPath())) {
                            resources.add(child);
                        }
                    }
                }
            }
            kryo.writeObject(output, resources);
            output.close();
            distributionPackage = new FileDistributionPackage(file, getType());

        } catch (Exception e) {
            throw new DistributionPackageBuildingException(e);
        }
        return distributionPackage;
    }

    public DistributionPackage readPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull InputStream stream) throws DistributionPackageReadingException {
        try {
            File file = File.createTempFile("dp_" + System.nanoTime(), ".kryo");
            OutputStream output = new FileOutputStream(file);
            int copied = IOUtils.copy(stream, output);

            log.debug("copied {} bytes", copied);

            return new FileDistributionPackage(file, getType());
        } catch (Exception e) {
            throw new DistributionPackageReadingException(e);
        }
    }

    public DistributionPackage getPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull String id) {
        File file = new File(id);
        if (file.exists()) {
            return new FileDistributionPackage(file, getType());
        } else {
            return null;
        }
    }

    public boolean installPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionPackage distributionPackage) throws DistributionPackageReadingException {
        try {
            Input input = new Input(distributionPackage.createInputStream());
            ResourceSerializer resourceSerializer = (ResourceSerializer) kryo.getDefaultSerializer(Resource.class);
            resourceSerializer.setResourceResolver(resourceResolver);
            LinkedList<Resource> resources = (LinkedList<Resource>) kryo.readObject(input, LinkedList.class);
            input.close();
            for (Resource resource : resources) {
                persistResource(resourceResolver, resource);
            }
            resourceResolver.commit();
        } catch (Exception e) {
            throw new DistributionPackageReadingException(e);
        }
        return true;
    }

    private void persistResource(@Nonnull ResourceResolver resourceResolver, Resource resource) throws PersistenceException {
        String path = resource.getPath().trim();
        String name = path.substring(path.lastIndexOf('/') + 1);
        String substring = path.substring(0, path.lastIndexOf('/'));
        String parentPath = substring.length() == 0 ? "/" : substring;
        Resource existingResource = resourceResolver.getResource(path);
        if (existingResource != null) {
            resourceResolver.delete(existingResource);
        }
        Resource parent = resourceResolver.getResource(parentPath);
        if (parent == null) {
            parent = createParent(resourceResolver, parentPath);
        }
        Resource createdResource = resourceResolver.create(parent, name, resource.getValueMap());
        log.info("created resource {}", createdResource);
    }

    private Resource createParent(ResourceResolver resourceResolver, String path) throws PersistenceException {
        String parentPath = path.substring(0, path.lastIndexOf('/'));
        String name = path.substring(path.lastIndexOf('/') + 1);
        Resource parentResource = resourceResolver.getResource(parentPath);
        if (parentResource == null) {
            parentResource = createParent(resourceResolver, parentPath);
        }
        Map<String, Object> properties = new HashMap<String, Object>();
        return resourceResolver.create(parentResource, name, properties);
    }

    private class ResourceSerializer extends Serializer<Resource> {

        private ResourceResolver resourceResolver;

        @Override
        public void write(Kryo kryo, Output output, Resource resource) {
            ValueMap valueMap = resource.getValueMap();

            output.writeString(resource.getPath());
            output.writeString(resource.getResourceType());

            HashMap map = new HashMap<String, Object>();
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
            }

            kryo.writeObjectOrNull(output, map, HashMap.class);
        }

        @Override
        public Resource read(Kryo kryo, Input input, Class<Resource> type) {

            String path = input.readString();
            String resourceType = input.readString();

            final HashMap map = kryo.readObjectOrNull(input, HashMap.class);

            return new SyntheticResource(resourceResolver, path, resourceType){
                @Override
                public ValueMap getValueMap() {
                    return new ValueMapDecorator(map);
                }

            };
        }

        public void setResourceResolver(ResourceResolver resourceResolver) {
            this.resourceResolver = resourceResolver;
        }
    }

    private class ValueMapSerializer extends Serializer<ValueMap> {
        @Override
        public void write(Kryo kryo, Output output, ValueMap valueMap) {
            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                output.writeString(entry.getKey());
                output.writeString(entry.getValue().toString());
            }
        }

        @Override
        public ValueMap read(Kryo kryo, Input input, Class<ValueMap> type) {
            final Map<String, Object> map = new HashMap<String, Object>();

            String key;
            while ((key = input.readString()) != null) {
                String value = input.readString();
                map.put(key,value);
            }
            return new ValueMapDecorator(map);
        }
    }
}
