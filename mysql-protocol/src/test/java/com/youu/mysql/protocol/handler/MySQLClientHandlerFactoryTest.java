package com.youu.mysql.protocol.handler;

import java.net.URI;

import com.youu.mysql.protocol.base.MySQLContainerBaseTest;
import com.youu.mysql.protocol.common.StorageProperties;
import com.youu.mysql.protocol.pkg.req.ComQuery;
import com.youu.mysql.protocol.pkg.req.ComQuit;
import com.youu.mysql.protocol.pkg.req.LoginRequest;
import com.youu.mysql.storage.StorageConfig.HostPort;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.apache.commons.pool2.PooledObject;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class MySQLClientHandlerFactoryTest extends MySQLContainerBaseTest {

    private static MySQLClientHandlerFactory factory = new MySQLClientHandlerFactory();
    private static StorageProperties properties;

    @BeforeClass
    public static void init() {
        String jdbcUrl = MYSQL.getJdbcUrl();
        URI uri = URI.create(jdbcUrl.substring(5));
        LoginRequest loginRequest = new LoginRequest();
        String loginHex
            =
            "d900000107a23e01ffffff001c0000000000000000000000000000000000000000000000726f6f74001457a7fdd47410b2a244b2796d529bfa7afdd802946d7973716c5f6e61746976655f70617373776f72640088105f72756e74696d655f76657273696f6e09312e382e305f3233310f5f636c69656e745f76657273696f6e06382e302e32350f5f636c69656e745f6c6963656e73650347504c0f5f72756e74696d655f76656e646f72124f7261636c6520436f72706f726174696f6e0c5f636c69656e745f6e616d65114d7953514c20436f6e6e6563746f722f4a";
        loginRequest.read(Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump(loginHex)));

        properties = new StorageProperties(new HostPort(uri.getHost(), uri.getPort()), bootstrap, loginRequest);
    }

    @Test
    public void create() throws Exception {
        MySQLClientHandler h1 = factory.create(properties);
        MySQLClientHandler h2 = factory.create(properties);

        ComQuery query = new ComQuery();
        query.setQuery("select 1");

        Assert.assertEquals(
            "010000010117000002036465660000000131000c3f000100000008810000000002000003013107000004fe000002000000",
            ByteBufUtil.hexDump(h1.execute(query)));

        Assert.assertEquals(
            "010000010117000002036465660000000131000c3f000100000008810000000002000003013107000004fe000002000000",
            ByteBufUtil.hexDump(h2.execute(query)));
        h1.execute(new ComQuit());
        h2.execute(new ComQuit());
    }

    @Test
    public void wrap() throws Exception {
        MySQLClientHandler h1 = factory.create(properties);
        PooledObject<MySQLClientHandler> pool = factory.wrap(h1);
        Assert.assertSame(h1, pool.getObject());
    }
}