package com.youu.mysql.protocol.net.pkg;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/6/25
 */
public interface CapabilityFlag {
    /**
     * Use the improved version of Old Password Authentication.
     * Not used.
     *
     * @note Assumed to be set since 4.1.1.
     */
    int CLIENT_LONG_PASSWORD = 1;
    /**
     * Send found rows instead of affected rows in @ref
     * page_protocol_basic_eof_packet
     */
    int CLIENT_FOUND_ROWS = 2;
    /**
     * @brief Get all column flags
     * Longer flags in Protocol::ColumnDefinition320.
     * @todo Reference Protocol::ColumnDefinition320
     * Server
     * ------
     * Supports longer flags.
     * Client
     * ------
     * Expects longer flags.
     */
    int CLIENT_LONG_FLAG = 4;
    /**
     * Database (schema) name can be specified on connect in Handshake Response
     * Packet.
     *
     * @todo Reference Handshake Response Packet.
     * Server
     * ------
     * Supports schema-name in Handshake Response Packet.
     * Client
     * ------
     * Handshake Response Packet contains a schema-name.
     * @sa send_client_reply_packet()
     */
    int CLIENT_CONNECT_WITH_DB = 8;
    int CLIENT_NO_SCHEMA = 16; /**< Don't allow database.table.column */
    /**
     * Compression protocol supported.
     *
     * @todo Reference Compression
     * Server
     * ------
     * Supports compression.
     * Client
     * ------
     * Switches to Compression compressed protocol after successful authentication.
     */
    int CLIENT_COMPRESS = 32;
    /**
     * Special handling of ODBC behavior.
     *
     * @note No special behavior since 3.22.
     */
    int CLIENT_ODBC = 64;
    /**
     * Can use LOAD DATA LOCAL.
     * Server
     * ------
     * Enables the LOCAL INFILE request of LOAD DATA|XML.
     * Client
     * ------
     * Will handle LOCAL INFILE request.
     */
    int CLIENT_LOCAL_FILES = 128;
    /**
     * Ignore spaces before '('
     * Server
     * ------
     * Parser can ignore spaces before '('.
     * Client
     * ------
     * Let the parser ignore spaces before '('.
     */
    int CLIENT_IGNORE_SPACE = 256;
    /**
     * New 4.1 protocol
     *
     * @todo Reference the new 4.1 protocol
     * Server
     * ------
     * Supports the 4.1 protocol.
     * Client
     * ------
     * Uses the 4.1 protocol.
     * @note this value was CLIENT_CHANGE_USER in 3.22, unused in 4.0
     */
    int CLIENT_PROTOCOL_41 = 512;
    /**
     * This is an interactive client
     * Use @ref System_variables::net_wait_timeout
     * versus @ref System_variables::net_interactive_timeout.
     * Server
     * ------
     * Supports interactive and noninteractive clients.
     * Client
     * ------
     * Client is interactive.
     *
     * @sa mysql_real_connect()
     */
    int CLIENT_INTERACTIVE = 1024;
    /**
     * Use SSL encryption for the session
     *
     * @todo Reference SSL
     * Server
     * ------
     * Supports SSL
     * Client
     * ------
     * Switch to SSL after sending the capability-flags.
     */
    int CLIENT_SSL = 2048;
    /**
     * Client only flag. Not used.
     * Client
     * ------
     * Do not issue SIGPIPE if network failures occur (libmysqlclient only).
     *
     * @sa mysql_real_connect()
     */
    int CLIENT_IGNORE_SIGPIPE = 4096;
    /**
     * Client knows about transactions
     * Server
     * ------
     * Can send status flags in @ref page_protocol_basic_ok_packet /
     *
     * @ref page_protocol_basic_eof_packet.
     * Client
     * ------
     * Expects status flags in @ref page_protocol_basic_ok_packet /
     * @ref page_protocol_basic_eof_packet.
     * @note This flag is optional in 3.23, but always set by the server since 4.0.
     * @sa send_server_handshake_packet(), parse_client_handshake_packet(),
     * net_send_ok(), net_send_eof()
     */
    int CLIENT_TRANSACTIONS = 8192;
    int CLIENT_RESERVED = 16384;
    /**
     * < DEPRECATED: Old flag for 4.1 protocol
     */
    int CLIENT_RESERVED2 = 32768; /**< DEPRECATED: Old flag for 4.1 authentication \
     CLIENT_SECURE_CONNECTION */
    /**
     * Enable/disable multi-stmt support
     * Also sets @ref CLIENT_MULTI_RESULTS. Currently not checked anywhere.
     * Server
     * ------
     * Can handle multiple statements per COM_QUERY and COM_STMT_PREPARE.
     * Client
     * -------
     * May send multiple statements per COM_QUERY and COM_STMT_PREPARE.
     *
     * @note Was named ::CLIENT_MULTI_QUERIES in 4.1.0, renamed later.
     * Requires
     * --------
     * ::CLIENT_PROTOCOL_41
     * @todo Reference COM_QUERY and COM_STMT_PREPARE
     */
    int CLIENT_MULTI_STATEMENTS = (1 << 16);

    /**
     * Enable/disable multi-results
     * Server
     * ------
     * Can send multiple resultsets for COM_QUERY.
     * Error if the server needs to send them and client
     * does not support them.
     * Client
     * -------
     * Can handle multiple resultsets for COM_QUERY.
     * Requires
     * --------
     * ::CLIENT_PROTOCOL_41
     *
     * @sa mysql_execute_command(), sp_head::MULTI_RESULTS
     */
    int CLIENT_MULTI_RESULTS = (1 << 17);

    /**
     * Multi-results and OUT parameters in PS-protocol.
     * Server
     * ------
     * Can send multiple resultsets for COM_STMT_EXECUTE.
     * Client
     * ------
     * Can handle multiple resultsets for COM_STMT_EXECUTE.
     * Requires
     * --------
     * ::CLIENT_PROTOCOL_41
     *
     * @todo Reference COM_STMT_EXECUTE and PS-protocol
     * @sa Protocol_binary::send_out_parameters
     */
    int CLIENT_PS_MULTI_RESULTS = (1 << 18);

    /**
     * Client supports plugin authentication
     * Server
     * ------
     * Sends extra data in Initial Handshake Packet and supports the pluggable
     * authentication protocol.
     * Client
     * ------
     * Supports authentication plugins.
     * Requires
     * --------
     * ::CLIENT_PROTOCOL_41
     *
     * @todo Reference plugin authentication, Initial Handshake Packet,
     * Authentication plugins
     * @sa send_change_user_packet(), send_client_reply_packet(), run_plugin_auth(),
     * parse_com_change_user_packet(), parse_client_handshake_packet()
     */
    int CLIENT_PLUGIN_AUTH = (1 << 19);

    /**
     * Client supports connection attributes
     * Server
     * ------
     * Permits connection attributes in Protocol::HandshakeResponse41.
     * Client
     * ------
     * Sends connection attributes in Protocol::HandshakeResponse41.
     *
     * @todo Reference Protocol::HandshakeResponse41
     * @sa send_client_connect_attrs(), read_client_connect_attrs()
     */
    int CLIENT_CONNECT_ATTRS = (1 << 20);

    /**
     * Enable authentication response packet to be larger than 255 bytes.
     * When the ability to change default plugin require that the initial password
     * field in the Protocol::HandshakeResponse41 paclet can be of arbitrary size.
     * However, the 4.1 client-server protocol limits the length of the
     * auth-data-field sent from client to server to 255 bytes.
     * The solution is to change the type of the field to a true length encoded
     * string and indicate the protocol change
     * with this client capability flag.
     * Server
     * ------
     * Understands length-encoded integer for auth response data in
     * Protocol::HandshakeResponse41.
     * Client
     * ------
     * Length of auth response data in Protocol::HandshakeResponse41
     * is a length-encoded integer.
     *
     * @todo Reference Protocol::HandshakeResponse41
     * @note The flag was introduced in 5.6.6, but had the wrong value.
     * @sa send_client_reply_packet(), parse_client_handshake_packet(),
     * get_56_lenc_string(), get_41_lenc_string()
     */
    int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = (1 << 21);

    /**
     * Don't close the connection for a user account with expired password.
     * Server
     * ------
     * Announces support for expired password extension.
     * Client
     * ------
     * Can handle expired passwords.
     *
     * @todo Reference expired password
     * @sa MYSQL_OPT_CAN_HANDLE_EXPIRED_PASSWORDS, disconnect_on_expired_password
     * ACL_USER::password_expired, check_password_lifetime(), acl_authenticate()
     */
    int CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS = (1 << 22);

    /**
     * Capable of handling server state change information. Its a hint to the
     * server to include the state change information in
     *
     * @ref page_protocol_basic_ok_packet.
     * Server
     * ------
     * Can set ::SERVER_SESSION_STATE_CHANGED in the ::SERVER_STATUS_flags_enum
     * and send @ref sect_protocol_basic_ok_packet_sessinfo in a
     * @ref page_protocol_basic_ok_packet.
     * Client
     * ------
     * Expects the server to send @ref sect_protocol_basic_ok_packet_sessinfo in
     * a @ref page_protocol_basic_ok_packet.
     * @sa enum_session_state_type, read_ok_ex(), net_send_ok(), Session_tracker,
     * State_tracker
     */
    int CLIENT_SESSION_TRACK = (1 << 23);

    /**
     * Client no longer needs @ref page_protocol_basic_eof_packet and will
     * use @ref page_protocol_basic_ok_packet instead.
     *
     * @sa net_send_ok()
     * Server
     * ------
     * Can send OK after a Text Resultset.
     * Client
     * ------
     * Expects an @ref page_protocol_basic_ok_packet (instead of
     * @ref page_protocol_basic_eof_packet) after the resultset rows of a
     * Text Resultset.
     * Background
     * ----------
     * To support ::CLIENT_SESSION_TRACK, additional information must be sent after
     * all successful commands. Although the @ref page_protocol_basic_ok_packet is
     * extensible, the @ref page_protocol_basic_eof_packet is not due to the overlap
     * of its bytes with the content of the Text Resultset Row.
     * Therefore, the @ref page_protocol_basic_eof_packet in the
     * Text Resultset is replaced with an @ref page_protocol_basic_ok_packet.
     * @ref page_protocol_basic_eof_packet is deprecated as of MySQL 5.7.5.
     * @todo Reference Text Resultset
     * @sa cli_safe_read_with_ok(), read_ok_ex(), net_send_ok(), net_send_eof()
     */
    int CLIENT_DEPRECATE_EOF = (1 << 24);

    /**
     * The client can handle optional metadata information in the resultset.
     */
    int CLIENT_OPTIONAL_RESULTSET_METADATA = (1 << 25);

    /**
     * Compression protocol extended to support zstd compression method
     * This capability flag is used to send zstd compression level between
     * client and server provided both client and server are enabled with
     * this flag.
     * Server
     * ------
     * Server sets this flag when global variable protocol-compression-algorithms
     * has zstd in its list of supported values.
     * Client
     * ------
     * Client sets this flag when it is configured to use zstd compression method.
     */
    int CLIENT_ZSTD_COMPRESSION_ALGORITHM = (1 << 26);

    /**
     * Support optional extension for query parameters into the @ref
     * page_protocol_com_query and @ref page_protocol_com_stmt_execute packets.
     * Server
     * ------
     * Expects an optional part containing the query parameter set(s). Executes the
     * query for each set of parameters or returns an error if more than 1 set of
     * parameters is sent and the server can't execute it.
     * Client
     * ------
     * Can send the optional part containing the query parameter set(s).
     */
    int CLIENT_QUERY_ATTRIBUTES = (1 << 27);

    /**
     * This flag will be reserved to extend the 32bit capabilities structure to
     * 64bits.
     */
    int CLIENT_CAPABILITY_EXTENSION = (1 << 29);

    /**
     * Verify server certificate.
     * Client only flag.
     *
     * @deprecated in favor of --ssl-mode.
     */
    int CLIENT_SSL_VERIFY_SERVER_CERT = (1 << 30);

    /**
     * Don't reset the options after an unsuccessful connect
     * Client only flag.
     * Typically passed via ::mysql_real_connect() 's client_flag parameter.
     *
     * @sa mysql_real_connect()
     */
    int CLIENT_REMEMBER_OPTIONS = (1 << 31);
    /** @}*/

    /**
     * a compatibility alias for CLIENT_COMPRESS
     */
    int CAN_CLIENT_COMPRESS = CLIENT_COMPRESS;

    /**
     * Gather all possible capabilites (flags) supported by the server
     */
    int CLIENT_ALL_FLAGS = (CLIENT_LONG_PASSWORD | CLIENT_FOUND_ROWS | CLIENT_LONG_FLAG |
        CLIENT_CONNECT_WITH_DB | CLIENT_NO_SCHEMA | CLIENT_COMPRESS | CLIENT_ODBC |
        CLIENT_LOCAL_FILES | CLIENT_IGNORE_SPACE | CLIENT_PROTOCOL_41 |
        CLIENT_INTERACTIVE | CLIENT_SSL | CLIENT_IGNORE_SIGPIPE |
        CLIENT_TRANSACTIONS | CLIENT_RESERVED | CLIENT_RESERVED2 |
        CLIENT_MULTI_STATEMENTS | CLIENT_MULTI_RESULTS | CLIENT_PS_MULTI_RESULTS |
        CLIENT_SSL_VERIFY_SERVER_CERT | CLIENT_REMEMBER_OPTIONS |
        CLIENT_PLUGIN_AUTH | CLIENT_CONNECT_ATTRS |
        CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA |
        CLIENT_CAN_HANDLE_EXPIRED_PASSWORDS | CLIENT_SESSION_TRACK |
        CLIENT_DEPRECATE_EOF | CLIENT_OPTIONAL_RESULTSET_METADATA |
        CLIENT_ZSTD_COMPRESSION_ALGORITHM | CLIENT_QUERY_ATTRIBUTES);

    /**
     * Switch off from ::CLIENT_ALL_FLAGS the flags that are optional and
     * depending on build flags.
     * If any of the optional flags is supported by the build it will be switched
     * on before sending to the client during the connection handshake.
     */
    int CLIENT_BASIC_FLAGS = (CLIENT_ALL_FLAGS & ~(CLIENT_SSL | CLIENT_COMPRESS | CLIENT_SSL_VERIFY_SERVER_CERT
        | CLIENT_ZSTD_COMPRESSION_ALGORITHM));

}
