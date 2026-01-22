package com.turkcell.soccer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferListFilter {

    private String teamName;
    private String country;
    private Integer maxPrice;
    private Integer minPrice;
    private String playerName;
}
