package com.turkcell.soccer.mapper;

import com.turkcell.soccer.dto.PlayerDto;
import com.turkcell.soccer.dto.response.AdminTeamResponse;
import com.turkcell.soccer.dto.response.TeamInfoResponse;
import com.turkcell.soccer.dto.response.TeamResponse;
import com.turkcell.soccer.dto.response.TeamUpdateResponse;
import com.turkcell.soccer.model.Player;
import com.turkcell.soccer.model.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring", uses = PlayerMapper.class)
public interface TeamMapper {

    TeamInfoResponse toTeamInfoResponse(Team team);
    TeamResponse toTeamResponse(Team team);
    TeamUpdateResponse toTeamUpdateResponse(Team team);


    @Mapping(source = "players",
             target = "teamValue",
             qualifiedByName = "calculateTotalValue")
    AdminTeamResponse toAdminTeamResponse(Team team);

    // Interface implement eden methodlar kullanabilsin diye default keyword
    @Named("calculateTotalValue")
    default int calculateTotalValue(List<Player> players) {

        if (players == null || players.isEmpty()) {
            return 0;
        }

        int totalValue = 0;
        for (Player player : players) {
            totalValue += player.getValue();
        }
        return totalValue;
    }



}
