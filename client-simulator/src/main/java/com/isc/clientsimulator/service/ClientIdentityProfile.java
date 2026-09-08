package com.isc.clientsimulator.service;

public class ClientIdentityProfile {
    private String cid;
    private String did;
    private String securityLevel;
    private String publicKey;

    public String getCid() { return cid; }
    public void setCid(String cid) { this.cid = cid; }
    public String getDid() { return did; }
    public void setDid(String did) { this.did = did; }
    public String getSecurityLevel() { return securityLevel; }
    public void setSecurityLevel(String securityLevel) { this.securityLevel = securityLevel; }
    public String getPublicKey() { return publicKey; }
    public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
}
