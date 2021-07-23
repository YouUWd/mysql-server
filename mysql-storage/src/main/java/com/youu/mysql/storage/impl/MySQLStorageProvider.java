package com.youu.mysql.storage.impl;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import com.google.common.collect.Maps;
import com.youu.mysql.common.util.ConnectionId;
import com.youu.mysql.storage.StorageProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/24
 */
@Slf4j
public class MySQLStorageProvider implements StorageProvider {

    private static final Map<Integer, Statement> CONNECTION_MAP = Maps.newConcurrentMap();
    private String url;
    private String username, password;

    public MySQLStorageProvider(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

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
        int ds = url.indexOf("/", 13);
        int de = url.indexOf("?", ds);
        de = de > 0 ? de : url.length();
        String jdbcUrl = url;
        if (ds > 0) {
            jdbcUrl = url.replaceFirst(url.substring(ds, de), "/" + schema);
        }
        CONNECTION_MAP.put(ConnectionId.get(),
            DriverManager.getConnection(jdbcUrl, username, password).createStatement());
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
