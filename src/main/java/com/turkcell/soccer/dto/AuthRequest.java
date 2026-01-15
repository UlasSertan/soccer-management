package com.turkcell.soccer.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(exclude = "password")
public class AuthRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

}
