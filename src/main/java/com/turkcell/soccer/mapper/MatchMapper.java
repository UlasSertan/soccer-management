package com.turkcell.soccer.mapper;

import com.turkcell.soccer.dto.MatchResult;
import com.turkcell.soccer.dto.Match;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface MatchMapper {

    @Mapping(source = "homeTeam", target = "homeTeamName")
    @Mapping(source = "awayTeam", target = "awayTeamName")
    Match toEntity(MatchResult matchResult);

    List<Match> toDtoList(List<MatchResult> matches);
}