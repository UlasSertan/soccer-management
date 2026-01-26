package com.turkcell.soccer.mapper;

import com.turkcell.soccer.dto.LeagueDto;
import com.turkcell.soccer.model.League;
import com.turkcell.soccer.model.LeagueStandings;
import com.turkcell.soccer.model.PlayerStandings;
import com.turkcell.soccer.model.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LeagueMapper {

    @Mapping(source = "teams", target = "teamIds")
    @Mapping(source = "leagueStandings", target = "standingIds")
    @Mapping(source = "playerStandings", target = "playerStandingIds")
    LeagueDto leagueToLeagueDto(League league);


    default Long mapTeamToId(Team team) {
        if (team == null) return null;
        return team.getId();
    }

    default Long mapStandingToId(LeagueStandings standing) {
        if (standing == null) return null;
        return standing.getStandingId();
    }

    default Long mapPlayerStandingToId(PlayerStandings playerStanding) {
        if (playerStanding == null) return null;
        return playerStanding.getPlayerStandingId();
    }
}