package com.turkcell.soccer.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfferCreationRequest {

    @NotNull(message = "Id is required.")
    @Min(value = 1, message = "Id must be a positive number.")
    private Long sellerTeamId;
    @NotNull(message = "Id is required.")
    @Min(value = 1, message = "Id must be a positive number.")
    private Long playerId;
    @NotNull(message = "Price is required.")
    @Min(value = 0, message = "Price must be a positive number or zero.")
    private Integer offeredPrice;
}
