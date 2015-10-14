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
package org.apache.sling.distribution.serialization.impl.avro;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.util.Utf8;
import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.distribution.DistributionRequest;
import org.apache.sling.distribution.packaging.DistributionPackage;
import org.apache.sling.distribution.serialization.DistributionPackageBuilder;
import org.apache.sling.distribution.serialization.DistributionPackageBuildingException;
import org.apache.sling.distribution.serialization.DistributionPackageReadingException;
import org.apache.sling.distribution.serialization.impl.FileDistributionPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Avro based {@link DistributionPackageBuilder}
 */
public class AvroDistributionPackageBuilder implements DistributionPackageBuilder {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private DataFileWriter<AvroShallowResource> dataFileWriter;
    private Schema schema;

    public AvroDistributionPackageBuilder() {
        DatumWriter<AvroShallowResource> datumWriter = new SpecificDatumWriter<AvroShallowResource>(AvroShallowResource.class);
        this.dataFileWriter = new DataFileWriter<AvroShallowResource>(datumWriter);
        try {
            schema = new Schema.Parser().parse(getClass().getResourceAsStream("/shallowresource.avsc"));
        } catch (IOException e) {
            // do nothing
        }
    }

    @Override
    public String getType() {
        return AvroDistributionPackageBuilderFactory.RESOURCEAVRO;
    }

    @Override
    public DistributionPackage createPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionRequest request) throws DistributionPackageBuildingException {
        DistributionPackage distributionPackage;
        try {
            String path = request.getPaths()[0];
            Resource resource = resourceResolver.getResource(path);

            AvroShallowResource avroShallowResource = getAvroShallowResource(request, path, resource);

            File file = File.createTempFile("dp-" + System.nanoTime(), ".avro");
            dataFileWriter.create(schema, file);
            dataFileWriter.append(avroShallowResource);

            distributionPackage = new FileDistributionPackage(file, getType());
        } catch (Exception e) {
            throw new DistributionPackageBuildingException(e);
        } finally {
            try {
                dataFileWriter.close();
            } catch (IOException e) {
                // do nothing
            }
        }
        return distributionPackage;
    }

    private AvroShallowResource getAvroShallowResource(DistributionRequest request, String path, Resource resource) {
        AvroShallowResource avroShallowResource = new AvroShallowResource();
        avroShallowResource.setName("avro_" + System.nanoTime());
        avroShallowResource.setPath(path);
        avroShallowResource.setResourceType(resource.getResourceType());
        ValueMap valueMap = resource.getValueMap();
        Map<CharSequence, Object> map = new HashMap<CharSequence, Object>();
        for (Map.Entry<String, Object> entry : valueMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof GregorianCalendar) {
                value = new SimpleDateFormat().format(((GregorianCalendar) value).getTime());
            } else if (value instanceof Object[]) {
                Object[] ar = (Object[]) value;
                value = Arrays.asList(ar);
            }
            map.put(entry.getKey(), value);
        }
        avroShallowResource.setValueMap(map);
        List<AvroShallowResource> children = new LinkedList<AvroShallowResource>();
        if (request.isDeep(path)) {
            for (Resource child : resource.getChildren()) {
                String childPath = child.getPath();
                children.add(getAvroShallowResource(request, childPath, child));
            }
        }
        avroShallowResource.setChildren(children);
        return avroShallowResource;
    }

    @Override
    public DistributionPackage readPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull InputStream stream) throws DistributionPackageReadingException {
        try {
            File file = File.createTempFile("dp_" + System.nanoTime(), ".avro");
            OutputStream output = new FileOutputStream(file);
            int copied = IOUtils.copy(stream, output);

            log.debug("copied {} bytes", copied);

            return new FileDistributionPackage(file, getType());
        } catch (Exception e) {
            throw new DistributionPackageReadingException(e);
        }
    }

    private Collection<AvroShallowResource> readAvroResources(File file) throws IOException {
        DatumReader<AvroShallowResource> datumReader = new SpecificDatumReader<AvroShallowResource>(AvroShallowResource.class);
        DataFileReader<AvroShallowResource> dataFileReader = new DataFileReader<AvroShallowResource>(file, datumReader);
        AvroShallowResource avroResource = null;
        Collection<AvroShallowResource> avroResources = new LinkedList<AvroShallowResource>();
        while (dataFileReader.hasNext()) {
// Reuse avroResource object by passing it to next(). This saves us from
// allocating and garbage collecting many objects for files with
// many items.
            avroResource = dataFileReader.next(avroResource);
            avroResources.add(avroResource);
        }
        return avroResources;
    }

    @Override
    public DistributionPackage getPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull String id) {
        File file = new File(id);
        if (file.exists()) {
            return new FileDistributionPackage(file, getType());
        } else {
            return null;
        }
    }

    @Override
    public boolean installPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionPackage distributionPackage) throws DistributionPackageReadingException {
        if (distributionPackage instanceof FileDistributionPackage) {
            try {
                String filePath = distributionPackage.getId();
                File f = new File(filePath);
                Collection<AvroShallowResource> avroShallowResources = readAvroResources(f);
                for (AvroShallowResource r : avroShallowResources) {
                    persistResource(resourceResolver, r);
                }
                resourceResolver.commit();
            } catch (Exception e) {
                throw new DistributionPackageReadingException(e);
            }
            return true;
        } else {
            return false;
        }
    }

    private void persistResource(@Nonnull ResourceResolver resourceResolver, AvroShallowResource r) throws PersistenceException {
        String path = r.getPath().toString();
        String name = path.substring(path.lastIndexOf('/') + 1);
        String substring = path.substring(0, path.lastIndexOf('/'));
        String parentPath = substring.length() == 0 ? "/" : substring;
        Map<String, Object> map = new HashMap<String, Object>();
        Map<CharSequence, Object> valueMap = r.getValueMap();
        for (Map.Entry<CharSequence, Object> entry : valueMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof GenericData.Array) {
                GenericData.Array array = (GenericData.Array) value;
                String[] s = new String[array.size()];
                for (int i = 0; i < s.length; i++) {
                    Object gd = array.get(i);
                    s[i] = gd.toString();
                }
                value = s;
            }
            if (value instanceof Utf8) {
                value = value.toString();
            }
            map.put(entry.getKey().toString(), value);
        }
        Resource existingResource = resourceResolver.getResource(path);
        if (existingResource != null) {
            resourceResolver.delete(existingResource);
        }
        Resource parent = resourceResolver.getResource(parentPath);
        Resource createdResource = resourceResolver.create(parent, name, map);
        log.info("created resource {}", createdResource);
        for (AvroShallowResource child : r.getChildren()) {
            persistResource(createdResource.getResourceResolver(), child);
        }
    }

}
