package com.turkcell.soccer.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AccountInfoResponse {

    @NotBlank
    private Long id;
    @NotBlank
    private String username;
    @NotBlank
    private LocalDateTime createdAt;
}
