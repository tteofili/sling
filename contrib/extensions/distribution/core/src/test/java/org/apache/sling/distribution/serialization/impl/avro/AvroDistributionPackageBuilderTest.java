package org.apache.sling.distribution.serialization.impl.avro;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.distribution.DistributionRequest;
import org.apache.sling.distribution.DistributionRequestType;
import org.apache.sling.distribution.SimpleDistributionRequest;
import org.apache.sling.distribution.packaging.DistributionPackage;
import org.apache.sling.distribution.serialization.impl.FileDistributionPackage;
import org.apache.sling.distribution.serialization.impl.kryo.KryoResourceDistributionPackageBuilder;
import org.apache.sling.testing.resourceresolver.MockHelper;
import org.apache.sling.testing.resourceresolver.MockResourceResolverFactory;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link AvroDistributionPackageBuilder}
 */
public class AvroDistributionPackageBuilderTest {

    private MockHelper helper;
    private ResourceResolver resourceResolver;

    @Before
    public void setUp() throws Exception {
        resourceResolver = new MockResourceResolverFactory().getResourceResolver(null);
        helper = MockHelper.create(resourceResolver).resource("/libs").p("prop", "value")
                .resource("sub").p("sub", "hello")
                .resource(".sameLevel")
                .resource("/apps").p("foo", "baa");
        helper.commit();
    }

    @Test
    public void testCreatePackage() throws Exception {
        AvroDistributionPackageBuilder avroDistributionPackageBuilder = new AvroDistributionPackageBuilder();
        DistributionRequest request = new SimpleDistributionRequest(DistributionRequestType.ADD, "/libs");
        DistributionPackage avroPackage = avroDistributionPackageBuilder.createPackage(resourceResolver, request);
        assertNotNull(avroPackage);
    }

    @Test
    public void testReadPackage() throws Exception {
        AvroDistributionPackageBuilder avroDistributionPackageBuilder = new AvroDistributionPackageBuilder();
        InputStream stream = getClass().getResourceAsStream("/avro/dp.avro");
        DistributionPackage distributionPackage = avroDistributionPackageBuilder.readPackage(resourceResolver, stream);
        assertNotNull(distributionPackage);
    }

    @Test
    public void testGetPackage() throws Exception {
        AvroDistributionPackageBuilder avroDistributionPackageBuilder = new AvroDistributionPackageBuilder();
        DistributionPackage aPackage = avroDistributionPackageBuilder.getPackage(resourceResolver, getClass().getResource("/avro/dp.avro").getFile());
        assertNotNull(aPackage);
    }

    @Test
    public void testInstallPackage() throws Exception {
        AvroDistributionPackageBuilder avroDistributionPackageBuilder = new AvroDistributionPackageBuilder();
        File f = new File(getClass().getResource("/avro/dp.avro").getFile());
        DistributionPackage distributionPackage = new FileDistributionPackage(f, "avro");
        boolean success = avroDistributionPackageBuilder.installPackage(resourceResolver, distributionPackage);
        assertTrue(success);
    }

    @Test
    public void testPackageLifecycle() throws Exception {
        AvroDistributionPackageBuilder avroDistributionPackageBuilder = new AvroDistributionPackageBuilder();
        DistributionPackage distributionPackage = avroDistributionPackageBuilder.createPackage(resourceResolver, new DistributionRequest() {
            @Nonnull
            public DistributionRequestType getRequestType() {
                return DistributionRequestType.ADD;
            }

            public String[] getPaths() {
                return new String[]{"/libs"};
            }

            public boolean isDeep(String path) {
                return false;
            }
        });
        assertNotNull(distributionPackage);
        DistributionPackage readPackage = avroDistributionPackageBuilder.readPackage(resourceResolver, distributionPackage.createInputStream());
        assertTrue(avroDistributionPackageBuilder.installPackage(resourceResolver, readPackage));
    }
}