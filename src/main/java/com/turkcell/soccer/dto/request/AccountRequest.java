package com.turkcell.soccer.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Account creation request")
public class AccountRequest {
    
    @Schema(description = "Username for the account", example = "johndoe", required = true)
    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Za-z])[A-Za-z0-9]+$",
            message = "Username must be alphanumeric with at least 1 letter"
    )
    private String username;
    
    @Schema(description = "Email address", example = "john.doe@example.com", required = true)
    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter a valid email address.")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com(\\.[A-Za-z]{2,})?$",
            message = "Email must end with .com or .com.xx (e.g. example.com, example.com.tr)."
    )
    private String email;
    
    @Schema(description = "Password for the account", example = "SecurePassword123!", required = true)
    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 16, message = "Password must be between 8 and 16 characters.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$",
            message = "Password must contain at least 1 uppercase letter, 1 lowercase letter, 1 number, and 1 special character."
    )
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
