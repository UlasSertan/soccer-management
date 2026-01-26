package com.turkcell.soccer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "league_standings")
@Getter
@Setter
@NoArgsConstructor
public class LeagueStandings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long standingId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id")
    private Team team;

    private int played = 0;
    private int wins = 0;
    private int draws = 0;
    private int losses = 0;
    private int goalsScored = 0;
    private int goalsConceded = 0;
    private int points = 0;
    private int goalDifference;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "league_id")
    private League league;

    @PrePersist
    @PreUpdate
    public void calculateStats() {
        this.goalDifference =  goalsScored - goalsConceded;
        this.points = this.wins*3 + this.draws;
    }

    public void incrementGoalsConceded(int newGoalsConceded) {
        this.goalsConceded += newGoalsConceded;
    }

    public void incrementGoalsScored(int newGoalsScored) {
        this.goalsScored += newGoalsScored;
    }
}
