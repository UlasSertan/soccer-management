package com.turkcell.soccer.dto;

import com.turkcell.soccer.model.Tactic;
import com.turkcell.soccer.model.Team;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TacticDto {

    private Long id;
    private int defenders;
    private int midfielders;
    private int forwards;
    private Tactic.TacticStyle style;
    List<Long> teamsIds;

}
