package com.turkcell.soccer.controller;

import com.turkcell.soccer.dto.AuthRequest;
import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.service.AccountService;
import com.turkcell.soccer.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Autowired
    private JwtUtil jwtUtil;


    @PostMapping("/auth")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        Account account = accountService.authenticate(authRequest.getUsername(), authRequest.getPassword());
        // Use account to get username to prevent bugs from user input just in case
        String token = jwtUtil.generateToken(account.getUsername());
        return ResponseEntity.ok(token);
    }


}
