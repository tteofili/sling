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
import java.io.InputStream;
import java.util.Map;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.PropertyOption;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.vault.fs.api.ImportMode;
import org.apache.jackrabbit.vault.fs.io.AccessControlHandling;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.apache.sling.distribution.DistributionRequest;
import org.apache.sling.distribution.component.impl.DistributionComponentConstants;
import org.apache.sling.distribution.component.impl.SettingsUtils;
import org.apache.sling.distribution.packaging.DistributionPackage;
import org.apache.sling.distribution.serialization.DistributionPackageBuilder;
import org.apache.sling.distribution.serialization.DistributionPackageBuildingException;
import org.apache.sling.distribution.serialization.DistributionPackageReadingException;
import org.apache.sling.distribution.serialization.impl.ResourceSharedDistributionPackageBuilder;
import org.apache.sling.distribution.serialization.impl.vlt.FileVaultDistributionPackageBuilder;
import org.apache.sling.distribution.serialization.impl.vlt.JcrVaultDistributionPackageBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory for {@link DistributionPackageBuilder}s based on Apache Avro.
 */
@Component(metatype = true,
        label = "Apache Sling Distribution Packaging - Avro Package Builder Factory",
        description = "OSGi configuration for Avro package builders",
        configurationFactory = true,
        specVersion = "1.1",
        policy = ConfigurationPolicy.REQUIRE
)
@Service(DistributionPackageBuilder.class)
public class AvroDistributionPackageBuilderFactory implements DistributionPackageBuilder {

    /**
     * name of this package builder.
     */
    @Property(label = "Name", description = "The name of the package builder.")
    public static final String NAME = DistributionComponentConstants.PN_NAME;

    public static final String JCRAVRO = "jcravro";

    public static final String RESOURCEAVRO = "resourceavro";

    /**
     * type of this package builder.
     */
    @Property(options = {
            @PropertyOption(name = JCRAVRO,
                    value = "Avro JCR packages"
            ),
            @PropertyOption(name = RESOURCEAVRO,
                    value = "Avro Resource packages"
            )},
            value = RESOURCEAVRO, label = "type", description = "The type of this package builder")
    public static final String TYPE = DistributionComponentConstants.PN_TYPE;

    /**
     * Temp file folder
     */
    @Property(label = "Temp Filesystem Folder", description = "The filesystem folder where the temporary files should be saved.")
    public static final String TEMP_FS_FOLDER = "tempFsFolder";

    private DistributionPackageBuilder packageBuilder;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    public void activate(Map<String, Object> config) {

        String type = PropertiesUtil.toString(config.get(TYPE), null);
        log.info("starting avro package builder of type {}", type);

        String name = PropertiesUtil.toString(config.get(NAME), null);
        String tempFsFolder = SettingsUtils.removeEmptyEntry(PropertiesUtil.toString(config.get(TEMP_FS_FOLDER), null));

        if (RESOURCEAVRO.equals(type)) {
            packageBuilder = new ResourceSharedDistributionPackageBuilder(new AvroDistributionPackageBuilder());
            log.info("started avro resource package builder");
        } else {
            throw new RuntimeException(type + " package builder not supported with Avro");
        }
    }

    public String getType() {
        return packageBuilder.getType();
    }

    public DistributionPackage createPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionRequest request) throws DistributionPackageBuildingException {
        return packageBuilder.createPackage(resourceResolver, request);
    }

    public DistributionPackage readPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull InputStream stream) throws DistributionPackageReadingException {
        return packageBuilder.readPackage(resourceResolver, stream);
    }

    public DistributionPackage getPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull String id) {
        return packageBuilder.getPackage(resourceResolver, id);
    }

    public boolean installPackage(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionPackage distributionPackage) throws DistributionPackageReadingException {
        return packageBuilder.installPackage(resourceResolver, distributionPackage);
    }
}
