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
package org.apache.sling.i18n.impl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;

import org.apache.sling.api.SlingException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JcrResourceBundle extends ResourceBundle {

    private static final Logger log = LoggerFactory.getLogger(JcrResourceBundle.class);

    static final String JCR_PATH = "jcr:path";

    static final String PROP_KEY = "sling:key";

    static final String PROP_VALUE = "sling:message";

    static final String PROP_BASENAME = "sling:basename";

    static final String PROP_LANGUAGE = "jcr:language";

    private final Map<String, Object> resources;

    private final Locale locale;

    JcrResourceBundle(Locale locale, String baseName,
            ResourceResolver resourceResolver) {
        this.locale = locale;
        
        long start = System.currentTimeMillis();
        refreshSession(resourceResolver, true);
        final String loadQuery = getFullLoadQuery(locale, baseName);
        this.resources = loadFully(resourceResolver, loadQuery);
        long end = System.currentTimeMillis();
        log.debug(
            "JcrResourceBundle: Fully loaded {} entries for {} (base: {}) in {}ms",
            new Object[] { resources.size(), locale, baseName,
                (end - start) });
    }

    static void refreshSession(ResourceResolver resolver, boolean keepChanges) {
        final Session s = resolver.adaptTo(Session.class);
        if(s == null) {
            log.warn("ResourceResolver {} does not adapt to Session, cannot refresh", resolver);
        } else {
            try {
                s.refresh(keepChanges);
            } catch(RepositoryException re) {
                throw new RuntimeException("RepositoryException in session.refresh", re);
            }
        }
    }

    @Override
    protected void setParent(ResourceBundle parent) {
        super.setParent(parent);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns a Set of all resource keys provided by this resource bundle only.
     * <p>
     * This method is a new Java 1.6 method to implement the
     * ResourceBundle.keySet() method.
     *
     * @return The keys of the resources provided by this resource bundle
     */
    @Override
    protected Set<String> handleKeySet() {
        return resources.keySet();
    }

    @Override
    public Enumeration<String> getKeys() {
        Enumeration<String> parentKeys = (parent != null)
                ? parent.getKeys()
                : null;
        return new ResourceBundleEnumeration(resources.keySet(), parentKeys);
    }

    @Override
    protected Object handleGetObject(String key) {
        return resources.get(key);
    }

    @SuppressWarnings("deprecation")
    private Map<String, Object> loadFully(
            final ResourceResolver resourceResolver, final String fullLoadQuery) {
        log.debug("Executing full load query {}", fullLoadQuery);

        // do an XPath query because this won't go away soon and still
        // (2011/04/04) is the fastest query language ...
        Iterator<Map<String, Object>> bundles = null;
        try {
            bundles = resourceResolver.queryResources(fullLoadQuery, Query.XPATH);
        } catch (final SlingException se) {
            log.error("Exception during resource query " + fullLoadQuery, se);
        }

        final Map<String, Object> rest = new HashMap<String, Object>();
        if ( bundles != null ) {
            final String[] path = resourceResolver.getSearchPath();

            final List<Map<String, Object>> res0 = new ArrayList<Map<String, Object>>();
            for (int i = 0; i < path.length; i++) {
                res0.add(new HashMap<String, Object>());
            }

            while (bundles.hasNext()) {
                final Map<String, Object> row = bundles.next();
                final String jcrPath = (String) row.get(JCR_PATH);
                String key = (String) row.get(PROP_KEY);

                if (key == null) {
                    key = ResourceUtil.getName(jcrPath);
                }

                Map<String, Object> dst = rest;
                for (int i = 0; i < path.length; i++) {
                    if (jcrPath.startsWith(path[i])) {
                        dst = res0.get(i);
                        break;
                    }
                }

                dst.put(key, row.get(PROP_VALUE));
            }

            for (int i = path.length - 1; i >= 0; i--) {
                rest.putAll(res0.get(i));
            }
        }
        return rest;
    }

    private static String getFullLoadQuery(final Locale locale,
            final String baseName) {
        final StringBuilder buf = new StringBuilder(64);

        buf.append("//element(*,mix:language)[");

        final String localeString = locale.toString();
        buf.append("@jcr:language='").
            append(localeString).
            append('\'');
        final String localeStringLower = localeString.toLowerCase();
        if (!localeStringLower.equals(localeString)) {
            buf.append(" or @jcr:language='").
                append(localeStringLower).
                append('\'');
        }
        final String localeRFC4646String = toRFC4646String(locale);
        if (!localeRFC4646String.equals(localeString)) {
            buf.append(" or @jcr:language='").
                append(localeRFC4646String).
                append('\'');
            final String localeRFC4646StringLower = localeRFC4646String.toLowerCase();
            if (!localeRFC4646StringLower.equals(localeRFC4646String)) {
                buf.append(" or @jcr:language='").
                    append(localeRFC4646StringLower).
                    append('\'');
            }
        }

        if (baseName != null) {
            buf.append(" and @");
            buf.append(PROP_BASENAME);
            if (baseName.length() > 0) {
                buf.append("='").append(baseName).append('\'');
            }
        }

        buf.append("]//element(*,sling:Message)");
        buf.append("[@").append(PROP_VALUE).append("]/(@");
        buf.append(PROP_KEY).append("|@").append(PROP_VALUE).append(")");

        return buf.toString();
    }

    // Would be nice if Locale.toString() output RFC 4646, but it doesn't
    private static String toRFC4646String(Locale locale) {
        return locale.toString().replace('_', '-');
    }
}
