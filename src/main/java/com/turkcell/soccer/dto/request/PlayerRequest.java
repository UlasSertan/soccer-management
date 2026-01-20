package com.turkcell.soccer.dto.request;

import com.turkcell.soccer.model.Team;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerRequest {


    private String firstName;

    private String lastName;

    private String country;

    private Integer value;

    private Integer age;

    private String position;

    private String team;


}
