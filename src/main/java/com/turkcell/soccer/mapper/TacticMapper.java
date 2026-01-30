package com.turkcell.soccer.mapper;

import com.turkcell.soccer.dto.TacticDto;
import com.turkcell.soccer.model.Tactic;
import com.turkcell.soccer.model.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface TacticMapper {
    @Mapping(source = "teams", target = "teamsIds", qualifiedByName = "mapTeamIds")
    TacticDto toDto(Tactic tactic);

    @Named("mapTeamIds")
    default List<Long> mapTeamIds(List<Team> teams) {
        if (teams == null) {return new ArrayList<>();}
        List<Long> teamIds = new ArrayList<>();
        for (Team team : teams) {
            teamIds.add(team.getId());
        }
        return teamIds;
    }
}
