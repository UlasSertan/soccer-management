package com.turkcell.soccer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerDto {

    private Long id;

    private String firstName;

    private String lastName;

    private String country;

    private Integer value;

    private Integer age;

    private String position;
}
