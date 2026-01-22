package com.turkcell.soccer.exception;

public class PlayerNotInTransferListException extends RuntimeException {
    public PlayerNotInTransferListException(String message) {
        super(message);
    }
}
