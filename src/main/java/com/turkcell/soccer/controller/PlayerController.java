package com.turkcell.soccer.controller;

import com.turkcell.soccer.dto.PlayerDto;
import com.turkcell.soccer.dto.request.PlayerRequest;
import com.turkcell.soccer.dto.response.PlayerResponse;
import com.turkcell.soccer.service.PlayerService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts/teams/players")
@Tag(name = "Player", description = "Player management APIs")
public class PlayerController {

    private final PlayerService playerService;

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ResponseEntity<List<PlayerDto>> getAllPlayers() {
        return ResponseEntity.ok(playerService.getAllPlayers());
    }

    // id -
    @GetMapping("/{id}")
    public ResponseEntity<PlayerDto> getPlayer(@PathVariable Long id) {
        return ResponseEntity.ok(playerService.getPlayer(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody PlayerRequest PlayerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(playerService.createPlayer(PlayerRequest));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Custom exception for authorization
    public ResponseEntity<PlayerResponse> updatePlayer(@PathVariable Long id,
                                                           @Valid @RequestBody PlayerRequest PlayerRequest) {
        return ResponseEntity.ok(playerService.updatePlayer(id, PlayerRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }



}
