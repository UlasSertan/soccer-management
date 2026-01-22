package com.turkcell.soccer.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AccountInfoResponse {

    @NotNull
    private Long id;
    @NotBlank
    private String username;
    @NotBlank
    private String email;
    @NotNull
    private LocalDateTime createdAt;
}
