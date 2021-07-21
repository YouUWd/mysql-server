package com.youu.common;

import java.security.DigestException;
import java.util.ArrayList;
import java.util.Properties;

import com.google.common.collect.Lists;
import com.mysql.cj.conf.PropertyKey;
import com.mysql.cj.jdbc.JdbcPropertySetImpl;
import com.mysql.cj.log.Log;
import com.mysql.cj.log.LogFactory;
import com.mysql.cj.protocol.AuthenticationPlugin;
import com.mysql.cj.protocol.Security;
import com.mysql.cj.protocol.a.NativeCapabilities;
import com.mysql.cj.protocol.a.NativePacketPayload;
import com.mysql.cj.protocol.a.NativeProtocol;
import com.mysql.cj.protocol.a.authentication.MysqlNativePasswordPlugin;
import com.mysql.cj.util.StringUtils;
import io.netty.buffer.ByteBufUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/16
 */
public class JDBCTest {
    @Test
    public void testNativeCapabilities() {
        String handshake
            =
            "0a382e302e3232000d0000005d722c1213317c7800ffffff0200ffc7150000000000000000000044390f0657293f1b501c2756006d7973716c5f6e61746976655f70617373776f726400";
        byte[] bytes = ByteBufUtil.decodeHexDump(handshake);
        NativePacketPayload payload = new NativePacketPayload(bytes);
        NativeCapabilities nc = new NativeCapabilities();
        nc.setInitialHandshakePacket(payload);
        System.out.println(nc);
    }

    @Test
    public void testNativeAuthenticationProvider() {
        String handshake
            =
            "0a382e302e3232000d0000005d722c1213317c7800ffffff0200ffc7150000000000000000000044390f0657293f1b501c2756006d7973716c5f6e61746976655f70617373776f726400";
        byte[] bytes = ByteBufUtil.decodeHexDump(handshake);
        NativePacketPayload payload = new NativePacketPayload(bytes);

        Properties info = new Properties();
        info.put("user", "root");
        info.put("password", "pass");
        JdbcPropertySetImpl propertySet = new JdbcPropertySetImpl();
        propertySet.initializeProperties(info);

        NativeProtocol protocol = new NativeProtocol(
            LogFactory.getLogger(propertySet.getStringProperty(PropertyKey.logger).getStringValue(),
                Log.LOGGER_INSTANCE_NAME));

        System.out.println(protocol);

        NativeCapabilities nc = new NativeCapabilities();
        nc.setInitialHandshakePacket(payload);

        AuthenticationPlugin plugin = new MysqlNativePasswordPlugin();

        plugin.init(protocol);

        plugin.setAuthenticationParameters("root", "pass");
        NativePacketPayload fromServer = nc.getInitialHandshakePacket();
        ArrayList<Object> toServer = Lists.newArrayList();
        plugin.nextAuthenticationStep(fromServer, toServer);
        System.out.println(toServer);

    }

    @Test
    public void testAuthenticationPlugin() {
        String handshake
            =
            "4a0000000a382e302e3232001900000005177b3f3c01661300ffffff0200ffc71500000000000000000000493e5c06475b620108272612006d7973716c5f6e61746976655f70617373776f726400";
        byte[] bytes = ByteBufUtil.decodeHexDump(handshake);
        NativePacketPayload payload = new NativePacketPayload(bytes);

        Properties info = new Properties();
        info.put("user", "root");
        info.put("password", "pass");
        JdbcPropertySetImpl propertySet = new JdbcPropertySetImpl();
        propertySet.initializeProperties(info);

        NativeProtocol protocol = new NativeProtocol(
            LogFactory.getLogger(propertySet.getStringProperty(PropertyKey.logger).getStringValue(),
                Log.LOGGER_INSTANCE_NAME));

        protocol.setPropertySet(propertySet);
        System.out.println(protocol.getPasswordCharacterEncoding());

        NativeCapabilities nc = new NativeCapabilities();
        nc.setInitialHandshakePacket(payload);

        AuthenticationPlugin plugin = new MysqlNativePasswordPlugin();

        plugin.init(protocol, cb -> {
        });

        plugin.setAuthenticationParameters("root", "pass");
        NativePacketPayload fromServer = nc.getInitialHandshakePacket();
        ArrayList<Object> toServer = Lists.newArrayList();
        plugin.nextAuthenticationStep(fromServer, toServer);

        System.out.println(toServer);
        System.out.println(toServer.get(0));
        System.out.println("-------");

    }

    @Test
    public void test() throws DigestException {
        String seed
            =
            "05177b3f3c016613493e5c06475b620108272612";
        byte[] bytes = ByteBufUtil.decodeHexDump(seed);
        byte[] passes = Security.scramble411("pass", bytes, "UTF-8");
        //edfa0ea3fae9331608a41dffbf6cd4b66cdc6244
        //edfa0ea3fae9331608a41dffbf6cd4b66cdc6244
        System.out.println(ByteBufUtil.hexDump(passes));
        seed
            =
            "3265444432306867715b69197e13732a3f222822";

        bytes = ByteBufUtil.decodeHexDump(seed);
        //3f51707f61f93a45959e2d284c1066fe5c16187723c49ae295f937569945a66f
        //3f51707f61f93a45959e2d284c1066fe5c16187723c49ae295f937569945a66f
        passes = Security
            .scrambleCachingSha2(StringUtils.getBytes("password", "UTF-8"), bytes);
        System.out.println(ByteBufUtil.hexDump(passes));

        seed
            =
            "58083e3857434e2e764f645f62764b6b6e627125";
        bytes = ByteBufUtil.decodeHexDump(seed);

        passes = Security.scramble411("pass", bytes, "GBK");

        Assert.assertEquals("57a7fdd47410b2a244b2796d529bfa7afdd80294", ByteBufUtil.hexDump(passes));
    }
}
