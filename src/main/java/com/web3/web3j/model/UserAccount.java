package com.web3.web3j.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_account")
@JsonIgnoreProperties({"wallets"}) // Prevent circular reference by ignoring wallets in JSON serialization
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String email;

    private Instant createdAt = Instant.now();

    // One user can have multiple wallets
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<WalletEntity> wallets = new ArrayList<>();

    public UserAccount() {}

    public UserAccount(String username, String email) {
        this.username = username;
        this.email = email;
        this.createdAt = Instant.now();
    }

    // Getters and Setters
    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<WalletEntity> getWallets() { return wallets; }
    public void setWallets(List<WalletEntity> wallets) { this.wallets = wallets; }
}
