package com.youu.mysql.protocol.net.storage;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import com.google.common.collect.Maps;
import com.youu.mysql.protocol.net.util.ConnectionId;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/24
 */
@Slf4j
public class H2StorageProvider implements StorageProvider {

    private static final Map<Integer, Statement> CONNECTION_MAP = Maps.newConcurrentMap();
    private static final String URL = "jdbc:h2:mem:%s;database_to_upper=false";

    @Override
    public void init(String schema) throws SQLException {
        org.h2.Driver.load();
        Statement statement = CONNECTION_MAP.get(ConnectionId.get());
        if (statement != null) {
            statement.close();
        }
        String jdbcUrl = (schema == null || "".equals(schema)) ? String.format(URL, schema) : String.format(URL, schema)
            + ";INIT=CREATE SCHEMA IF NOT EXISTS " + schema;
        CONNECTION_MAP.put(ConnectionId.get(), DriverManager.getConnection(jdbcUrl, "sa", "sa").createStatement());
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
