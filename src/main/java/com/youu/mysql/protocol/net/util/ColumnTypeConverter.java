package com.youu.mysql.protocol.net.util;

import com.youu.mysql.protocol.net.constant.MySQLColumnType;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/29
 */
public class ColumnTypeConverter {

    /**
     * @param h2Type h2 column type
     * @return MySQL对应的column type
     */
    public static MySQLColumnType h22MySQL(int h2Type, String h2TypeName) {
        switch (h2Type) {
            case 4://INT
                return MySQLColumnType.MYSQL_TYPE_INT24;
            case 12://TIMESTAMP
                return MySQLColumnType.MYSQL_TYPE_VARCHAR;
            case 93://VARCHAR
                return MySQLColumnType.MYSQL_TYPE_TIMESTAMP;
            default:
                throw new RuntimeException(
                    "should convert h2 column(" + h2TypeName + ")  type (" + h2Type + ") to mysql column type");
        }
    }
}
