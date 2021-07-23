package com.youu.mysql.storage;

import java.util.List;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.youu.mysql.storage.StorageConfig.HostPort;
import org.junit.Assert;
import org.junit.Test;

public class StorageConfigTest {
    @Test
    public void test() {
        Assert.assertSame(StorageConfig.getConfig(), StorageConfig.getConfig());
        Assert.assertEquals(new HostPort("127.0.0.1", 3306), StorageConfig.getConfig().getSchema());
        Assert.assertEquals("root", StorageConfig.getConfig().getUsername());
        Assert.assertEquals("pass", StorageConfig.getConfig().getPassword());
        Assert.assertEquals(ImmutableMap.of("root", "pass"), StorageConfig.getConfig().getUserPass());
        List<HostPort> hps = Lists.newArrayList(new HostPort("127.0.0.1", 33050), new HostPort("127.0.0.1", 33060));
        Assert.assertEquals(hps, StorageConfig.getConfig().getStorages());
    }

}