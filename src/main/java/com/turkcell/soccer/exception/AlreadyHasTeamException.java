package com.turkcell.soccer.exception;

public class AlreadyHasTeamException extends RuntimeException {
    public AlreadyHasTeamException(String message) {
        super(message);
    }

}
