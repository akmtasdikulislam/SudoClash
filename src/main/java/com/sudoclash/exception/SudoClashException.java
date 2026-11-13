package com.sudoclash.exception;

// Parent of every checked exception in SudoClash (exception inheritance).
public class SudoClashException extends Exception {
    private static final long serialVersionUID = 1L;

    public SudoClashException(String message) {
        super(message);
    }

    public SudoClashException(String message, Throwable cause) {
        super(message, cause);
    }
}
