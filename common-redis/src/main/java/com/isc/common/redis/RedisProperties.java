package com.isc.common.redis;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "common.redis")
public class RedisProperties {

    /**
     * Mandatory.
     *
     * Every consuming service must explicitly specify:
     *
     * common.redis.enabled=true
     *
     * or:
     *
     * common.redis.enabled=false
     */
    private Boolean enabled;

    /**
     * Redis URI.
     *
     * Examples:
     *
     * redis://localhost:6379
     * redis://localhost:6379/0
     * redis://:password@localhost:6379/0
     * redis://username:password@localhost:6379/0
     * rediss://:password@redis.example.com:6380/0
     */
    private String url = "redis://localhost:6379";

    /**
     * Optional password.
     *
     * If specified, this takes precedence over the password
     * contained in the Redis URL.
     */
    private String password;

    /**
     * Redis logical database.
     *
     * The database specified in the URL takes precedence over
     * this property.
     */
    private int database = 0;

    /**
     * Redis command timeout.
     */
    private Duration timeout = Duration.ofSeconds(2);

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getDatabase() {
        return database;
    }

    public void setDatabase(int database) {
        this.database = database;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }
}