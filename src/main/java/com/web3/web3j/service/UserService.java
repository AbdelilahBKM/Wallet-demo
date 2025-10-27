package com.web3.web3j.service;

import com.web3.web3j.model.UserAccount;
import com.web3.web3j.model.WalletEntity;
import org.springframework.stereotype.Service;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import com.web3.web3j.repository.UserAccountRepository;

@Service
public class UserService {
    private final UserAccountRepository userRepository;
    private final WalletService walletService;
    private final BlockchainService blockchainService;

    public UserService(UserAccountRepository userRepository, WalletService walletService, BlockchainService blockchainService) {
        this.userRepository = userRepository;
        this.walletService = walletService;
        this.blockchainService = blockchainService;
    }

    // Create a new user account
    public UserAccount createUser(String username, String email) {
        UserAccount user = new UserAccount(username, email);
        return userRepository.save(user);
    }

    // Create a new wallet for a user
    public WalletEntity createWalletForUser(Long userId, String password, String walletName) throws Exception {
        return walletService.createWalletForUser(userId, password, walletName);
    }

    // Get all wallets for a user
    public List<WalletEntity> getUserWallets(Long userId) {
        return walletService.getWalletsForUser(userId);
    }

    // Get balance for a specific wallet
    public BigDecimal getWalletBalance(Long walletId) throws Exception {
        Optional<WalletEntity> wallet = walletService.getWalletById(walletId);
        if (wallet.isEmpty()) {
            throw new RuntimeException("Wallet not found");
        }
        return blockchainService.getEtherBalance(wallet.get().getAddress());
    }

    // Send ETH from a user's wallet
    public TransactionReceipt sendEtherFromWallet(Long walletId, String password, String toAddress, BigDecimal amountEther) throws Exception {
        Optional<Credentials> credentials = walletService.loadCredentials(walletId, password);
        if (credentials.isEmpty()) {
            throw new RuntimeException("Invalid wallet or password");
        }

        if (!blockchainService.isValidAddress(toAddress)) {
            throw new IllegalArgumentException("Invalid destination address");
        }

        return blockchainService.sendEther(credentials.get(), toAddress, amountEther);
    }

    // Transfer ERC20 tokens from a user's wallet
    public TransactionReceipt transferERC20FromWallet(Long walletId, String password, String contractAddress, String toAddress, BigInteger amount) throws Exception {
        Optional<Credentials> credentials = walletService.loadCredentials(walletId, password);
        if (credentials.isEmpty()) {
            throw new RuntimeException("Invalid wallet or password");
        }

        if (!blockchainService.isValidAddress(toAddress) || !blockchainService.isValidAddress(contractAddress)) {
            throw new IllegalArgumentException("Invalid address");
        }

        return blockchainService.transferERC20(credentials.get(), contractAddress, toAddress, amount);
    }

    // Get user by ID
    public Optional<UserAccount> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    // Get user by username
    public Optional<UserAccount> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // Update wallet name
    public Optional<WalletEntity> updateWalletName(Long walletId, String newName) {
        return walletService.updateWalletName(walletId, newName);
    }

    // Delete a wallet (be very careful with this!)
    public void deleteWallet(Long walletId) {
        walletService.deleteWallet(walletId);
    }
}