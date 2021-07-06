package com.youu.mysql.protocol.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface StorageProvider {

    void init(String schema) throws SQLException;

    void execute(String sql) throws SQLException;

    ResultSet executeQuery(String sql) throws SQLException;

    void release() throws SQLException;
}
