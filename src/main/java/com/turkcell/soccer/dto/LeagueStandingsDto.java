package com.turkcell.soccer.dto;

public record LeagueStandingsDto(
        Long teamId,
        String teamName,
        int played,
        int wins,
        int draws,
        int losses,
        int goalsScored,
        int goalsConceded,
        int average,
        int points
) {}