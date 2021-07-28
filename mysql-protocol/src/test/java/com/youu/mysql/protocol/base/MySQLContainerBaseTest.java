package com.youu.mysql.protocol.base;

import com.youu.mysql.protocol.codec.MySQLEncoder;
import com.youu.mysql.protocol.handler.MySQLClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/26
 */
public class MySQLContainerBaseTest {
    private static final DockerImageName MYSQL_80_IMAGE = DockerImageName.parse("mysql:8.0.25");

    protected static final String USER_NAME = "root";
    protected static final String PASS_WORD = "pass";

    protected static final MySQLContainer MYSQL = new MySQLContainer<>(MYSQL_80_IMAGE)
        .withDatabaseName("test")
        .withUsername(USER_NAME)
        .withPassword(PASS_WORD);

    protected static EventLoopGroup group = new NioEventLoopGroup();

    protected static Bootstrap bootstrap = new Bootstrap().group(group)
        .channel(NioSocketChannel.class)
        .handler(new ChannelInitializer<SocketChannel>() {
                     @Override
                     protected void initChannel(SocketChannel ch) {
                         ChannelPipeline pipeline = ch.pipeline();
                         // and then business logic.
                         pipeline.addLast(new MySQLEncoder());
                         pipeline.addLast(new MySQLClientHandler());
                     }
                 }
        );

    static {
        MYSQL.start();
    }

}
