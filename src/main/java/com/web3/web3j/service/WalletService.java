package com.web3.web3j.service;

import com.web3.web3j.model.UserAccount;
import com.web3.web3j.model.WalletEntity;
import com.web3.web3j.repository.UserAccountRepository;
import com.web3.web3j.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserAccountRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WalletService(WalletRepository walletRepository, UserAccountRepository userRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new wallet for a user. The password encrypts the keystore JSON.
     * @param userId The user ID
     * @param password Password to encrypt the wallet
     * @param walletName Optional name for the wallet
     * @return Created WalletEntity
     */
    public WalletEntity createWalletForUser(Long userId, String password, String walletName) throws Exception {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Create temporary directory for keystore generation
        Path tmpDir = Files.createTempDirectory("keystore");
        try {
            // Generate new wallet file in temporary directory
            String fileName = WalletUtils.generateFullNewWalletFile(password, tmpDir.toFile());
            Path walletFile = tmpDir.resolve(fileName);

            // Read the keystore JSON content
            String keystoreJson = Files.readString(walletFile, StandardCharsets.UTF_8);

            // Parse JSON to WalletFile object and decrypt to validate
            WalletFile walletFileObj = objectMapper.readValue(keystoreJson, WalletFile.class);
            ECKeyPair keyPair = Wallet.decrypt(password, walletFileObj);
            Credentials credentials = Credentials.create(keyPair);
            String address = credentials.getAddress();

            // Create and save wallet entity
            WalletEntity walletEntity = new WalletEntity(user, address, walletName, keystoreJson);
            return walletRepository.save(walletEntity);

        } finally {
            // Clean up temporary files
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(tmpDir)) {
                for (Path p : ds) {
                    Files.deleteIfExists(p);
                }
            } catch (Exception ignore) {}
            Files.deleteIfExists(tmpDir);
        }
    }

    /**
     * Load credentials for a wallet using the user's password
     * @param walletId The wallet ID
     * @param password The password used to encrypt the wallet
     * @return Credentials if successful, empty if wallet not found or wrong password
     */
    public Optional<Credentials> loadCredentials(Long walletId, String password) {
        return walletRepository.findById(walletId).map(wallet -> {
            try {
                // Parse JSON string to WalletFile object
                WalletFile walletFile = objectMapper.readValue(wallet.getKeystoreJson(), WalletFile.class);
                ECKeyPair keyPair = Wallet.decrypt(password, walletFile);
                return Credentials.create(keyPair);
            } catch (Exception e) {
                // Wrong password or corrupted keystore
                return null;
            }
        });
    }

    /**
     * Get all wallets for a user
     */
    public List<WalletEntity> getWalletsForUser(Long userId) {
        return walletRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get a specific wallet by ID
     */
    public Optional<WalletEntity> getWalletById(Long walletId) {
        return walletRepository.findById(walletId);
    }

    /**
     * Find wallet by Ethereum address
     */
    public Optional<WalletEntity> getWalletByAddress(String address) {
        return walletRepository.findByAddress(address);
    }

    /**
     * Delete a wallet (be careful - this permanently removes access to funds!)
     */
    public void deleteWallet(Long walletId) {
        walletRepository.deleteById(walletId);
    }

    /**
     * Update wallet name
     */
    public Optional<WalletEntity> updateWalletName(Long walletId, String newName) {
        return walletRepository.findById(walletId).map(wallet -> {
            wallet.setWalletName(newName);
            return walletRepository.save(wallet);
        });
    }
}
