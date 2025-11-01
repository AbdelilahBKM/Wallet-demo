package com.web3.web3j.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web3.web3j.model.UserAccount;
import com.web3.web3j.model.WalletEntity;
import com.web3.web3j.repository.UserAccountRepository;
import com.web3.web3j.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    WalletRepository walletRepository;

    @Mock
    UserAccountRepository userRepository;

    WalletService walletService;

    ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        walletService = new WalletService(walletRepository, userRepository);
    }

    @Test
    void loadCredentials_returnsCredentials_whenPasswordCorrect() throws Exception {
        String password = "testpass";
        ECKeyPair keyPair = Keys.createEcKeyPair();
        WalletFile wf = Wallet.createStandard(password, keyPair);
        String keystoreJson = objectMapper.writeValueAsString(wf);

        WalletEntity w = new WalletEntity(null, wf.getAddress(), "n", keystoreJson);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(w));

        Optional<Credentials> creds = walletService.loadCredentials(1L, password);
        assertTrue(creds.isPresent());
        assertEquals(creds.get().getAddress().toLowerCase(), ("0x" + wf.getAddress()).toLowerCase());
    }

    @Test
    void loadCredentials_returnsEmpty_whenPasswordWrong() throws Exception {
        String password = "correct";
        ECKeyPair keyPair = Keys.createEcKeyPair();
        WalletFile wf = Wallet.createStandard(password, keyPair);
        String keystoreJson = objectMapper.writeValueAsString(wf);

        WalletEntity w = new WalletEntity(null, wf.getAddress(), "n", keystoreJson);

        when(walletRepository.findById(2L)).thenReturn(Optional.of(w));

        Optional<org.web3j.crypto.Credentials> creds = walletService.loadCredentials(2L, "wrong");
        assertFalse(creds.isPresent());
    }

    @Test
    void createWalletForUser_generatesAndSavesWallet() throws Exception {
        UserAccount user = new UserAccount("u", "u@example.com");
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(walletRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        WalletEntity created = walletService.createWalletForUser(10L, "p123", "myw");

        assertNotNull(created);
        assertNotNull(created.getKeystoreJson());
        assertTrue(created.getAddress().startsWith("0x") || created.getAddress().startsWith("0X"));
        verify(walletRepository, times(1)).save(any(WalletEntity.class));
    }

    @Test
    void getWalletsForUser_delegatesToRepository() {
        WalletEntity w = new WalletEntity(null, "0x1", "n", "{}");
        when(walletRepository.findAllByUserIdOrderByCreatedAtDesc(5L)).thenReturn(List.of(w));

        List<WalletEntity> list = walletService.getWalletsForUser(5L);
        assertEquals(1, list.size());
        assertSame(w, list.get(0));
    }

    @Test
    void updateWalletName_updatesAndSaves() {
        WalletEntity w = new WalletEntity(null, "0x1", "old", "{}");
        when(walletRepository.findById(7L)).thenReturn(Optional.of(w));
        when(walletRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Optional<WalletEntity> opt = walletService.updateWalletName(7L, "newName");
        assertTrue(opt.isPresent());
        assertEquals("newName", opt.get().getWalletName());
        verify(walletRepository).save(w);
    }

    @Test
    void deleteWallet_invokesRepositoryDelete() {
        doNothing().when(walletRepository).deleteById(8L);
        walletService.deleteWallet(8L);
        verify(walletRepository).deleteById(8L);
    }

    @Test
    void getWalletByAddress_returnsRepoOptional() {
        WalletEntity w = new WalletEntity(null, "0xabc", "n", "{}");
        when(walletRepository.findByAddress("0xabc")).thenReturn(Optional.of(w));

        Optional<WalletEntity> opt = walletService.getWalletByAddress("0xabc");
        assertTrue(opt.isPresent());
        assertSame(w, opt.get());
    }
}
