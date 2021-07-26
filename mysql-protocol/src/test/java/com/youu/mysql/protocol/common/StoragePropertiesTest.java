package com.youu.mysql.protocol.common;

import com.youu.mysql.storage.StorageConfig;
import org.junit.Assert;
import org.junit.Test;

public class StoragePropertiesTest {

    @Test
    public void testEquals() {
        Assert.assertEquals(new StorageProperties(
            StorageConfig.getConfig().getStore(1), null,
            null), new StorageProperties(
            StorageConfig.getConfig().getStore(1), null,
            null));

        Assert.assertNotEquals(new StorageProperties(
            StorageConfig.getConfig().getStore(1), null,
            null), new StorageProperties(
            StorageConfig.getConfig().getStore(2), null,
            null));
    }
}