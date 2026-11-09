// Put in: com.catering.util
package com.catering.util;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/** Reversible AES-GCM encryption so the admin can view issued passwords. */
public class PasswordVault {

    // Change this text to your own secret. If you change it later, old saved
    // passwords can no longer be decrypted.
    private static final String SECRET = "change-this-secret-before-submission";
    private static final SecureRandom RANDOM = new SecureRandom();

    private static SecretKeySpec key() throws Exception {
        byte[] k = MessageDigest.getInstance("SHA-256")
                .digest(SECRET.getBytes(StandardCharsets.UTF_8));
        return new SecretKeySpec(k, "AES");
    }

    public static String encrypt(String plain) {
        try {
            byte[] iv = new byte[12];
            RANDOM.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            byte[] enc = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] all = new byte[iv.length + enc.length];
            System.arraycopy(iv, 0, all, 0, iv.length);
            System.arraycopy(enc, 0, all, iv.length, enc.length);
            return Base64.getEncoder().encodeToString(all);
        } catch (Exception e) {
            throw new IllegalStateException("Could not encrypt the password.", e);
        }
    }

    public static String decrypt(String stored) {
        try {
            byte[] all = Base64.getDecoder().decode(stored);
            byte[] iv = Arrays.copyOfRange(all, 0, 12);
            byte[] data = Arrays.copyOfRange(all, 12, all.length);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(128, iv));
            return new String(c.doFinal(data), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Could not read the saved password.", e);
        }
    }
}