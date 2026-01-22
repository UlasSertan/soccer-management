package com.turkcell.soccer.dto.request;

import com.turkcell.soccer.model.Team;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerRequest {

    @Pattern(regexp = "^[A-Za-z]+$", message = "First name must contain only alphabetic characters.")
    private String firstName;

    @Pattern(regexp = "^[A-Za-z]+$", message = "Last name must contain only alphabetic characters.")
    private String lastName;

    @Pattern(regexp = "^[A-Za-z]+$", message = "Country must contain only alphabetic characters.")
    private String country;

    // nullable, but if present must be >= 0
    @Min(value = 0, message = "Value must be a positive number or zero.")
    private Integer value;

    @Min(value = 0, message = "Age must be a positive number or zero.")
    private Integer age;

    @Pattern(regexp = "^[A-Za-z]+$", message = "Position must contain only alphabetic characters.")
    private String position;

    @Pattern(regexp = "^[A-Za-z]+$", message = "Team must contain only alphabetic characters.")
    private String team;


}
