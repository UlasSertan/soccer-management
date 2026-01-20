package com.turkcell.soccer.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountUpdateResponse {
    @NotBlank
    private Long id;
    @NotBlank
    private String username;
    @NotBlank
    private String email;
}
