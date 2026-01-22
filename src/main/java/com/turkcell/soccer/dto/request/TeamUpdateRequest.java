package com.turkcell.soccer.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeamUpdateRequest {

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
}
