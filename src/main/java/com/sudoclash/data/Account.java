package com.sudoclash.data;

import java.io.Serializable;
import java.util.Locale;

import com.sudoclash.exception.StorageException;

// Encapsulation: private final fields, only getters. An account never changes: a new password makes a new object.
public class Account implements Serializable {
    private static final long serialVersionUID = 3L;

    private final String username;
    private final String salt;
    private final String passwordHash;
    // 0 = old account (one round of salted SHA-256), otherwise the PBKDF2 work factor.
    private final int iterations;
    private final long createdAt;
    private final long updatedAt;
    // Salted hashes of the private recovery answers (nickname and hobby). Empty strings when none are set.
    private final String nickSalt;
    private final String nickHash;
    private final String hobbySalt;
    private final String hobbyHash;

    public Account(String username, String salt, String passwordHash, int iterations, long createdAt,
                   long updatedAt, String nickSalt, String nickHash, String hobbySalt, String hobbyHash) {
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
        this.iterations = iterations;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.nickSalt = nickSalt;
        this.nickHash = nickHash;
        this.hobbySalt = hobbySalt;
        this.hobbyHash = hobbyHash;
    }

    // An account saved by the first version of SudoClash.
    public Account(String username, String salt, String passwordHash, long createdAt) {
        this(username, salt, passwordHash, 0, createdAt, createdAt, "", "", "", "");
    }

    // One rule for comparing usernames everywhere ("Amy", "amy" and "AMY" are the same player).
    // Locale.ROOT makes the result the same on every computer (for example the Turkish "I" problem).
    public static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    // Creates an account with a new PBKDF2 hash (no recovery answers yet).
    public static Account create(String username, String password) {
        long now = System.currentTimeMillis();
        String salt = PasswordHasher.newSalt();
        int rounds = PasswordHasher.CURRENT_ITERATIONS;
        return new Account(username, salt, PasswordHasher.pbkdf2(password, salt, rounds), rounds, now, now, "", "", "", "");
    }

    public Account withNewPassword(String password, long now) {
        String newSalt = PasswordHasher.newSalt();
        int rounds = PasswordHasher.CURRENT_ITERATIONS;
        return new Account(username, newSalt, PasswordHasher.pbkdf2(password, newSalt, rounds), rounds, createdAt,
                Math.max(now, updatedAt + 1), nickSalt, nickHash, hobbySalt, hobbyHash);
    }

    // The answers are saved as salted hashes, never as text.
    public Account withAnswers(String nickname, String hobby, long now) {
        String newNickSalt = PasswordHasher.newSalt();
        String newHobbySalt = PasswordHasher.newSalt();
        return new Account(username, salt, passwordHash, iterations, createdAt, Math.max(now, updatedAt + 1),
                newNickSalt, answerHash(nickname, newNickSalt), newHobbySalt, answerHash(hobby, newHobbySalt));
    }

    private static String answerHash(String answer, String saltHex) {
        String clean = RecoveryAnswers.normalize(answer);
        // PBKDF2 refuses an empty secret; an empty answer simply never matches.
        return PasswordHasher.pbkdf2(clean.isEmpty() ? "\u0000" : clean, saltHex, RecoveryAnswers.ITERATIONS);
    }

    public String getUsername() {
        return username;
    }

    public String getSalt() {
        return salt;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public int getIterations() {
        return iterations;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public String getNickSalt() {
        return nickSalt;
    }

    public String getNickHash() {
        return nickHash;
    }

    public String getHobbySalt() {
        return hobbySalt;
    }

    public String getHobbyHash() {
        return hobbyHash;
    }

    public boolean hasAnswers() {
        return !nickHash.isEmpty() && !hobbyHash.isEmpty();
    }

    // True for accounts that still use the old, fast hash.
    public boolean needsUpgrade() {
        return iterations < PasswordHasher.CURRENT_ITERATIONS;
    }

    public boolean checkPassword(String password) {
        if (iterations == 0) {
            return PasswordHasher.matches(password, salt, passwordHash);
        }
        return PasswordHasher.constantTimeEquals(PasswordHasher.pbkdf2(password, salt, iterations), passwordHash);
    }

    public boolean checkNickname(String answer) {
        return hasAnswers() && PasswordHasher.constantTimeEquals(answerHash(answer, nickSalt), nickHash);
    }

    public boolean checkHobby(String answer) {
        return hasAnswers() && PasswordHasher.constantTimeEquals(answerHash(answer, hobbySalt), hobbyHash);
    }

    // null when the account looks fine, otherwise the reason (used before accepting imported data).
    public String validate() {
        if (username == null || !username.matches("[A-Za-z0-9_]{2,16}")) {
            return "bad username";
        }
        if (!PasswordHasher.isHex(salt, 2, 64) || !PasswordHasher.isHex(passwordHash, 64, 64)) {
            return "bad password data";
        }
        if (iterations != 0 && (iterations < 1000 || iterations > 5_000_000)) {
            return "bad work factor";
        }
        if (createdAt <= 0 || updatedAt < createdAt) {
            return "bad time";
        }
        if (!validAnswerPair(nickSalt, nickHash) || !validAnswerPair(hobbySalt, hobbyHash)
                || nickHash.isEmpty() != hobbyHash.isEmpty()) {
            return "bad recovery data";
        }
        return null;
    }

    private static boolean validAnswerPair(String saltHex, String hashHex) {
        if (saltHex.isEmpty() && hashHex.isEmpty()) {
            return true;
        }
        return PasswordHasher.isHex(saltHex, 2, 64) && PasswordHasher.isHex(hashHex, 64, 64);
    }

    private static String dash(String text) {
        return text.isEmpty() ? "-" : text;
    }

    private static String undash(String text) {
        return text.equals("-") ? "" : text;
    }

    // C|username|salt|hash|iterations|created|updated|nickSalt|nickHash|hobbySalt|hobbyHash   ("-" means empty)
    public String toLine() {
        return "C|" + username + "|" + salt + "|" + passwordHash + "|" + iterations + "|" + createdAt + "|"
                + updatedAt + "|" + dash(nickSalt) + "|" + dash(nickHash) + "|" + dash(hobbySalt) + "|"
                + dash(hobbyHash);
    }

    // Reads the new "C" line, the "B" line of the previous version (its recovery code is no longer used)
    // and the "A" line of the first version.
    public static Account fromLine(String line) throws StorageException {
        String[] p = line.split("\\|", -1);
        try {
            if (p.length == 5 && p[0].equals("A")) {
                return new Account(p[1], p[2], p[3], Long.parseLong(p[4]));
            }
            if (p.length == 9 && p[0].equals("B")) {
                return new Account(p[1], p[2], p[3], Integer.parseInt(p[4]), Long.parseLong(p[5]),
                        Long.parseLong(p[6]), "", "", "", "");
            }
            if (p.length == 11 && p[0].equals("C")) {
                return new Account(p[1], p[2], p[3], Integer.parseInt(p[4]), Long.parseLong(p[5]),
                        Long.parseLong(p[6]), undash(p[7]), undash(p[8]), undash(p[9]), undash(p[10]));
            }
        } catch (NumberFormatException e) {
            throw new StorageException("Bad account line", e);
        }
        throw new StorageException("Bad account line: " + line);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Account)) {
            return false;
        }
        return normalize(username).equals(normalize(((Account) other).username));
    }

    @Override
    public int hashCode() {
        return normalize(username).hashCode();
    }

    @Override
    public String toString() {
        return username;
    }
}
