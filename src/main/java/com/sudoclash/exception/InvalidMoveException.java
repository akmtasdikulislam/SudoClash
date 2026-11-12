package com.sudoclash.exception;

public class InvalidMoveException extends SudoClashException {
    private static final long serialVersionUID = 1L;

    public InvalidMoveException(String message) {
        super(message);
    }
}
