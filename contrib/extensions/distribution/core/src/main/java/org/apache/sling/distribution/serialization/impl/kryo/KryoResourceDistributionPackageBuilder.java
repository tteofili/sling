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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.apache.sling.api.resource.SyntheticResource;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.wrappers.ValueMapDecorator;
import org.apache.sling.distribution.DistributionRequest;
import org.apache.sling.distribution.packaging.DistributionPackage;
import org.apache.sling.distribution.serialization.DistributionPackageBuilder;
import org.apache.sling.distribution.serialization.DistributionPackageBuildingException;
import org.apache.sling.distribution.serialization.DistributionPackageReadingException;
import org.apache.sling.distribution.serialization.impl.FileDistributionPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

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
        kryo.register(Resource.class, new ResourceSerializer());
    }

    public DistributionPackage createPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionRequest request) throws DistributionPackageBuildingException {
        DistributionPackage distributionPackage = null;
        try {
            String[] paths = request.getPaths();
            File file = File.createTempFile("rp-kryo-" + System.nanoTime(), ".bin");
            Output output = new Output(new FileOutputStream(file));
            ArrayList<Resource> resources = new ArrayList<Resource>();
            for (String p : paths) {
                Resource resource = resourceResolver.getResource(p);
                if (resource != null) {
                    resources.add(resource);
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
            File file = File.createTempFile("rp-kryo-" + System.nanoTime(), ".bin");
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
            ArrayList<Resource> resources = (ArrayList<Resource>) kryo.readObject(input, ArrayList.class);
            input.close();
            for (Resource r : resources) {
                String path = r.getPath();
                String parent = path.substring(0, path.lastIndexOf('/'));
                Resource createdResource = resourceResolver.create(resourceResolver.getResource(parent), path, r.adaptTo(ValueMap.class));
                log.info("installed resource {}", createdResource);
            }
            resourceResolver.commit();
        } catch (Exception e) {
            throw new DistributionPackageReadingException(e);
        }
        return true;
    }

    private class ResourceSerializer extends Serializer<Resource> {

        @Override
        public void write(Kryo kryo, Output output, Resource resource) {
            ValueMap valueMap = resource.getValueMap();

            output.writeInt(valueMap.size());
            output.writeString(resource.getPath());
            output.writeString(resource.getResourceType());

            for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
                output.writeString(entry.getKey());
                output.writeString(entry.getValue().toString());
            }
        }

        @Override
        public Resource read(Kryo kryo, Input input, Class<Resource> type) {
            int len = input.readInt(true);
            final Map<String, Object> map = new HashMap<String, Object>();

            String path = input.readString();
            String resourceType = input.readString();

            for (int i = 0; i < len; i++) {
                String key = input.readString();
                String value = input.readString();
                map.put(key,value);
            }

            return new SyntheticResource(null, path, resourceType){

                @Override
                public ValueMap getValueMap() {
                    return new ValueMapDecorator(map);
                }
            };
        }
    }

}
