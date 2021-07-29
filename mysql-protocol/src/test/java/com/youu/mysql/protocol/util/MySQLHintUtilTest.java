package com.youu.mysql.protocol.util;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

public class MySQLHintUtilTest {

    @Test
    public void getIndex() {
        Assert.assertEquals(Optional.of(1), MySQLHintUtil.getIndex("/*+ USE_STORE(1) */"));
        Assert.assertEquals(Optional.of(2), MySQLHintUtil.getIndex("/*+  USE_STORE(2)*/"));
        Assert.assertEquals(Optional.of(3), MySQLHintUtil.getIndex("/*+USE_STORE(3)  */"));
        Assert.assertEquals(Optional.of(4), MySQLHintUtil.getIndex("/*+USE_STORE(4)*/"));
        Assert.assertEquals(Optional.of(5), MySQLHintUtil.getIndex("/*+   USE_STORE(5)   */"));
        Assert.assertEquals(Optional.of(6), MySQLHintUtil.getIndex("/*+USE_STORE(6)*/select 1"));
    }
}