package com.turkcell.soccer.dto.request;

import com.turkcell.soccer.model.Tactic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TacticCreationRequest {

    private int defenders;
    private int midfielders;
    private int forwards;
    private Tactic.TacticStyle style;

}
