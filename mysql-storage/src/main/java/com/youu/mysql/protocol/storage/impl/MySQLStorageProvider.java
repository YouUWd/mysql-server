package com.youu.mysql.protocol.storage.impl;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.youu.mysql.common.util.ConnectionId;
import com.youu.mysql.protocol.storage.StorageProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/24
 */
@Slf4j
public class MySQLStorageProvider implements StorageProvider {

    private static final Map<Integer, Statement> CONNECTION_MAP = Maps.newConcurrentMap();
    private static final String URL = "jdbc:mysql://localhost:33050/%s?useSSL=true";

    @Override
    public void init(String schema) throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            throw new SQLException("can not find com.mysql.cj.jdbc.Driver", "28000", 4444);
        }
        Statement statement = CONNECTION_MAP.get(ConnectionId.get());
        if (statement != null) {
            statement.close();
        }
        String jdbcUrl = String.format(URL, Strings.nullToEmpty(schema));
        CONNECTION_MAP.put(ConnectionId.get(), DriverManager.getConnection(jdbcUrl, "root", "pass").createStatement());
    }

    @Override
    public void execute(String sql) throws SQLException {
        CONNECTION_MAP.get(ConnectionId.get()).execute(sql);
    }

    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        ResultSet resultSet = CONNECTION_MAP.get(ConnectionId.get()).executeQuery(sql);
        return resultSet;
    }

    @Override
    public void release() throws SQLException {
        Statement statement = CONNECTION_MAP.get(ConnectionId.get());
        if (statement != null) {
            statement.close();
        }
        CONNECTION_MAP.remove(ConnectionId.get());
    }
}
