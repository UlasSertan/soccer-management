package com.turkcell.soccer.mapper;


import com.turkcell.soccer.dto.PlayerDto;
import com.turkcell.soccer.dto.response.PlayerResponse;
import com.turkcell.soccer.model.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    PlayerDto playerToDto(Player player);
    @Mapping(source = "team.name", target = "team")
    PlayerResponse playerToResponse(Player player);
    List<PlayerDto> toPlayerDtoList(List<Player> players);

}

