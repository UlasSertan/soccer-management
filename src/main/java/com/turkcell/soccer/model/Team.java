package com.turkcell.soccer.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "teams")
public class Team {

    // To prevent racing conditions - double purchases
    @Version
    private Long version;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column (nullable = false)
    private String name;

    @NotBlank
    @Column (nullable = false)
    private String country;

    @Min(0)
    private Integer playerCount = 20;

    @Min(0)
    private Integer budget = 5_000_000;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Player> players = new ArrayList<>();

    public void removePlayer(Player player) {
        players.remove(player);
        player.setTeam(null);
    }

    public int getTeamValue() {
        int teamValue = 0;
        for (Player player : players) {
            teamValue += player.getValue();
        }
        return teamValue;
    }

}
