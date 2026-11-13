package com.sudoclash.exception;

// Too many wrong attempts: login (or recovery) is blocked for a while.
public class LockedOutException extends AuthException {
    private static final long serialVersionUID = 1L;

    private final long secondsRemaining;

    public LockedOutException(long secondsRemaining) {
        super("Too many wrong attempts. Try again in " + format(secondsRemaining) + ".");
        this.secondsRemaining = secondsRemaining;
    }

    public long getSecondsRemaining() {
        return secondsRemaining;
    }

    // m:ss
    public static String format(long seconds) {
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }
}
