package com.turkcell.soccer.exception;

public class NoSuchPlayerException extends RuntimeException {
    public NoSuchPlayerException(String playerName) {
        super("No player with name " + playerName);
    }
}
