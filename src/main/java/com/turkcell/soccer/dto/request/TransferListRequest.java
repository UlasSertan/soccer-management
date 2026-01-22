package com.turkcell.soccer.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class TransferListRequest {

    // Main request for creating

    @Data
    public static class Add {

        @NotNull(message = "Player id is required.")
        @Min(value = 1, message = "Player id must be a positive number.")
        private Long playerId;

        @NotNull(message = "Price is required.")
        @Min(value = 0, message = "Price must be a positive number or zero.")
        private Integer price;
    }

    // Small request just for patching
    @Data
    public static class UpdatePrice {

        @NotNull(message = "Price is required.")
        @Min(value = 0, message = "Price must be a positive number or zero.")
        private Integer price;
    }

}
