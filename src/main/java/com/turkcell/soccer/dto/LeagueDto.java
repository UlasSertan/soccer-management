package com.turkcell.soccer.dto;

import com.turkcell.soccer.model.LeagueStandings;
import com.turkcell.soccer.model.PlayerStandings;
import com.turkcell.soccer.model.Team;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LeagueDto {

    private Long leagueId;

    private List<Long> teamIds;

    private List<Long> standingIds;

    private List<Long> playerStandingIds;
}
