package com.turkcell.soccer.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamUpdateResponse {
    @NotNull
    private long id;
    @NotBlank
    private String name;
    @NotBlank
    private String country;


}
