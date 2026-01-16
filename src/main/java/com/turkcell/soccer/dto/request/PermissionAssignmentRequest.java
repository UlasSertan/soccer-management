package com.turkcell.soccer.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermissionAssignmentRequest {

    @NotBlank
    private String role;
    @NotBlank
    private String permission;
}
