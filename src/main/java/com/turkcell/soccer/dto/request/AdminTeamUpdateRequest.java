package com.turkcell.soccer.dto.request;

import jakarta.validation.constraints.*;
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

    @NotNull(message = "Id is required.")
    @Min(value = 1, message = "Id must be a positive number.")
    private Long id;

    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters.")
    @Pattern(
            regexp = "^[A-Za-z]+$",
            message = "Name must contain only alphabetic characters."
    )
    private String name;

    @Size(min = 2, max = 50, message = "Country must be between 2 and 50 characters.")
    @Pattern(
            regexp = "^[A-Za-z]+$",
            message = "Country must contain only alphabetic characters."
    )
    private String country;

    @Min(value = 0, message = "Budget must be a positive number or zero.")
    private Integer budget;

}
