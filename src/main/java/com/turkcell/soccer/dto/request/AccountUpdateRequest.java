package com.turkcell.soccer.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateRequest {

    @NotBlank
    private String password;
    @NotBlank
    private String email;


}
