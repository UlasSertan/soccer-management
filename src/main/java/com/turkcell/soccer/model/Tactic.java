package com.turkcell.soccer.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tactics", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"defenders", "midfielders", "forwards", "style"})
})
public class Tactic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private int defenders = 4;
    private int midfielders = 4;
    private int forwards = 2;
    @Enumerated(EnumType.STRING)
    private TacticStyle style = TacticStyle.BALANCED;
    @OneToMany(mappedBy = "tactic", fetch = FetchType.EAGER)
    List<Team> teams = new ArrayList<>();
    public enum TacticStyle {
        ATTACKING,
        BALANCED,
        DEFENSIVE
    }

    public boolean isInUse() {
        return  teams != null && !teams.isEmpty() ;
    }


}


