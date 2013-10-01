package org.apache.sling.jcr.resource.internal.helper;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Testcase for {@link JcrPropertyMapCacheEntry}
 */
public class JcrPropertyMapCacheEntryTest {

    @Test
    public void testByteArray() throws Exception {
        assertNotNull(new JcrPropertyMapCacheEntry(new Byte[0], null));
        assertNotNull(new JcrPropertyMapCacheEntry(new byte[0], null));
    }

    @Test
    public void testShortArray() throws Exception {
        assertNotNull(new JcrPropertyMapCacheEntry(new Short[0], null));
        assertNotNull(new JcrPropertyMapCacheEntry(new short[0], null));
    }

    @Test
    public void testIntArray() throws Exception {
        assertNotNull(new JcrPropertyMapCacheEntry(new Integer[0], null));
        assertNotNull(new JcrPropertyMapCacheEntry(new int[0], null));
    }

    @Test
    public void testLongArray() throws Exception {
        assertNotNull(new JcrPropertyMapCacheEntry(new Long[0], null));
        assertNotNull(new JcrPropertyMapCacheEntry(new long[0], null));
    }

    @Test
    public void testFloatArray() throws Exception {
        assertNotNull(new JcrPropertyMapCacheEntry(new Float[0], null));
        assertNotNull(new JcrPropertyMapCacheEntry(new float[0], null));
    }

    @Test
    public void testDoubleArray() throws Exception {
        assertNotNull(new JcrPropertyMapCacheEntry(new Double[0], null));
        assertNotNull(new JcrPropertyMapCacheEntry(new double[0], null));
    }

    @Test
    public void testBooleanArray() throws Exception {
        assertNotNull(new JcrPropertyMapCacheEntry(new Boolean[0], null));
        assertNotNull(new JcrPropertyMapCacheEntry(new boolean[0], null));
    }

    @Test
    public void testCharArray() throws Exception {
        assertNotNull(new JcrPropertyMapCacheEntry(new Character[0], null));
        assertNotNull(new JcrPropertyMapCacheEntry(new char[0], null));
    }

}
