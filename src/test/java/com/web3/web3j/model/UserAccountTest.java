package com.web3.web3j.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserAccountTest {

    @Test
    void constructorAndGetters_work() {
        String username = "alice";
        String email = "alice@example.com";

        UserAccount user = new UserAccount(username, email);

        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertNotNull(user.getCreatedAt());

        Instant now = Instant.now();
        // createdAt should be very recent
        assertFalse(user.getCreatedAt().isAfter(now));
        assertTrue(Duration.between(user.getCreatedAt(), now).toSeconds() < 5);
    }

    @Test
    void settersUpdateFields() {
        UserAccount user = new UserAccount();
        user.setUsername("bob");
        user.setEmail("bob@example.com");

        assertEquals("bob", user.getUsername());
        assertEquals("bob@example.com", user.getEmail());

        Instant custom = Instant.parse("2020-01-01T00:00:00Z");
        user.setCreatedAt(custom);
        assertEquals(custom, user.getCreatedAt());
    }

    @Test
    void wallets_initiallyEmpty_and_mutable() {
        UserAccount user = new UserAccount("c", "c@example.com");

        List<?> initial = user.getWallets();
        assertNotNull(initial);
        assertEquals(0, initial.size());

        // Avoid referencing WalletEntity type in the test; use raw list to mutate
        @SuppressWarnings("rawtypes")
        List raw = user.getWallets();
        Object dummy = new Object();
        raw.add(dummy);

        assertEquals(1, user.getWallets().size());
        assertSame(dummy, user.getWallets().get(0));
    }

    @Test
    void setWallets_replacesList() {
        UserAccount user = new UserAccount("d", "d@example.com");

        List<Object> newList = new ArrayList<>();
        Object item = new Object();
        newList.add(item);

        // setWallets accepts List<WalletEntity> at compile in production; here we cast to raw to avoid WalletEntity type
        @SuppressWarnings({"unchecked", "rawtypes"})
        List casted = (List) newList;
        user.setWallets(casted);

        assertEquals(1, user.getWallets().size());
        assertSame(item, user.getWallets().get(0));
    }
}