package com.web3.web3j.controller;

import com.web3.web3j.service.UserService;
import com.web3.web3j.service.BlockchainService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

@RestController
@RequestMapping("/blockchain")
public class BlockchainController {

    private final UserService userService;
    private final BlockchainService blockchainService;

    public BlockchainController(UserService userService, BlockchainService blockchainService) {
        this.userService = userService;
        this.blockchainService = blockchainService;
    }

    @GetMapping("/wallets/{walletId}/balance")
    public ResponseEntity<?> getWalletBalance(@PathVariable Long walletId) {
        try {
            BigDecimal balance = userService.getWalletBalance(walletId);
            return ResponseEntity.ok(Map.of("balance", balance, "unit", "ETH"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get balance: " + e.getMessage());
        }
    }

    @GetMapping("/balance/{address}")
    public ResponseEntity<?> getAddressBalance(@PathVariable String address) {
        try {
            if (!blockchainService.isValidAddress(address)) {
                return ResponseEntity.badRequest().body("Invalid Ethereum address");
            }
            BigDecimal balance = blockchainService.getEtherBalance(address);
            return ResponseEntity.ok(Map.of("address", address, "balance", balance, "unit", "ETH"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get balance: " + e.getMessage());
        }
    }

    @PostMapping("/wallets/{walletId}/send-ether")
    public ResponseEntity<?> sendEther(@PathVariable Long walletId, @RequestBody Map<String, Object> request) {
        try {
            String password = (String) request.get("password");
            String toAddress = (String) request.get("toAddress");
            BigDecimal amount = new BigDecimal(request.get("amount").toString());

            if (password == null || toAddress == null || amount == null) {
                return ResponseEntity.badRequest().body("Missing required fields: password, toAddress, amount");
            }

            TransactionReceipt receipt = userService.sendEtherFromWallet(walletId, password, toAddress, amount);
            return ResponseEntity.ok(Map.of(
                "transactionHash", receipt.getTransactionHash(),
                "status", receipt.getStatus(),
                "gasUsed", receipt.getGasUsed()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Transaction failed: " + e.getMessage());
        }
    }

    @PostMapping("/wallets/{walletId}/transfer-erc20")
    public ResponseEntity<?> transferERC20(@PathVariable Long walletId, @RequestBody Map<String, Object> request) {
        try {
            String password = (String) request.get("password");
            String contractAddress = (String) request.get("contractAddress");
            String toAddress = (String) request.get("toAddress");
            BigInteger amount = new BigInteger(request.get("amount").toString());

            if (password == null || contractAddress == null || toAddress == null || amount == null) {
                return ResponseEntity.badRequest().body("Missing required fields: password, contractAddress, toAddress, amount");
            }

            TransactionReceipt receipt = userService.transferERC20FromWallet(walletId, password, contractAddress, toAddress, amount);
            return ResponseEntity.ok(Map.of(
                "transactionHash", receipt.getTransactionHash(),
                "status", receipt.getStatus(),
                "gasUsed", receipt.getGasUsed()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Transaction failed: " + e.getMessage());
        }
    }

    // === UTILITY ENDPOINTS ===

    @GetMapping("/validate-address/{address}")
    public ResponseEntity<Map<String, Boolean>> validateAddress(@PathVariable String address) {
        boolean isValid = blockchainService.isValidAddress(address);
        return ResponseEntity.ok(Map.of("valid", isValid));
    }
}
