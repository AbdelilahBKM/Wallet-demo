package com.web3.web3j.controller;

import com.web3.web3j.model.WalletEntity;
import com.web3.web3j.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/wallets")
public class WalletController {
    private final UserService userService;

    public WalletController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create/{userId}/wallets")
    public ResponseEntity<?> createWallet(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            String walletName = request.get("walletName");

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Password is required");
            }

            WalletEntity wallet = userService.createWalletForUser(userId, password, walletName);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create wallet: " + e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/wallets")
    public ResponseEntity<List<WalletEntity>> getUserWallets(@PathVariable Long userId) {
        List<WalletEntity> wallets = userService.getUserWallets(userId);
        return ResponseEntity.ok(wallets);
    }

    @PutMapping("/wallets/{walletId}/name")
    public ResponseEntity<?> updateWalletName(@PathVariable Long walletId, @RequestBody Map<String, String> request) {
        String newName = request.get("walletName");
        Optional<WalletEntity> updated = userService.updateWalletName(walletId, newName);
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/wallets/{walletId}")
    public ResponseEntity<?> deleteWallet(@PathVariable Long walletId) {
        try {
            userService.deleteWallet(walletId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete wallet: " + e.getMessage());
        }
    }
}
