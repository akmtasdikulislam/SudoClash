package com.sudoclash.data;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

// Passwords are never saved as plain text.
// New accounts use PBKDF2-HMAC-SHA256 with a random salt and many iterations (slow on purpose, so guessing is expensive).
// Old accounts used one round of salted SHA-256; they still log in and are upgraded at their next successful login.
public final class PasswordHasher {
    // The work factor. It is saved with every account, so it can be raised later without breaking old accounts.
    public static final int CURRENT_ITERATIONS = 310_000;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordHasher() {
    }

    public static String newSalt() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);
        return toHex(bytes);
    }

    // PBKDF2: used for new passwords and for recovery codes.
    public static String pbkdf2(String secret, String saltHex, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(secret.toCharArray(), fromHex(saltHex), iterations, 256);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return toHex(factory.generateSecret(spec).getEncoded());
        } catch (GeneralSecurityException e) {
            // Every Java 8+ installation has PBKDF2WithHmacSHA256, so this cannot happen.
            throw new IllegalStateException("PBKDF2 is not available", e);
        } finally {
            spec.clearPassword();
        }
    }

    // Method overloading (compile-time polymorphism): the old SHA-256 way, only to check old accounts.
    public static String hash(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] result = digest.digest((salt + password).getBytes(StandardCharsets.UTF_8));
            return toHex(result);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public static String hash(String password) {
        return hash(password, "");
    }

    public static boolean matches(String password, String salt, String expectedHash) {
        return constantTimeEquals(hash(password, salt), expectedHash);
    }

    // Takes the same time whether the first or the last character differs.
    public static boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean isHex(String text, int minLength, int maxLength) {
        return text != null && text.length() >= minLength && text.length() <= maxLength
                && text.length() % 2 == 0 && text.matches("[0-9a-f]+");
    }

    private static String toHex(byte[] bytes) {
        StringBuilder text = new StringBuilder();
        for (byte b : bytes) {
            text.append(String.format("%02x", b));
        }
        return text.toString();
    }

    private static byte[] fromHex(String hex) {
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }
}
