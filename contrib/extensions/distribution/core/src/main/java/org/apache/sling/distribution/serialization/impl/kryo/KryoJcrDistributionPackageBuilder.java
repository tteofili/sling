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
import javax.annotation.Nonnull;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.distribution.DistributionRequest;
import org.apache.sling.distribution.packaging.DistributionPackage;
import org.apache.sling.distribution.serialization.DistributionPackageBuildingException;
import org.apache.sling.distribution.serialization.DistributionPackageReadingException;
import org.apache.sling.distribution.serialization.impl.AbstractDistributionPackageBuilder;
import org.apache.sling.distribution.serialization.impl.FileDistributionPackage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * A {@link org.apache.sling.distribution.serialization.DistributionPackageBuilder} based on {@link Kryo} (de)serializer for JCR.
 */
public class KryoJcrDistributionPackageBuilder extends AbstractDistributionPackageBuilder {

    private final Kryo kryo = new Kryo();

    private final Logger log = LoggerFactory.getLogger(getClass());

    public KryoJcrDistributionPackageBuilder(String type) {
        super(type);
        kryo.register(Node.class);
        kryo.register(Property.class);
    }

    @Override
    protected DistributionPackage createPackageForAdd(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionRequest request) throws DistributionPackageBuildingException {
        DistributionPackage distributionPackage = null;
        try {
            Session session = getSession(resourceResolver);

            String[] paths = request.getPaths();
            File file = File.createTempFile("rp-kryo-" + System.nanoTime(), ".bin");
            Output output = new Output(new FileOutputStream(file));
            Node[] nodes = new Node[paths.length];
            int i = 0;
            for (String p : paths) {
                if (session.nodeExists(p)) {
                    nodes[i] = session.getNode(p);
                } else {
                    nodes[i] = null;
                }
                i++;
            }
            kryo.writeObject(output, nodes);
            output.close();
            distributionPackage = new FileDistributionPackage(file, getType());

        } catch (Exception e) {
            throw new DistributionPackageBuildingException(e);
        }
        return distributionPackage;
    }

    @Override
    protected DistributionPackage readPackageInternal(@Nonnull ResourceResolver resourceResolver, @Nonnull InputStream stream) throws DistributionPackageReadingException {

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

    private void addNode(Node parent, Node n) throws RepositoryException {
        Node node = parent.addNode(n.getName(), n.getPrimaryNodeType().getName());
        for (NodeType nt : n.getMixinNodeTypes()) {
            if (node.canAddMixin(nt.getName())) {
                node.addMixin(nt.getName());
            }
        }
        for (PropertyIterator it = n.getProperties(); it.hasNext(); ) {
            Property property = it.nextProperty();
            if (property.isMultiple()) {
                node.setProperty(property.getName(), property.getValues());
            } else {
                node.setProperty(property.getName(), property.getValue());
            }
        }
        if (n.hasNodes()) {
            for (NodeIterator nit = n.getNodes(); nit.hasNext(); ) {
                addNode(node, nit.nextNode());
            }
        }
    }

    @Override
    protected boolean installPackageInternal(@Nonnull ResourceResolver resourceResolver, @Nonnull DistributionPackage distributionPackage) throws DistributionPackageReadingException {
        try {
            Session session = getSession(resourceResolver);
            Input input = new Input(distributionPackage.createInputStream());
            Node[] nodes = kryo.readObject(input, Node[].class);
            input.close();
            for (Node n : nodes) {
                if (n != null) {
                    String path = n.getPath();
                    if (session.nodeExists(path)) {
                        session.removeItem(path);
                    }
                    Node parent = session.getNode(path.substring(0, path.lastIndexOf('/')));
                    addNode(parent, n);
                }
            }
            session.save();
        } catch (Exception e) {
            throw new DistributionPackageReadingException(e);

        }
        return true;
    }

    @Override
    protected DistributionPackage getPackageInternal(@Nonnull ResourceResolver resourceResolver, @Nonnull String id) {
        File file = new File(id);
        if (file.exists()) {
            return new FileDistributionPackage(file, getType());
        } else {
            return null;
        }
    }
}
