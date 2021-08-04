package com.youu.mysql.common.util;

import org.junit.Assert;
import org.junit.Test;

public class H2MySQLConverterTest {

    @Test
    public void convertMySQL() {

        Assert.assertEquals("create schema d1",
            H2MySQLConverter.convertMySQL("create database d1"));

        Assert.assertEquals("/*+ NOCACHE */create schema d1",
            H2MySQLConverter.convertMySQL("/*+ NOCACHE */create database d1"));

        Assert.assertEquals("CREATE /*+ NOCACHE */SCHEMA d1",
            H2MySQLConverter.convertMySQL("CREATE /*+ NOCACHE */DATABASE d1"));
    }
}