package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.LeagueDto;
import com.turkcell.soccer.dto.MatchResult;
import com.turkcell.soccer.dto.request.LeagueRequest;
import com.turkcell.soccer.mapper.LeagueMapper;
import com.turkcell.soccer.model.*;
import com.turkcell.soccer.repository.LeagueRepository;
import com.turkcell.soccer.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final TeamService teamService;
    private final MatchSimulationService matchSimulationService;
    private final TeamRepository teamRepository;
    private final LeagueMapper leagueMapper;

    @Autowired
    public LeagueService(LeagueRepository leagueRepository, TeamService teamService,
                         MatchSimulationService matchSimulationService, TeamRepository teamRepository,
                         LeagueMapper leagueMapper) {
        this.leagueRepository = leagueRepository;
        this.teamService = teamService;
        this.matchSimulationService = matchSimulationService;
        this.teamRepository = teamRepository;
        this.leagueMapper = leagueMapper;

    }

    @Transactional
    public LeagueDto createLeague(LeagueRequest request) {
        League league = new League();

        List<Team> teams = new ArrayList<>();
        if (request.getTeamIds() != null && !request.getTeamIds().isEmpty()) {
            teams = teamRepository.findAllById(request.getTeamIds());

            if (teams.size() != request.getTeamIds().size()) {
                throw new NoSuchElementException("All teams are not in the database");
            }
        }
        league.setTeams(teams);

        List<LeagueStandings> initialStandings = new ArrayList<>();
        for (Team team : teams) {
            LeagueStandings standing = new LeagueStandings();
            standing.setLeague(league);
            standing.setTeam(team);
            initialStandings.add(standing);
        }

        league.setLeagueStandings(initialStandings);
        league.setPlayerStandings(new ArrayList<>());

        return leagueMapper.leagueToLeagueDto(leagueRepository.save(league));
    }

    @Transactional
    public void simulateSeason(Long leagueId) {
        League league = getLeagueById(leagueId);
        List<LeagueStandings> standings = league.getLeagueStandings();
        List<Team> teams = league.getTeams();

        for (int i = 0; i < teams.size(); i++) {
            for  (int j = i; j < teams.size(); j++) {
                if (i == j) {continue;}
                Team team1 = teams.get(i);
                Team team2 = teams.get(j);
                MatchResult result = matchSimulationService.playMatch(team1.getId(), team2.getId());
                updateTeamStandings(standings, result);
                result = matchSimulationService.playMatch(team2.getId(), team1.getId());
                updateTeamStandings(standings, result);
            }
        }
    }

    private void updateTeamStandings(List<LeagueStandings> standings, MatchResult matchResult) {
        int result = 1; // 0 -> home win, 1 -> draw, 2 -> away win
        if (matchResult.homeScore() > matchResult.awayScore()) {result = 0;}
        if (matchResult.awayScore() > matchResult.homeScore()) {result = 2;}

        LeagueStandings homeStanding = null;
        LeagueStandings awayStanding = null;
        for (LeagueStandings standing : standings) {
            if(standing.getTeam().getId().equals(matchResult.homeTeamId()))
                homeStanding = standing;
            if (standing.getTeam().getId().equals(matchResult.awayTeamId()))
                awayStanding = standing;
        }

        if (awayStanding == null || homeStanding == null) {
            log.warn("One of the teams are not in the standings for team ids: {} - {}",
                    matchResult.homeTeamId(), matchResult.awayTeamId());
            throw new NoSuchElementException("One of the teams are not in the standings for team");
        }
        int homeScored = matchResult.homeScore();
        int awayScored = matchResult.awayScore();

        homeStanding.setPlayed(homeStanding.getPlayed() + 1);
        awayStanding.setPlayed(awayStanding.getPlayed() + 1);

        homeStanding.incrementGoalsScored(homeScored);
        awayStanding.incrementGoalsScored(awayScored);
        homeStanding.incrementGoalsConceded(awayScored);
        awayStanding.incrementGoalsConceded(homeScored);

        switch (result) {
            case 0:
                homeStanding.setWins(homeStanding.getWins() + 1);
                awayStanding.setLosses(awayStanding.getLosses() + 1);

                break;
            case 1:
                homeStanding.setDraws(homeStanding.getDraws() + 1);
                awayStanding.setDraws(awayStanding.getDraws() + 1);
                break;
            case 2:
                homeStanding.setLosses(homeStanding.getLosses() + 1);
                awayStanding.setWins(awayStanding.getWins() + 1);
                break;
        }
    }




    private League getLeagueById(Long leagueId) {
        League league = leagueRepository.findById(leagueId).orElse(null);
        if (league == null) {
            log.warn("League with id {} not found", leagueId);
            throw new NoSuchElementException("League with id " + leagueId + " not found");
        }
        return league;
    }

}
