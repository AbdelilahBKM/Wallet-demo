package com.web3.web3j.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockchainServiceTest {

    @Mock
    Web3j web3j;

    BlockchainService blockchainService;

    @BeforeEach
    void setUp() {
        blockchainService = new BlockchainService(web3j);
    }

    @Test
    void isValidAddress_acceptsAndRejects() {
        assertTrue(blockchainService.isValidAddress("0x0000000000000000000000000000000000000000"));
        assertTrue(blockchainService.isValidAddress("0xAbCDEFabcdef0123456789012345678901234567"));

        assertFalse(blockchainService.isValidAddress(null));
        assertFalse(blockchainService.isValidAddress(""));
        assertFalse(blockchainService.isValidAddress("0x123"));
        assertFalse(blockchainService.isValidAddress("1234567890abcdef1234567890abcdef12345678"));
    }

    @Test
    void getEtherBalance_returnsEtherAmount() throws Exception {
        String address = "0x0000000000000000000000000000000000000001";

        @SuppressWarnings({"unchecked", "rawtypes"})
        Request req = mock(Request.class);
        EthGetBalance ethGetBalance = mock(EthGetBalance.class);

        when(web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST)).thenReturn(req);
        when(req.send()).thenReturn(ethGetBalance);
        when(ethGetBalance.getBalance()).thenReturn(new BigInteger("1000000000000000000")); // 1 ETH in wei

        BigDecimal result = blockchainService.getEtherBalance(address);
        assertEquals(new BigDecimal("1"), result.stripTrailingZeros());
    }

    @Test
    void transferERC20_returnsReceipt_whenReceiptFound() throws Exception {
        Credentials credentials = Credentials.create(Keys.createEcKeyPair());
        String contractAddress = "0x1111111111111111111111111111111111111111";
        String toAddress = "0x2222222222222222222222222222222222222222";
        BigInteger amount = BigInteger.valueOf(1000);

        // Mock nonce request
        @SuppressWarnings({"unchecked", "rawtypes"})
        Request reqNonce = mock(Request.class);
        EthGetTransactionCount ethGetTransactionCount = mock(EthGetTransactionCount.class);
        when(web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)).thenReturn(reqNonce);
        when(reqNonce.send()).thenReturn(ethGetTransactionCount);
        when(ethGetTransactionCount.getTransactionCount()).thenReturn(BigInteger.ONE);

        // Mock send raw transaction (separate Request)
        EthSendTransaction ethSendTransaction = mock(EthSendTransaction.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Request reqSendTx = mock(Request.class);
        when(web3j.ethSendRawTransaction(anyString())).thenReturn(reqSendTx);
        when(reqSendTx.send()).thenReturn(ethSendTransaction);
        when(ethSendTransaction.getTransactionHash()).thenReturn("0xdeadbeef");

        // Mock getTransactionReceipt (separate Request)
        EthGetTransactionReceipt ethGetTransactionReceipt = mock(EthGetTransactionReceipt.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Request reqGetReceipt = mock(Request.class);
        when(web3j.ethGetTransactionReceipt("0xdeadbeef")).thenReturn(reqGetReceipt);
        when(reqGetReceipt.send()).thenReturn(ethGetTransactionReceipt);

        TransactionReceipt receipt = new TransactionReceipt();
        receipt.setTransactionHash("0xdeadbeef");
        when(ethGetTransactionReceipt.getTransactionReceipt()).thenReturn(Optional.of(receipt));

        TransactionReceipt result = blockchainService.transferERC20(credentials, contractAddress, toAddress, amount);
        assertNotNull(result);
        assertEquals("0xdeadbeef", result.getTransactionHash());
    }

    @Test
    void transferERC20_throws_whenNoReceipt() throws Exception {
        Credentials credentials = Credentials.create(Keys.createEcKeyPair());
        String contractAddress = "0x1111111111111111111111111111111111111111";
        String toAddress = "0x2222222222222222222222222222222222222222";
        BigInteger amount = BigInteger.valueOf(1000);

        // Mock nonce request
        @SuppressWarnings({"unchecked", "rawtypes"})
        Request reqNonce = mock(Request.class);
        EthGetTransactionCount ethGetTransactionCount = mock(EthGetTransactionCount.class);
        when(web3j.ethGetTransactionCount(credentials.getAddress(), DefaultBlockParameterName.LATEST)).thenReturn(reqNonce);
        when(reqNonce.send()).thenReturn(ethGetTransactionCount);
        when(ethGetTransactionCount.getTransactionCount()).thenReturn(BigInteger.ONE);

        // Mock send raw transaction (separate Request)
        EthSendTransaction ethSendTransaction = mock(EthSendTransaction.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Request reqSendTx = mock(Request.class);
        when(web3j.ethSendRawTransaction(anyString())).thenReturn(reqSendTx);
        when(reqSendTx.send()).thenReturn(ethSendTransaction);
        when(ethSendTransaction.getTransactionHash()).thenReturn("0xnope");

        // Mock getTransactionReceipt (separate Request)
        EthGetTransactionReceipt ethGetTransactionReceipt = mock(EthGetTransactionReceipt.class);
        @SuppressWarnings({"unchecked", "rawtypes"})
        Request reqGetReceipt = mock(Request.class);
        when(web3j.ethGetTransactionReceipt("0xnope")).thenReturn(reqGetReceipt);
        when(reqGetReceipt.send()).thenReturn(ethGetTransactionReceipt);
        when(ethGetTransactionReceipt.getTransactionReceipt()).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                blockchainService.transferERC20(credentials, contractAddress, toAddress, amount));
        assertTrue(ex.getMessage().contains("Transaction receipt not found"));
    }
}
