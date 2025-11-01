package com.web3.web3j.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class WalletEntityTest {

    @Test
    void constructorAndGettersAndSetters_work() {
        UserAccount user = new UserAccount("u1", "u1@example.com");
        String address = "0x0000000000000000000000000000000000000001";
        String name = "MyWallet";
        String keystore = "{\"dummy\":true}";

        WalletEntity w = new WalletEntity(user, address, name, keystore);

        assertSame(user, w.getUser());
        assertEquals(address, w.getAddress());
        assertEquals(name, w.getWalletName());
        assertEquals(keystore, w.getKeystoreJson());
        assertNotNull(w.getCreatedAt());

        // Test setters
        w.setWalletName("NewName");
        assertEquals("NewName", w.getWalletName());

        Instant now = Instant.parse("2020-01-01T00:00:00Z");
        w.setCreatedAt(now);
        assertEquals(now, w.getCreatedAt());

        w.setAddress("0x00000000000000000000000000000000000000FF");
        assertEquals("0x00000000000000000000000000000000000000FF", w.getAddress());
    }
}

