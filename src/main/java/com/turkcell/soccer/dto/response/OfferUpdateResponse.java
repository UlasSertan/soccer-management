package com.turkcell.soccer.dto.response;

import com.turkcell.soccer.model.Offer;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class OfferUpdateResponse {


    @NotNull(message = "Id is required.")
    @Min(value = 1, message = "Id must be a positive number.")
    private Long offerId;
    @NotNull(message = "Id is required.")
    @Min(value = 1, message = "Id must be a positive number.")
    private Long sellerTeamId;
    @NotNull(message = "Id is required.")
    @Min(value = 1, message = "Id must be a positive number.")
    private Long buyerTeamId;
    @NotNull(message = "Id is required.")
    @Min(value = 1, message = "Id must be a positive number.")
    private Long playerId;
    @NotNull(message = "Price is required.")
    @Min(value = 0, message = "Price must be a positive number or zero.")
    private Integer offeredPrice;
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private Offer.OfferStatus status;

}
