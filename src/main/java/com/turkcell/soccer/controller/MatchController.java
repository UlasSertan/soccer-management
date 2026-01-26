package com.turkcell.soccer.controller;

import com.turkcell.soccer.service.MatchSimulationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friendly-matches")
public class MatchController {

    private final MatchSimulationService matchSimulationService;

    @Autowired
    public MatchController(MatchSimulationService matchSimulationService) {
        this.matchSimulationService = matchSimulationService;
    }

    @PostMapping("/{homeTeamId}/{awayTeamId}")
    public ResponseEntity<MatchSimulationService.MatchResult> playGame(@PathVariable Long homeTeamId,
                                                                       @PathVariable Long awayTeamId) {

        return ResponseEntity.ok().body(matchSimulationService.playMatch(homeTeamId, awayTeamId));

    }

}
