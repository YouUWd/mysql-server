package com.youu.mysql.protocol.pkg.req;

import java.util.Map;

import com.google.common.collect.Maps;
import com.youu.mysql.protocol.pkg.MySQLPacket;
import com.youu.mysql.protocol.util.MySQLBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.mysql.cj.protocol.a.NativeServerSession.CLIENT_CONNECT_ATTRS;
import static com.mysql.cj.protocol.a.NativeServerSession.CLIENT_CONNECT_WITH_DB;
import static com.mysql.cj.protocol.a.NativeServerSession.CLIENT_PLUGIN_AUTH;
import static com.mysql.cj.protocol.a.NativeServerSession.CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA;
import static com.mysql.cj.protocol.a.NativeServerSession.CLIENT_SECURE_CONNECTION;

/**
 * @Author Timmy
 * @Description HandshakeResponse Send By Client for username and password etc.
 * @Date 2021/6/17
 * @see
 * <a href="https://dev.mysql.com/doc/internals/en/connection-phase-packets.html#packet-Protocol::HandshakeResponse">HandshakeResponse</a>
 */
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest extends MySQLPacket {
    private long capabilityFlags;
    private long maxPacketSize;
    private short characterSet;
    private static final byte[] RESERVED = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
        0};
    private String username;
    private byte[] authResponse;
    private String database;
    private String authPluginName;
    private long attributesLength;
    private Map<String, String> attributes = Maps.newLinkedHashMap();

    @Override
    protected void readPayload(ByteBuf buffer) {
        //TODO 密码校验等
        this.capabilityFlags = buffer.readUnsignedIntLE();
        this.maxPacketSize = buffer.readUnsignedIntLE();
        this.characterSet = buffer.readUnsignedByte();
        buffer.skipBytes(23);//reserved (all [0])
        this.username = MySQLBufUtil.readNullTerminatedString(buffer);
        if ((capabilityFlags & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) == CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) {
            this.authResponse = new byte[(int)MySQLBufUtil.readLenEncInteger(buffer)];
            buffer.readBytes(this.authResponse);
        } else if ((capabilityFlags & CLIENT_SECURE_CONNECTION) == CLIENT_SECURE_CONNECTION) {
            this.authResponse = new byte[(int)MySQLBufUtil.readLenEncInteger(buffer)];
            buffer.readBytes(this.authResponse);
        } else {
            this.authResponse = MySQLBufUtil.readNullTerminatedBytes(buffer);
        }
        if ((capabilityFlags & CLIENT_CONNECT_WITH_DB) == CLIENT_CONNECT_WITH_DB) {
            this.database = MySQLBufUtil.readNullTerminatedString(buffer);
        }

        if ((capabilityFlags & CLIENT_PLUGIN_AUTH) == CLIENT_PLUGIN_AUTH) {
            this.authPluginName = MySQLBufUtil.readNullTerminatedString(buffer);
        }
        if ((capabilityFlags & CLIENT_CONNECT_ATTRS) == CLIENT_CONNECT_ATTRS) {
            this.attributesLength = MySQLBufUtil.readLenEncInteger(buffer);
            while (buffer.isReadable()) {
                this.attributes.put(MySQLBufUtil.readLenEncString(buffer), MySQLBufUtil.readLenEncString(buffer));
            }
        }
    }

    @Override
    protected void writePayload(ByteBuf buffer) {
        buffer.writeIntLE((int)capabilityFlags);
        buffer.writeIntLE((int)maxPacketSize);
        buffer.writeByte(characterSet);
        buffer.writeBytes(RESERVED);//reserved (all [0])
        MySQLBufUtil.writeNullTerminatedString(buffer, username);

        if ((capabilityFlags & CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) == CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA) {
            MySQLBufUtil.writeLenEncInt(buffer, (long)authResponse.length);
            buffer.writeBytes(authResponse);
        } else if ((capabilityFlags & CLIENT_SECURE_CONNECTION) == CLIENT_SECURE_CONNECTION) {
            MySQLBufUtil.writeLenEncInt(buffer, (long)authResponse.length);
            buffer.writeBytes(authResponse);
        } else {
            buffer.writeBytes(authResponse);
            buffer.writeByte(0x00);
        }
        if ((capabilityFlags & CLIENT_CONNECT_WITH_DB) == CLIENT_CONNECT_WITH_DB) {
            MySQLBufUtil.writeNullTerminatedString(buffer, database);
        }

        if ((capabilityFlags & CLIENT_PLUGIN_AUTH) == CLIENT_PLUGIN_AUTH) {
            MySQLBufUtil.writeNullTerminatedString(buffer, authPluginName);
        }
        if ((capabilityFlags & CLIENT_CONNECT_ATTRS) == CLIENT_CONNECT_ATTRS) {
            ByteBuf buf = Unpooled.buffer(2048);
            attributes.forEach((k, v) -> {
                MySQLBufUtil.writeLenEncString(buf, k);
                MySQLBufUtil.writeLenEncString(buf, v);
            });
            MySQLBufUtil.writeLenEncInt(buffer, (long)buf.writerIndex());
            buffer.writeBytes(buf);
        }
    }

}
