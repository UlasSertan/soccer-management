package com.turkcell.soccer.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String country;

    private Integer value;

    private Integer age;

    private String position;

    private String team; // Team id too
}
