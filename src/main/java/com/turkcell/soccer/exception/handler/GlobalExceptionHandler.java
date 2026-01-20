package com.turkcell.soccer.exception.handler;

import com.turkcell.soccer.dto.response.ErrorResponse;
import com.turkcell.soccer.exception.AlreadyHasTeamException;
import com.turkcell.soccer.exception.InvalidTokenException;
import com.turkcell.soccer.exception.NoSuchPlayerException;
import com.turkcell.soccer.exception.NoSuchTeamException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.FORBIDDEN.value(),
                "NOT AUTHORIZED",
                "You do not have permission for this action!",
                System.currentTimeMillis()
        );
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<String> handleInvalidTokenException(InvalidTokenException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleNotFound(UsernameNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(NoSuchTeamException.class)
    public ResponseEntity<String> handleNoSuchField(NoSuchTeamException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(NoSuchPlayerException.class)
    public ResponseEntity<String> handleNoSuchPlayer(NoSuchPlayerException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(AlreadyHasTeamException.class)
    public ResponseEntity<String> handleAlreadyHasTeam(AlreadyHasTeamException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
