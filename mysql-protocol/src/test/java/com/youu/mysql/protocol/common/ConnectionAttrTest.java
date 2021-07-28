package com.youu.mysql.protocol.common;

import org.junit.Assert;
import org.junit.Test;

public class ConnectionAttrTest {

    @Test
    public void test() {
        ConnectionAttr build = ConnectionAttr.builder()
            .connectionId(1)
            .clientCharset(2)
            .schema("test")
            .build();

        Assert.assertEquals(1, build.getConnectionId());
        Assert.assertEquals(2, build.getClientCharset());
        Assert.assertEquals("test", build.getSchema());

        build.setConnectionId(10);
        build.setClientCharset(20);
        build.setSchema("d1");
        Assert.assertEquals(10, build.getConnectionId());
        Assert.assertEquals(20, build.getClientCharset());
        Assert.assertEquals("d1", build.getSchema());
    }
}