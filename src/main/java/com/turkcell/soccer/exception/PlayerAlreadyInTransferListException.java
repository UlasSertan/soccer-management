package com.turkcell.soccer.exception;


public class PlayerAlreadyInTransferListException extends RuntimeException {
    public PlayerAlreadyInTransferListException(String message) {
        super(message);
    }
}
