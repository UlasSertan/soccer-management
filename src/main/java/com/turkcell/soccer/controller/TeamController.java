package com.turkcell.soccer.controller;


import com.turkcell.soccer.docs.TeamControllerDocs;
import com.turkcell.soccer.dto.request.AdminTeamUpdateRequest;
import com.turkcell.soccer.dto.request.TeamRequest;
import com.turkcell.soccer.dto.request.TeamUpdateRequest;
import com.turkcell.soccer.dto.response.*;
import com.turkcell.soccer.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts/teams")
public class TeamController implements TeamControllerDocs {



    private final TeamService teamService;

    @Autowired
    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }


    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest teamRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(teamService.createTeam(teamRequest));
    }

    @GetMapping
    public ResponseEntity<TeamInfoResponse> getTeamInfo() {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeamInfo());
    }

    @GetMapping("/all")
    public ResponseEntity<List<TeamInfoResponse>> getAllTeams() {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getAllTeamsInfo());
    }

    @PatchMapping
    public ResponseEntity<TeamUpdateResponse>  updateTeam(@Valid @RequestBody TeamUpdateRequest teamRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.updateTeam(teamRequest));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteTeam() {
        teamService.deleteTeam();
        return ResponseEntity.noContent().build();
    }

    // id -
    @GetMapping("/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminTeamResponse> getTeam(@PathVariable Long teamId) {
        return ResponseEntity.status(HttpStatus.OK).body(teamService.getTeamInfo(teamId));
    }

    @PatchMapping("/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminTeamResponse> updateTeam(
            @PathVariable Long teamId,
            @Valid @RequestBody AdminTeamUpdateRequest updateRequest) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                teamService.updateTeam(teamId, updateRequest)
        );
    }

    @DeleteMapping("/{teamId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long teamId) {
        teamService.deleteTeam(teamId);
        return ResponseEntity.noContent().build();
    }





}
