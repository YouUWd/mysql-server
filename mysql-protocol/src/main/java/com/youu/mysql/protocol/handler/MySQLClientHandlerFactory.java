package com.youu.mysql.protocol.handler;

import java.util.concurrent.CyclicBarrier;

import com.youu.mysql.storage.StorageConfig;
import com.youu.mysql.storage.StorageConfig.HostPort;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/20
 */
@Slf4j
public class MySQLClientHandlerFactory extends BaseKeyedPooledObjectFactory<Integer, MySQLClientHandler> {

    private final Bootstrap bootstrap;

    public MySQLClientHandlerFactory(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    @Override
    public MySQLClientHandler create(Integer storeIndex) throws Exception {
        HostPort store = StorageConfig.getConfig().getStore(storeIndex);
        ChannelFuture f = bootstrap.connect(store.getHost(), store.getPort())
            .sync();
        MySQLClientHandler handler = (MySQLClientHandler)f.channel().pipeline().last();
        handler.login();
        return handler;
    }

    @Override
    public PooledObject<MySQLClientHandler> wrap(MySQLClientHandler handler) {
        return new DefaultPooledObject<>(handler);
    }

    @Override
    public void destroyObject(Integer key, PooledObject<MySQLClientHandler> p) {
        log.info("destroyObject {} {}", key, p);
        p.getObject().stop();
    }
}
