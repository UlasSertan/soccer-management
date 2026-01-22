package com.turkcell.soccer.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequest {

    @NotNull(message = "Player id is required.")
    @Min(value = 1, message = "Player id must be a positive number.")
    private Long playerId;

    @NotNull(message = "Price is required.")
    @Min(value = 0, message = "Price must be a positive number or zero.")
    private Integer price;

}
