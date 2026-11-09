package com.catering.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PasswordHelperTest {

    @Test
    void correctPasswordVerifies() {
        String hash = PasswordHelper.hash("secret123");
        assertTrue(PasswordHelper.verify("secret123", hash));
    }

    @Test
    void wrongPasswordFails() {
        String hash = PasswordHelper.hash("secret123");
        assertFalse(PasswordHelper.verify("wrong", hash));
    }

    @Test
    void hashIsNotPlainText() {
        String hash = PasswordHelper.hash("secret123");
        assertNotEquals("secret123", hash);
    }
}