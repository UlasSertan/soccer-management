package com.turkcell.soccer.dto;

public record PlayerStandingsDto(
        Long playerId,
        String playerName,
        String teamName,
        int goals,
        int assists
) {}