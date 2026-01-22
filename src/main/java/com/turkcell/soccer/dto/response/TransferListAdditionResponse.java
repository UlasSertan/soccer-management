package com.turkcell.soccer.dto.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferListAdditionResponse {

    @NotNull
    private Integer price;
    @NotNull
    private Long playerId;
    @NotNull
    private LocalDateTime timeStamp;
}
