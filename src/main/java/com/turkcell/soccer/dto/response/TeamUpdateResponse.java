package com.turkcell.soccer.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamUpdateResponse {
    @NotBlank
    private long id;
    @NotBlank
    private String name;
    @NotBlank
    private String country;


}
