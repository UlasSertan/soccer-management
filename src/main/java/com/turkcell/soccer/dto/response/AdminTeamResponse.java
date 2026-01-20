package com.turkcell.soccer.dto.response;

import com.turkcell.soccer.dto.PlayerDto;
import lombok.Data;

import java.util.List;

@Data
public class AdminTeamResponse {

    private Long id;

    private String name;
    private String country;

    private Integer playerCount;

    private Integer budget;

    private List<PlayerDto> players;

    private int teamValue;

    public AdminTeamResponse(Long id, String name, String country,
                             Integer playerCount, Integer budget,
                             List<PlayerDto> players, int teamValue) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.playerCount = playerCount;
        this.budget = budget;

        this.players = players;
        this.teamValue = teamValue;

    }


}
