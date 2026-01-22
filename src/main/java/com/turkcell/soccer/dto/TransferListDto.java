package com.turkcell.soccer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferListDto {

    private Long id;
    private PlayerDto player;
    private Integer price;

}
