package com.turkcell.soccer.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountUpdateResponse {
    @NotNull
    private Long id;
    @NotBlank
    private String username;
    @NotBlank
    private String email;
}
