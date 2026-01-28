package com.turkcell.soccer.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor


public class Tactic {


    private int defenders = 4;
    private int midfielders = 4;
    private int forwards = 2;
    @Enumerated(EnumType.STRING)
    private TacticStyle style = TacticStyle.BALANCED;

    public enum TacticStyle {
        ATTACKING,
        BALANCED,
        DEFENSIVE
    }




}


