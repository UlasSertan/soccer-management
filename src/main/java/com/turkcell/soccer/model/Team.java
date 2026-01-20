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

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "team", cascade = CascadeType.ALL)
    private List<Player> players = new ArrayList<>();

    public int getTeamValue() {
        int teamValue = 0;
        for (Player player : players) {
            teamValue += player.getValue();
        }
        return teamValue;
    }

}
