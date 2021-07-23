package com.youu.mysql.storage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.google.common.collect.Lists;
import org.testcontainers.shaded.org.bouncycastle.util.Strings;

/**
 * @Author Timmy
 * @Description
 * @Date 2021/7/15
 */

public class StorageConfig {

    private static StorageConfig INSTANCE;

    private HostPort schema;

    private String username;
    private String password;

    private Map<String, String> userPass;
    private List<HostPort> storages;

    private StorageConfig() {
    }

    public static StorageConfig getConfig() {
        if (INSTANCE == null) {
            synchronized (StorageConfig.class) {
                if (INSTANCE == null) {
                    StorageConfig config = new StorageConfig();
                    Properties props = new Properties();
                    try {
                        props.load(StorageConfig.class.getClassLoader().getResourceAsStream("config.properties"));

                        String schema = props.getProperty("schema");
                        String[] hp = Strings.split(schema, ':');
                        config.setSchema(new HostPort(hp[0], Integer.valueOf(hp[1])));

                        config.setUsername(props.getProperty("username"));
                        config.setPassword(props.getProperty("password"));
                        String userPassStr = props.getProperty("user_pass");
                        String[] userPassArray = Strings.split(userPassStr, ',');
                        Map<String, String> userPass = new HashMap<>();
                        for (String up : userPassArray) {
                            String[] u_p = Strings.split(up, ':');
                            userPass.put(u_p[0], u_p[1]);
                        }
                        config.setUserPass(userPass);
                        String storagesStr = props.getProperty("storages");
                        String[] hostPorts = Strings.split(storagesStr, ',');
                        List<HostPort> storages = Lists.newArrayList();
                        for (String hostPort : hostPorts) {
                            String[] h_p = Strings.split(hostPort, ':');
                            storages.add(new HostPort(h_p[0], Integer.valueOf(h_p[1])));
                        }
                        config.setStorages(storages);
                        INSTANCE = config;
                    } catch (IOException e) {
                        throw new RuntimeException("config.properties should set...");
                    }

                }
            }
        }
        return INSTANCE;
    }

    public HostPort getSchema() {
        return schema;
    }

    public void setSchema(HostPort schema) {
        this.schema = schema;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getUserPass() {
        return userPass;
    }

    public void setUserPass(Map<String, String> userPass) {
        this.userPass = userPass;
    }

    public List<HostPort> getStorages() {
        return storages;
    }

    public void setStorages(List<HostPort> storages) {
        this.storages = storages;
    }

    public HostPort getStore(int index) {
        return storages.get(index - 1);
    }

    @Override
    public String toString() {
        return "StorageConfig{" +
            "schema=" + schema +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", userPass=" + userPass +
            ", storages=" + storages +
            '}';
    }

    public static class HostPort {
        private String host;
        private int port;

        public HostPort(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }

            if (o == null || getClass() != o.getClass()) { return false; }

            HostPort h = (HostPort)o;

            return h.host.equals(this.host) && h.port == this.port;
        }

        @Override
        public int hashCode() {
            return (this.host + this.port).hashCode();
        }

        @Override
        public String toString() {
            return "HostPort{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
        }
    }
}
