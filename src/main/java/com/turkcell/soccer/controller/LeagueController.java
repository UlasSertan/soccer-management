package com.turkcell.soccer.controller;

import com.turkcell.soccer.dto.LeagueDto;
import com.turkcell.soccer.dto.LeagueStandingsDto;
import com.turkcell.soccer.dto.PlayerStandingsDto;
import com.turkcell.soccer.dto.request.LeagueRequest;
import com.turkcell.soccer.dto.Match;
import com.turkcell.soccer.service.LeagueService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{leagueId}")
    public ResponseEntity<List<LeagueStandingsDto>> getLeagueResults(@PathVariable Long leagueId) {
        return ResponseEntity.ok().body(leagueService.getFinalResults(leagueId));
    }

    @GetMapping("/match-results/{leagueId}")
    public ResponseEntity<List<Match>> getPlayerResults(@PathVariable Long leagueId) {
        return ResponseEntity.ok().body(leagueService.getFinalMatches(leagueId));
    }
    @GetMapping("/match-results/{leagueId}/{week}")
    public ResponseEntity<List<Match>> getMatchesWeek(@PathVariable Long leagueId, @PathVariable Integer week) {
        return ResponseEntity.ok().body(leagueService.getWeekMatches(leagueId, week));
    }

    @GetMapping("/goal-table/{leagueId}")
    public ResponseEntity<List<PlayerStandingsDto>> getTopScorers(@PathVariable Long leagueId) {
        return ResponseEntity.ok().body(leagueService.getTopScorers(leagueId));
    }

    @GetMapping("/assist-table/{leagueId}")
    public ResponseEntity<List<PlayerStandingsDto>> getTopAssisters(@PathVariable Long leagueId) {
        return ResponseEntity.ok().body(leagueService.getTopAssisters(leagueId));
    }

    @PatchMapping("/reset/{leagueId}")
    public ResponseEntity<Void> resetLeague(@PathVariable Long leagueId) {
        leagueService.resetLeague(leagueId);
        return ResponseEntity.ok().build();
    }
}
