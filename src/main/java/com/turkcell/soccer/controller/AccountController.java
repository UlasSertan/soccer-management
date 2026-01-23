package com.turkcell.soccer.controller;

import com.turkcell.soccer.annotation.RateLimit;
import com.turkcell.soccer.dto.request.AccountRequest;
import com.turkcell.soccer.dto.request.AccountUpdateRequest;
import com.turkcell.soccer.dto.response.AccountResponse;
import com.turkcell.soccer.dto.request.PermissionAssignmentRequest;
import com.turkcell.soccer.service.AccountService;
import com.turkcell.soccer.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Account", description = "Account management APIs")
public class AccountController {

    private final AccountService accountService;
    private final RoleService roleService;

    @Autowired
    public AccountController(AccountService accountService, RoleService roleService) {
        this.accountService = accountService;
        this.roleService = roleService;
    }

    @Operation(
            summary = "Create a new account",
            description = "Creates a new account with username, email and password. Username and email must be unique."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(schema = @Schema(implementation = AccountResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request - Username or email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    @RateLimit(capacity = 20, timeInSeconds = 60)
    public ResponseEntity<?> createAccount(@Valid @RequestBody AccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{name}")
    @PreAuthorize("@accountSecurity.canDeleteAccount(#name)")
    @RateLimit(capacity = 10, timeInSeconds = 60)
    public ResponseEntity<?> deleteAccount(@PathVariable String name) {
        accountService.deleteAccount(name);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimit(capacity = 20, timeInSeconds = 60)
    public ResponseEntity<?> grantPermission(@Valid @RequestBody PermissionAssignmentRequest permissionAssignmentRequest) {
        roleService.assignPermissions(
                permissionAssignmentRequest.getRole(),
                permissionAssignmentRequest.getPermission()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(permissionAssignmentRequest);
    }

    @PatchMapping("/{name}")
    @PreAuthorize("@accountSecurity.canUpdateAccount(#name)")
    @RateLimit(capacity = 20, timeInSeconds = 60)
    public ResponseEntity<?> updateAccount(@Valid @RequestBody AccountUpdateRequest request, @PathVariable String name) {
        return ResponseEntity.ok().body(accountService.updateAccount(request, name));
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getAccountInfo(@PathVariable String name) {
        return ResponseEntity.ok().body(accountService.getAccountInfo(name));
    }


    // Inner class for error response
    private static class ErrorResponse {
        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
