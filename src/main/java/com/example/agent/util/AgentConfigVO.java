package com.example.agent.util;

import java.util.Properties;

public class AgentConfigVO {
    private final Properties config;

    public AgentConfigVO(Properties config) {
        this.config = config;
    }


    public String getServerId() {
        return config.getProperty("server.id", "default-server");
    }

    public String getWebSocketUrl() {
        return config.getProperty("monitoring.Server.Url");
    }

    public int getMaxReconnectAttempts() {
        return Integer.parseInt(config.getProperty("MaxReconnect.Attempts", "5"));
    }

    public String getProperty(String key) {
        return config.getProperty(key);
    }

    public Properties getRaw() {
        return config;
    }
}
