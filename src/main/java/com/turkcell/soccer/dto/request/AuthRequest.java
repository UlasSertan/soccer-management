package com.turkcell.soccer.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "password")
public class AuthRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;

}
