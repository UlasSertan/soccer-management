package com.turkcell.soccer.dto;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "matches")
@Data
@NoArgsConstructor
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long leagueId;
    private int week;

    private Long homeTeamId;
    private String homeTeamName;

    private Long awayTeamId;
    private String awayTeamName;

    private int homeScore;
    private int awayScore;

    private boolean played;
}