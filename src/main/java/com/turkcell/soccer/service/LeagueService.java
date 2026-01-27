package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.LeagueDto;
import com.turkcell.soccer.dto.LeagueStandingsDto;
import com.turkcell.soccer.dto.MatchResult;
import com.turkcell.soccer.dto.PlayerStandingsDto;
import com.turkcell.soccer.dto.request.LeagueRequest;
import com.turkcell.soccer.mapper.LeagueMapper;
import com.turkcell.soccer.mapper.MatchMapper;
import com.turkcell.soccer.model.*;
import com.turkcell.soccer.dto.Match;
import com.turkcell.soccer.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

@Service
@Slf4j
@RequiredArgsConstructor
public class LeagueService {

    private final LeagueRepository leagueRepository;
    private final TeamService teamService;
    private final MatchSimulationService matchSimulationService;
    private final TeamRepository teamRepository;
    private final LeagueMapper leagueMapper;
    private final LeagueStandingsRepository leagueStandingsRepository;
    private final PlayerStandingsRepository playerStandingsRepository;
    private final MatchMapper matchMapper;
    private final MatchRepository matchRepository;



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
        List<PlayerStandings> allPlayerStandings = new ArrayList<>();
        List<Team> validTeams = new ArrayList<>();
        for (Team team : teams) {


            if (team.getPlayers() == null || team.getPlayers().isEmpty() ||
                team.getPlayers().size() < 11) {
                log.warn("Team {} with ID:{} has not enough eligible players! This team will not be in the league!",
                        team.getName(),  team.getId());
                continue;
            }
            validTeams.add(team);

            LeagueStandings standing = new LeagueStandings();
            standing.setLeague(league);
            standing.setTeam(team);
            initialStandings.add(standing);

            for (Player player : team.getPlayers()) {
                PlayerStandings playerStandings = new PlayerStandings();
                playerStandings.setPlayer(player);
                playerStandings.setLeague(league);
                playerStandings.setGoals(0);
                playerStandings.setAssists(0);
                allPlayerStandings.add(playerStandings);
            }

        }
        league.setTeams(validTeams);
        league.setLeagueStandings(initialStandings);
        league.setPlayerStandings(allPlayerStandings);

        return leagueMapper.leagueToLeagueDto(leagueRepository.save(league));
    }

    @Transactional
    public void simulateSeason(Long leagueId) {
        League league = getLeagueById(leagueId);
        List<LeagueStandings> standings = league.getLeagueStandings();
        List<Team> participants = league.getTeams();

        if (participants.size() % 2 != 0) {
            log.error("Odd number of teams not supported in MVP. League ID: {}", leagueId);
            throw new RuntimeException("Number of teams is not even");
        }
        List<Team> teams = new ArrayList<>(participants);

        int week = 1;
        int half = teams.size() / 2;

        for (week = 1; week <= teams.size()-1; week++) {

            for (int i = 0 ; i < half; i++) {

                Team team1 = teams.get(i);
                Team team2 = teams.get(teams.size()-i-1);

                playMatch(team1, team2, leagueId, standings, league, week, week + teams.size()-1);
            }

            Team last = teams.removeLast();
            teams.add(1,  last);

        }
    }

    private void playMatch(Team team1, Team team2, Long leagueId, List<LeagueStandings> standings,
                           League league, int week, int secondRoundWeek) {

        MatchResult result = matchSimulationService.playMatch(team1.getId(), team2.getId());

        saveMatch(result, leagueId, week);
        updateTeamStandings(standings, result);
        updatePlayerStandings(league, result);


        result = matchSimulationService.playMatch(team2.getId(), team1.getId());

        saveMatch(result, leagueId, secondRoundWeek);
        updateTeamStandings(standings, result);
        updatePlayerStandings(league, result);
    }

    private void saveMatch(MatchResult result, Long leagueId, int week) {
        Match matchEntity = matchMapper.toEntity(result);

        matchEntity.setLeagueId(leagueId);
        matchEntity.setWeek(week);
        matchEntity.setPlayed(true);

        matchRepository.save(matchEntity);
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

    @Transactional
    public List<Match> getFinalMatches(Long leagueId) {
        return matchRepository.findByLeagueId(leagueId);
    }
    @Transactional
    public List<Match> getWeekMatches(Long leagueId, Integer week) {
        return matchRepository.findByLeagueIdAndWeek(leagueId, week);
    }

    @Transactional
    public List<LeagueStandingsDto> getFinalResults(Long leagueId) {
        return leagueMapper.toLeagueStandingsDtoList(leagueStandingsRepository.findFinalTable(leagueId));
    }

    @Transactional
    public List<PlayerStandingsDto> getTopScorers(Long leagueId) {
        List<PlayerStandings> scorers = playerStandingsRepository.findGoalTable(leagueId);
        return leagueMapper.toPlayerStandingsDtoList(scorers);
    }

    @Transactional
    public List<PlayerStandingsDto> getTopAssisters(Long leagueId) {
        List<PlayerStandings> assisters = playerStandingsRepository.findAssistTable(leagueId);
        return leagueMapper.toPlayerStandingsDtoList(assisters);
    }

    @Transactional
    public void resetLeague(Long leagueId) {
        League league = getLeagueById(leagueId);

        for (LeagueStandings s : league.getLeagueStandings()) {
            s.setPlayed(0);
            s.setWins(0);
            s.setDraws(0);
            s.setLosses(0);
            s.setGoalsScored(0);
            s.setGoalsConceded(0);
        }

        for (PlayerStandings p : league.getPlayerStandings()) {
            p.setGoals(0);
            p.setAssists(0);
        }
    }

    private void updatePlayerStandings(League league, MatchResult matchResult) {
        List<PlayerStandings> standings = league.getPlayerStandings();
        double assistProb = 0.7;
        Team homeTeam = teamService.getTeamById(matchResult.homeTeamId());
        Team awayTeam = teamService.getTeamById(matchResult.awayTeamId());
        int homeScore = matchResult.homeScore();
        int awayScore = matchResult.awayScore();

        updatePlayersOfTeam(homeTeam, homeScore, standings,  assistProb);
        updatePlayersOfTeam(awayTeam, awayScore, standings, assistProb);


    }


    private void updatePlayersOfTeam(Team team, int goalsScored, List<PlayerStandings> standings,
                                     double assistProb) {
        Random generator = new Random();

        if (team.getPlayers() == null || team.getPlayers().isEmpty()) {
            log.warn("Team {} has no players! Goals will not be assigned.", team.getName());
            return;
        }

        for (int i = 0; i < goalsScored; i++) {
            Player playerScored = distributeAmongPosition(team.getPlayers(), 0.7, 0.25);
            PlayerStandings playerScoredStanding = standings.stream().
                    filter(p -> p.getPlayer().getId().equals(playerScored.getId()))
                    .findFirst()
                    .orElseThrow();

            playerScoredStanding.setGoals(playerScoredStanding.getGoals() + 1);
            if(generator.nextDouble() <= assistProb) {
                Player playerAssisted = distributeAmongPosition(team.getPlayers(), 0.2, 0.7);


                if (!playerAssisted.getId().equals(playerScored.getId())){
                    PlayerStandings playerAssistedStandings = standings.stream().
                            filter(p -> p.getPlayer().getId().equals(playerAssisted.getId()))
                            .findFirst()
                            .orElseThrow();
                    playerAssistedStandings.setAssists(playerAssistedStandings.getAssists() + 1);
                }

            }
        }
    }

    private Player distributeAmongPosition(List<Player> players, double f, double m) {
        Random generator = new Random();
        double randomNumber = generator.nextDouble();

        List<Player> selectedPool;

        if (randomNumber <= f) {
            selectedPool = getPlayersByPosition(players, "Forward");
            if (selectedPool.isEmpty()) {
                selectedPool = getPlayersByPosition(players, "Midfielder");
            }
        } else if (randomNumber <= f + m) {
            selectedPool = getPlayersByPosition(players, "Midfielder");
        } else {
            selectedPool = getPlayersByPosition(players, "Defender");
        }

        // IF Still empty get the whole team
        if (selectedPool.isEmpty()) {
            selectedPool = new ArrayList<>(players);
        }
        List<Double> cumulativeProbabilities = calculateCumulativeProbabilities(selectedPool);

        return selectPlayer(selectedPool, cumulativeProbabilities);
    }



    private List<Double> calculateCumulativeProbabilities(List<Player> pool) {

        double totalValue = pool.stream()
                .mapToDouble(p -> (p.getValue() != null ? p.getValue() : 0))
                .sum();

        List<Double> probabilities = new ArrayList<>();
        double runningSum = 0;

        for (Player p : pool) {
            double val = (p.getValue() != null ? p.getValue() : 0);
            runningSum += (val / totalValue);
            probabilities.add(runningSum);
        }

        return probabilities;
    }

    // Probabilities are cumulative starting from the best player
    private Player selectPlayer(List<Player> players, List<Double> probabilities) {

        Random generator = new Random();
        double value = generator.nextDouble();
        int index = 0;
        for (int i  = 0; i < probabilities.size(); i++) {
            if (probabilities.get(i) >= value) {return players.get(i);}
        }
        return players.get(index);

    }

    private List<Player> getPlayersByPosition(List<Player> allPlayers, String position) {
        return allPlayers.stream()
                .filter(p -> p.getPosition() != null &&
                        p.getPosition().toUpperCase().contains(position.toUpperCase()))
                .toList();
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
