package com.youu.mysql.storage.util;

import java.sql.Types;

import com.mysql.cj.MysqlType;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/8/5
 */
public class ColumnTypeConverter {
    /**
     * @param h2Type h2 column type
     * @return MySQL对应的column type
     */
    public static int h22MySQL(int h2Type, String h2TypeName) {
        switch (h2Type) {
            case Types.BOOLEAN:
            case Types.TINYINT:
                return MysqlType.FIELD_TYPE_TINY;
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                return MysqlType.FIELD_TYPE_LONGLONG;
            case Types.VARCHAR:
                return MysqlType.FIELD_TYPE_VAR_STRING;
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                return MysqlType.FIELD_TYPE_TIMESTAMP;
            default:
                throw new RuntimeException(
                    "should convert h2 column(" + h2TypeName + ")  type (" + h2Type + ") to mysql column type");
        }
    }
}
