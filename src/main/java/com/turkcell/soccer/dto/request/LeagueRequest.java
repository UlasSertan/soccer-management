package com.turkcell.soccer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeagueRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    String name;

    List<Long> teamIds;
}
