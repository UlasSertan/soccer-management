package com.turkcell.soccer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "leagues")
@Getter
@Setter
@NoArgsConstructor
public class League {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long leagueId;

    @NotNull
    @Size(min = 2, max = 20)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "leagues_teams",
            joinColumns = @JoinColumn(name = "league_id"),
            inverseJoinColumns = @JoinColumn(name = "team_id")
    )
    private List<Team> teams;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL)
    private List<LeagueStandings> leagueStandings;

    @OneToMany(mappedBy = "league", cascade = CascadeType.ALL)
    private List<PlayerStandings> playerStandings;



}
