package com.turkcell.soccer.controller;

import com.turkcell.soccer.dto.request.AuthRequest;
import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.service.AccountService;
import com.turkcell.soccer.security.common.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication", description = "Authentication management API")
public class AuthController {

    private final AccountService accountService;

    public AuthController(AccountService accountService) {
        this.accountService = accountService;
    }

    @Autowired
    private JwtUtil jwtUtil;


    @Operation(
            summary = "Handles authentication",
            description = "Create JWT tokens upon successful login, and check further requests for that token"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token generated successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Unauthorized - Username or password is wrong"
            )
    })
    // Login
    @PostMapping("/auth")
    public ResponseEntity<String> authenticateUser(@Valid @RequestBody AuthRequest authRequest) {
        Account account = accountService.authenticate(authRequest.getUsername(), authRequest.getPassword());
        // Use account to get username to prevent bugs from user input just in case
        String token = jwtUtil.generateToken(account.getUsername());
        return ResponseEntity.ok(token);
    }


}
