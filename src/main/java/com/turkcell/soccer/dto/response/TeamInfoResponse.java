package com.turkcell.soccer.dto.response;

import com.turkcell.soccer.dto.PlayerDto;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TeamInfoResponse {

    private Long id;
    private String name;
    private String country;
    private Integer playerCount;
    private Integer budget;
    private List<PlayerDto> players;
}
