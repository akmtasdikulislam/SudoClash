package com.sudoclash.exception;

// Thrown for a wrong password, a taken username or invalid input.
public class AuthException extends SudoClashException {
    private static final long serialVersionUID = 1L;

    public AuthException(String message) {
        super(message);
    }
}
