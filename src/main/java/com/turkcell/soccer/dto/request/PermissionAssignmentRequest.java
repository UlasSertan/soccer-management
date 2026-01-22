package com.turkcell.soccer.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionAssignmentRequest {

    @NotBlank(message = "Role is required.")
    @Size(min = 2, max = 30, message = "Role must be between 2 and 30 characters.")
    @Pattern(
            regexp = "^[A-Za-z]+$",
            message = "Role must contain only alphabetic characters."
    )
    private String role;

    @NotBlank(message = "Permission is required.")
    @Size(min = 2, max = 30, message = "Permission must be between 2 and 30 characters.")
    @Pattern(
            regexp = "^[A-Za-z]+$",
            message = "Permission must contain only alphabetic characters."
    )
    private String permission;
}
