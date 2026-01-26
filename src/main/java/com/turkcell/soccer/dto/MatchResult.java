package com.turkcell.soccer.dto;

import jakarta.validation.constraints.NotNull;

public record MatchResult(
        Long homeTeamId,
        Long awayTeamId,
        String homeTeam,
        String awayTeam,
        int homeScore,
        int awayScore
) {

    @Override
    @NotNull
    public String toString() {
        return homeTeam + " " + homeScore + " - " + awayScore + " " + awayTeam;
    }
}