package com.web3.web3j.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "wallets")
@JsonIgnoreProperties({"user"}) // Prevent circular reference by ignoring the user field in JSON serialization
public class WalletEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    // Ethereum address (with 0x prefix)
    @Column(nullable = false, unique = true)
    private String address;

    // Optional wallet name/label for user convenience
    private String walletName;

    // Encrypted keystore JSON (stores the private key encrypted with user password)
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String keystoreJson;

    private Instant createdAt = Instant.now();

    public WalletEntity() {}

    public WalletEntity(UserAccount user, String address, String walletName, String keystoreJson) {
        this.user = user;
        this.address = address;
        this.walletName = walletName;
        this.keystoreJson = keystoreJson;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }

    public UserAccount getUser() { return user; }
    public void setUser(UserAccount user) { this.user = user; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getWalletName() { return walletName; }
    public void setWalletName(String walletName) { this.walletName = walletName; }

    public String getKeystoreJson() { return keystoreJson; }
    public void setKeystoreJson(String keystoreJson) { this.keystoreJson = keystoreJson; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
