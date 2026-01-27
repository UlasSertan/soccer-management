package com.turkcell.soccer.mapper;

import com.turkcell.soccer.dto.LeagueDto;
import com.turkcell.soccer.dto.LeagueStandingsDto;
import com.turkcell.soccer.dto.PlayerStandingsDto;
import com.turkcell.soccer.model.League;
import com.turkcell.soccer.model.LeagueStandings;
import com.turkcell.soccer.model.PlayerStandings;
import com.turkcell.soccer.model.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

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

    @Mapping(source = "team.id", target = "teamId")
    @Mapping(source = "team.name", target = "teamName")
    @Mapping(target = "average", expression = "java(s.getGoalsScored() - s.getGoalsConceded())")
    LeagueStandingsDto toLeagueStandingsDto(LeagueStandings s);

    List<LeagueStandingsDto> toLeagueStandingsDtoList(List<LeagueStandings> standings);


    // --- PLAYER STANDINGS MAPPING ---
    @Mapping(source = "player.id", target = "playerId")
    @Mapping(target = "playerName", source = "player")
    @Mapping(source = "player.team.name", target = "teamName")
    PlayerStandingsDto toPlayerStandingsDto(PlayerStandings p);

    List<PlayerStandingsDto> toPlayerStandingsDtoList(List<PlayerStandings> playerStandings);


    default String mapPlayerName(com.turkcell.soccer.model.Player player) {
        if (player == null) {
            return null;
        }
        return player.getFirstName() + " " + player.getLastName();
    }

}