package com.turkcell.soccer.controller;

import com.turkcell.soccer.dto.LeagueDto;
import com.turkcell.soccer.dto.request.LeagueRequest;
import com.turkcell.soccer.service.LeagueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leagues")
public class LeagueController {

    private final LeagueService leagueService;

    @Autowired
    public LeagueController(LeagueService leagueService) {
        this.leagueService = leagueService;
    }

    @PostMapping
    public ResponseEntity<LeagueDto> createLeague(@RequestBody @Valid LeagueRequest request) {
        return ResponseEntity.ok().body(leagueService.createLeague(request));
    }

    @PostMapping("/{leagueId}/simulate")
    public ResponseEntity<Void> simulateSeason(@PathVariable Long leagueId) {
        leagueService.simulateSeason(leagueId);
        return ResponseEntity.ok().build();

    }
}
