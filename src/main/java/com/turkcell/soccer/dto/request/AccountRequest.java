package com.turkcell.soccer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Account creation request")
public class AccountRequest {
    
    @Schema(description = "Username for the account", example = "johndoe", required = true)
    private String username;
    
    @Schema(description = "Email address", example = "john.doe@example.com", required = true)
    private String email;
    
    @Schema(description = "Password for the account", example = "SecurePassword123!", required = true)
    private String password;

    // Constructors
    public AccountRequest() {
    }

    public AccountRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
