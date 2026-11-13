package com.sudoclash.data;

import java.util.Locale;

// The two private answers (nickname, hobby) that prove who you are when the password is forgotten.
// They are compared in a forgiving way ("Chess!", " chess " and "CHESS" are the same) and only a salted hash is saved.
public final class RecoveryAnswers {
    public static final int MIN_LENGTH = 2;
    public static final int MAX_NICKNAME = 24;
    public static final int MAX_HOBBY = 30;
    // Answers are short and the number of guesses is limited, so a lighter work factor than for passwords is enough.
    public static final int ITERATIONS = 100_000;

    private RecoveryAnswers() {
    }

    // Lower case, only letters and digits.
    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        StringBuilder clean = new StringBuilder();
        for (char c : text.trim().toLowerCase(Locale.ROOT).toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                clean.append(c);
            }
        }
        return clean.toString();
    }

    // null when the text is fine, otherwise what is wrong (shown to the player).
    public static String nicknameProblem(String nickname) {
        return problem(nickname, "nickname", MAX_NICKNAME);
    }

    public static String hobbyProblem(String hobby) {
        return problem(hobby, "hobby", MAX_HOBBY);
    }

    private static String problem(String text, String what, int max) {
        String trimmed = text == null ? "" : text.trim();
        if (normalize(trimmed).length() < MIN_LENGTH) {
            return "Enter a " + what + " (at least " + MIN_LENGTH + " letters or numbers).";
        }
        if (trimmed.length() > max) {
            return "The " + what + " can have at most " + max + " characters.";
        }
        return null;
    }
}
