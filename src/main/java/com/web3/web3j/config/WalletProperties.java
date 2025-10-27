package com.web3.web3j.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "wallets")
public class WalletProperties {

    private List<WalletEntry> entries = new ArrayList<>();

    public List<WalletEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<WalletEntry> entries) {
        this.entries = entries;
    }

    public static class WalletEntry {
        // optional identifier; if empty the service will assign one
        private String id;
        // either a raw private key (hex, optional) ...
        private String privateKey;
        // ... or a keystore path + password (optional)
        private String keystorePath;
        private String keystorePassword;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getPrivateKey() { return privateKey; }
        public void setPrivateKey(String privateKey) { this.privateKey = privateKey; }

        public String getKeystorePath() { return keystorePath; }
        public void setKeystorePath(String keystorePath) { this.keystorePath = keystorePath; }

        public String getKeystorePassword() { return keystorePassword; }
        public void setKeystorePassword(String keystorePassword) { this.keystorePassword = keystorePassword; }
    }
}
