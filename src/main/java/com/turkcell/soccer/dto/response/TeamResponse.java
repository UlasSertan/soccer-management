package com.turkcell.soccer.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class TeamResponse {

    @NotBlank
    private Long id;
    @NotBlank
    private String teamName;
    @NotBlank
    private String country;

    @NotBlank
    private Integer budget;

}
