package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.TeamStats;
import com.turkcell.soccer.model.Player;
import com.turkcell.soccer.model.Team;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
@Slf4j
public class MatchSimulationService {

    private final TeamService teamService;
    private static final double HOME_ADVANTAGE = 1.10;
    private enum Position { GK, DEF, MID, FWD }

    @Autowired
    public MatchSimulationService(TeamService teamService) {
        this.teamService = teamService;
    }
    public record MatchResult(String homeTeam, String awayTeam, int homeScore, int awayScore) {
        @Override
        @NotNull
        public String toString() {
            return homeTeam + " " + homeScore + " - " + awayScore + " " + awayTeam;
        }
    }

    public MatchResult playMatch(Long homeTeamId, Long awayTeamId) {
        Team home = teamService.getTeamById(homeTeamId);
        Team away = teamService.getTeamById(awayTeamId);

        TeamStats homeStats = calculateTeamStats(home);
        TeamStats awayStats = calculateTeamStats(away);

        // Midfield effect
        double homeEffectiveOffense = homeStats.totalOffense - (awayStats.midfieldControl * 0.5);
        double awayEffectiveOffense = awayStats.totalOffense - (homeStats.midfieldControl * 0.5);

        // Home field advantage
        homeEffectiveOffense *= HOME_ADVANTAGE;

        // Goal calculation
        int homeGoals = calculateGoals(homeEffectiveOffense, awayStats.totalDefense);
        int awayGoals = calculateGoals(awayEffectiveOffense, homeStats.totalDefense);

        return new MatchResult(home.getName(), away.getName(), homeGoals, awayGoals);
    }

    private TeamStats calculateTeamStats(Team team) {
        TeamStats stats = new TeamStats();

        for (Player p : autoSelectBestXI(team)) {
            double performance = calculatePlayerMatchPerformance(p);

            Position pos = determinePosition(p);

            switch (pos) {
                case FWD:
                    stats.totalOffense += performance; // %100 Offense
                    break;
                case MID:
                    stats.totalOffense += performance * 0.5; // %50 Offense
                    stats.totalDefense += performance * 0.25; // %25 Defense
                    stats.midfieldControl += performance; // %100 Midfield control
                    break;
                case DEF:
                    stats.totalDefense += performance; // %100 Defense
                    break;
                case GK:
                    stats.totalDefense += performance * 1.5; // Goalkeeper bonus
                    break;
            }
        }
        return stats;
    }


    private List<Player> autoSelectBestXI(Team team) {
        return autoSelectBestXI(team, 4, 4, 2);
    }


    private List<Player> autoSelectBestXI(Team team, int n_def, int n_mid, int n_for) {

        List<Player> GK = getBestPlayersByPos(team, "Goalkeeper", 1);
        List<Player> DEF = getBestPlayersByPos(team, "Defender", n_def);
        List<Player> MID = getBestPlayersByPos(team, "Midfielder", n_mid);
        List<Player> FWD = getBestPlayersByPos(team, "Forward", n_for);

        List<Player> XI = new ArrayList<>();
        XI.addAll(GK);
        XI.addAll(DEF);
        XI.addAll(MID);
        XI.addAll(FWD);

        if (XI.size() < 11) {
            log.warn("Team {} is fielding only {} players!", team.getName(), XI.size());
        }

        return XI;
    }

    private List<Player> getBestPlayersByPos(Team team, String position, int limit) {
        return team.getPlayers().stream()
                .filter(p -> p.getPosition() != null && p.getPosition().equalsIgnoreCase(position))
                .sorted((p1, p2) -> Double.compare(
                        calculateBasePower(p2),
                        calculateBasePower(p1)))
                .limit(limit)
                .toList();
    }

    private double calculateBasePower(Player p) {
        double currentValue = (p.getValue() != null && p.getValue() > 0) ? p.getValue() : 1000000;
        double basePower = 50 + (20 * Math.log10(currentValue / 1_000_000.0));
        if (basePower < 10) basePower = 10;

        double ageFactor = getAgeFactor(p.getAge());

        return basePower * ageFactor;
    }

    private double calculatePlayerMatchPerformance(Player p) {
        // Value based power calculation
        double base = calculateBasePower(p);

        Random r = new Random();
        double luckFactor = 0.90 + (r.nextDouble() * 0.20); // 0.90 - 1.10

        return base * luckFactor;
    }

    private double getAgeFactor(int age) {
        if (age < 21) return 0.90;

        if (age <= 29) return 1.05;

        return 0.95;
    }

    // Calculates xG
    private int calculateGoals(double offensePower, double defensePower) {

        double ratio = offensePower*1.5 / (defensePower + 1);

        double lambda = 1.6 * ratio;
        Random r = new Random();
        double matchTempo = 0.85 + (r.nextDouble() * 0.35);

        return getPoisson(lambda * matchTempo);
    }

    // Statistical goal distribution
    private int getPoisson(double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;
        Random rng = new Random();
        do {
            k++;
            p *= rng.nextDouble();
        } while (p > L);
        return k - 1;
    }
    private Position determinePosition(Player p) {
        String pos = p.getPosition();
        if (pos == null) return Position.MID; // Default

        return switch (pos.toLowerCase(Locale.ENGLISH)) {
            case "goalkeeper" -> Position.GK;
            case "defender" -> Position.DEF;
            case "midfielder" -> Position.MID;
            case "forward" -> Position.FWD;
            default -> Position.MID;
        };

    }


}
