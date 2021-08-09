package com.youu.mysql.common.util;

import java.sql.Types;

import com.mysql.cj.MysqlType;
import com.youu.mysql.storage.util.ColumnTypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class ColumnTypeConverterTest {

    @Test
    public void h22MySQL() {

        Assert.assertEquals(MysqlType.FIELD_TYPE_TINY, ColumnTypeConverter.h22MySQL(Types.BOOLEAN, "Types.BOOLEAN"));
        Assert.assertEquals(MysqlType.FIELD_TYPE_TINY, ColumnTypeConverter.h22MySQL(Types.TINYINT, "Types.TINYINT"));

        Assert.assertEquals(MysqlType.FIELD_TYPE_LONGLONG,
            ColumnTypeConverter.h22MySQL(Types.SMALLINT, "Types.SMALLINT"));
        Assert.assertEquals(MysqlType.FIELD_TYPE_LONGLONG,
            ColumnTypeConverter.h22MySQL(Types.INTEGER, "Types.INTEGER"));
        Assert.assertEquals(MysqlType.FIELD_TYPE_LONGLONG, ColumnTypeConverter.h22MySQL(Types.BIGINT, "Types.BIGINT"));

        Assert.assertEquals(MysqlType.FIELD_TYPE_VAR_STRING,
            ColumnTypeConverter.h22MySQL(Types.VARCHAR, "Types.VARCHAR"));

        Assert.assertEquals(MysqlType.FIELD_TYPE_TIMESTAMP,
            ColumnTypeConverter.h22MySQL(Types.TIMESTAMP, "Types.TIMESTAMP"));
        Assert.assertEquals(MysqlType.FIELD_TYPE_TIMESTAMP,
            ColumnTypeConverter.h22MySQL(Types.TIMESTAMP_WITH_TIMEZONE, "Types.TIMESTAMP_WITH_TIMEZONE"));

    }

    @Test(expected = RuntimeException.class)
    public void h22MySQLException() {
        ColumnTypeConverter.h22MySQL(Types.REAL, "Types.REAL");
    }
}