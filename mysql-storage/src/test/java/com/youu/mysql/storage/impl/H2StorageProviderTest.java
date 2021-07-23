package com.youu.mysql.storage.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class H2StorageProviderTest {
    H2StorageProvider provider = new H2StorageProvider();

    @Before
    public void init() throws SQLException {
        provider.init("d1");
    }

    @Test
    public void execute() throws SQLException {
        provider.execute("create schema d2");
    }

    @Test
    public void executeQuery() throws SQLException {
        ResultSet resultSet = provider.executeQuery("select 1");
        while (resultSet.next()) {
            int r = resultSet.getInt("1");
            Assert.assertEquals(1, r);
        }
    }

    @Test
    public void release() throws SQLException {
        provider.release();
    }
}