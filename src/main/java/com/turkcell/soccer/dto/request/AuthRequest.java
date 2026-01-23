package com.turkcell.soccer.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class AuthRequest {

    @NotBlank(message = "Username is required.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])[A-Za-z0-9]+$",
            message = "Username must be alphanumeric with at least 1 letter"
    )
    private String username;
    @NotBlank(message = "Password is required.")
    private String password;

}
