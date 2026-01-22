package com.turkcell.soccer.dto.response;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PurchaseResponse {

    @NotNull
    private Long purchaseId;
    @NotNull
    private Long playerId;
    @NotNull
    private Long sellerId;
    @NotNull
    private Long buyerId;
    @NotNull
    private Integer price;
    @NotNull
    private LocalDateTime createdAt;

}
