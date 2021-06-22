package com.youu.mysql.protocol.net.pkg;

public interface ComId {
    byte COM_SLEEP = 0x00;
    byte COM_QUIT = 0x01;
    byte COM_INIT_DB = 0x02;
    byte COM_QUERY = 0x03;
    byte COM_FIELD_LIST = 0x04;
    byte COM_CREATE_DB = 0x05;
    byte COM_DROP_DB = 0x06;
    byte COM_REFRESH = 0x07;
    byte COM_SHUTDOWN = 0x08;
    byte COM_STATISTICS = 0x09;
    byte COM_PROCESS_INFO = 0x0a;
    byte COM_CONNECT = 0x0b;
    byte COM_PROCESS_KILL = 0x0c;
    byte COM_DEBUG = 0x0d;
    byte COM_PING = 0x0e;
    byte COM_TIME = 0x0f;
    byte COM_DELAYED_INSERT = 0x10;
    byte COM_CHANGE_USER = 0x11;
    byte COM_BINLOG_DUMP = 0x12;
    byte COM_TABLE_DUMP = 0x13;
    byte COM_CONNECT_OUT = 0x14;
    byte COM_REGISTER_SLAVE = 0x15;
    byte COM_STMT_PREPARE = 0x16;
    byte COM_STMT_EXECUTE = 0x17;
    byte COM_STMT_SEND_LONG_DATA = 0x18;
    byte COM_STMT_CLOSE = 0x19;
    byte COM_STMT_RESET = 0x1a;
    byte COM_SET_OPTION = 0x1b;
    byte COM_STMT_FETCH = 0x1c;
    byte COM_DAEMON = 0x1d;
    byte COM_BINLOG_DUMP_GTID = 0x1e;
    byte COM_RESET_CONNECTION = 0x1f;
}
