package com.web3.web3j.service;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.tx.Transfer;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Service
public class BlockchainService {
    private final Web3j web3j;

    public BlockchainService(Web3j web3j) {
        this.web3j = web3j;
    }

    // Get ETH balance for an address (returns value in Ether)
    public BigDecimal getEtherBalance(String address) throws Exception {
        EthGetBalance balance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
        return Convert.fromWei(new BigDecimal(balance.getBalance()), Convert.Unit.ETHER);
    }

    // Send Ether using provided credentials
    public TransactionReceipt sendEther(Credentials credentials, String toAddress, BigDecimal amountEther) throws Exception {
        return Transfer.sendFunds(web3j, credentials, toAddress, amountEther, Convert.Unit.ETHER).send();
    }

    // Transfer ERC20 tokens
    public TransactionReceipt transferERC20(Credentials credentials, String contractAddress, String toAddress, BigInteger amount) throws Exception {
        // Get nonce
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.LATEST).send();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        // Create transfer function
        Function function = new Function(
                "transfer",
                Arrays.asList(new Address(toAddress), new Uint256(amount)),
                Collections.emptyList());

        String encodedFunction = FunctionEncoder.encode(function);

        // Create raw transaction
        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                DefaultGasProvider.GAS_PRICE,
                DefaultGasProvider.GAS_LIMIT,
                contractAddress,
                encodedFunction);

        // Sign and send transaction
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Numeric.toHexString(signedMessage);

        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
        String transactionHash = ethSendTransaction.getTransactionHash();

        // Wait for transaction receipt
        Optional<TransactionReceipt> receiptOptional = getTransactionReceipt(transactionHash);
        return receiptOptional.orElseThrow(() -> new RuntimeException("Transaction receipt not found"));
    }

    // Helper method to get transaction receipt
    private Optional<TransactionReceipt> getTransactionReceipt(String transactionHash) throws Exception {
        EthGetTransactionReceipt transactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send();
        return transactionReceipt.getTransactionReceipt();
    }

    // Check if address is valid Ethereum address
    public boolean isValidAddress(String address) {
        try {
            return address != null && address.matches("^0x[a-fA-F0-9]{40}$");
        } catch (Exception e) {
            return false;
        }
    }
}
