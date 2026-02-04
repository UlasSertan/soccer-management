package com.turkcell.soccer.exception;

public class NoSuchPlayerException extends RuntimeException {
    public NoSuchPlayerException(String message) {
        super(message);
    }
}
