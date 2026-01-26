package com.turkcell.soccer.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "player_standings")
@Getter
@Setter
@NoArgsConstructor
public class PlayerStandings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long playerStandingId;


    @ManyToOne
    @JoinColumn(name = "league_id")
    private League league;

    @ManyToOne
    @JoinColumn(name = "player_id")
    private Player player;

    private int goals = 0;
    private int assists = 0;


}
