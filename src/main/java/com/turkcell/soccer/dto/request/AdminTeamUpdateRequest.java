package com.turkcell.soccer.dto.request;

import com.turkcell.soccer.dto.PlayerDto;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminTeamUpdateRequest {

    private Long id;
    private String name;
    private String country;
    private Integer budget;

}
