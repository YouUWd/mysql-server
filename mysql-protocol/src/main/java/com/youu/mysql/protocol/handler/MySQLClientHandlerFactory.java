package com.youu.mysql.protocol.handler;

import com.google.common.primitives.Bytes;
import com.mysql.cj.CharsetMapping;
import com.mysql.cj.protocol.Security;
import com.mysql.cj.util.StringUtils;
import com.youu.mysql.protocol.common.StorageProperties;
import com.youu.mysql.protocol.pkg.req.LoginRequest;
import com.youu.mysql.protocol.pkg.res.HandshakePacket;
import com.youu.mysql.storage.StorageConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/20
 */
public class MySQLClientHandlerFactory extends BaseKeyedPooledObjectFactory<StorageProperties, MySQLClientHandler> {

    @Override
    public MySQLClientHandler create(StorageProperties properties) throws Exception {
        ChannelFuture f = properties.getBootstrap().connect(properties.getHostPort().getHost(),
            properties.getHostPort().getPort())
            .sync();
        // Get the handler instance to retrieve the answer.
        MySQLClientHandler handler = (MySQLClientHandler)f.channel().pipeline().last();

        ByteBuf handshakeData = handler.handshake();
        HandshakePacket handshakePacket = new HandshakePacket();
        handshakePacket.read(handshakeData);
        byte[] authPluginDataPart1 = handshakePacket.getAuthPluginDataPart1();
        byte[] authPluginDataPart2 = handshakePacket.getAuthPluginDataPart2();
        byte[] authPluginDataPart2True = new byte[authPluginDataPart2.length - 1];
        System.arraycopy(authPluginDataPart2, 0, authPluginDataPart2True, 0, authPluginDataPart2True.length);
        byte[] seed = Bytes.concat(authPluginDataPart1, authPluginDataPart2True);

        LoginRequest loginRequest = properties.getLoginRequest();
        byte[] passes;
        if ("caching_sha2_password".equals(handshakePacket.getAuthPluginName())) {
            passes = Security
                .scrambleCachingSha2(
                    StringUtils.getBytes(StorageConfig.getConfig().getUserPass().get(loginRequest.getUsername()),
                        CharsetMapping.getJavaEncodingForCollationIndex(loginRequest.getCharacterSet())),
                    seed);
            loginRequest.setAuthPluginName("caching_sha2_password");
        } else {
            passes = Security.scramble411(StorageConfig.getConfig().getUserPass().get(loginRequest.getUsername()),
                seed,
                CharsetMapping.getJavaEncodingForCollationIndex(loginRequest.getCharacterSet()));
        }

        loginRequest.setAuthResponse(passes);

        handler.execute(loginRequest);
        return handler;
    }

    @Override
    public PooledObject<MySQLClientHandler> wrap(MySQLClientHandler handler) {
        return new DefaultPooledObject(handler);
    }

}
