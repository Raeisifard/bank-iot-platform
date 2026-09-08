package com.isc.clientsimulator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "simulator")
public class ClientSimulatorProperties {
    private ClientIdentityService clientIdentityService = new ClientIdentityService();
    private TokenService tokenService = new TokenService();
    private Mqtt mqtt = new Mqtt();
    private Android android = new Android();
    private Security security = new Security();

    public ClientIdentityService getClientIdentityService() { return clientIdentityService; }
    public void setClientIdentityService(ClientIdentityService clientIdentityService) { this.clientIdentityService = clientIdentityService; }
    public TokenService getTokenService() { return tokenService; }
    public void setTokenService(TokenService tokenService) { this.tokenService = tokenService; }
    public Mqtt getMqtt() { return mqtt; }
    public void setMqtt(Mqtt mqtt) { this.mqtt = mqtt; }
    public Android getAndroid() { return android; }
    public void setAndroid(Android android) { this.android = android; }
    public Security getSecurity() { return security; }
    public void setSecurity(Security security) { this.security = security; }

    public static class ClientIdentityService {
        private String baseUrl = "http://localhost:8085";
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }

    public static class TokenService {
        private String baseUrl = "http://localhost:8083";
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }

    public static class Mqtt {
        private String host = "localhost";
        private int tcpPort = 1883;
        private int tlsPort = 8883;
        private int websocketPort = 8084;
        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getTcpPort() { return tcpPort; }
        public void setTcpPort(int tcpPort) { this.tcpPort = tcpPort; }
        public int getTlsPort() { return tlsPort; }
        public void setTlsPort(int tlsPort) { this.tlsPort = tlsPort; }
        public int getWebsocketPort() { return websocketPort; }
        public void setWebsocketPort(int websocketPort) { this.websocketPort = websocketPort; }
    }

    public static class Android {
        private String packageName = "com.bank.mobile";
        private String version = "1.0.0";
        public String getPackageName() { return packageName; }
        public void setPackageName(String packageName) { this.packageName = packageName; }
        public String getVersion() { return version; }
        public void setVersion(String version) { this.version = version; }
    }

    public static class Security {
        private String defaultLevel = "TRUSTED_ENVIRONMENT";
        public String getDefaultLevel() { return defaultLevel; }
        public void setDefaultLevel(String defaultLevel) { this.defaultLevel = defaultLevel; }
    }
}
