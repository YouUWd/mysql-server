package com.youu.mysql.protocol.common;

import com.youu.mysql.protocol.pkg.req.LoginRequest;
import com.youu.mysql.protocol.storage.StorageConfig.HostPort;
import io.netty.bootstrap.Bootstrap;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/21
 */
public class StorageProperties {
    private HostPort hostPort;
    private Bootstrap bootstrap;

    private LoginRequest loginRequest;

    public StorageProperties(HostPort hostPort, Bootstrap bootstrap, LoginRequest loginRequest) {
        this.hostPort = hostPort;
        this.bootstrap = bootstrap;
        this.loginRequest = loginRequest;
    }

    public HostPort getHostPort() {
        return hostPort;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public LoginRequest getLoginRequest() {
        return loginRequest;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) { return false; }

        StorageProperties p = (StorageProperties)o;

        return this.hostPort.getHost().equals(p.hostPort.getHost())
            && this.hostPort.getPort() == p.hostPort.getPort();
    }

    @Override
    public int hashCode() {
        return this.hostPort.hashCode();
    }
}
