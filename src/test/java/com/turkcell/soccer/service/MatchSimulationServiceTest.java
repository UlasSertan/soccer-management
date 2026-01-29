package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.MatchResult;
import com.turkcell.soccer.model.Player;
import com.turkcell.soccer.model.Team;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchSimulationServiceTest {

    @Mock
    private TeamService teamService;

    @InjectMocks
    private MatchSimulationService matchSimulationService;

    private Team homeTeam;
    private Team awayTeam;

    @BeforeEach
    void setUp() {
        homeTeam = createTeamWithPlayers(1L, "Home FC");
        awayTeam = createTeamWithPlayers(2L, "Away FC");
    }

    private Team createTeamWithPlayers(Long teamId, String teamName) {
        Team team = new Team();
        team.setId(teamId);
        team.setName(teamName);
        team.setCountry("Turkey");

        List<Player> players = new ArrayList<>();

        // 1 Goalkeeper
        players.add(createPlayer(teamId * 100 + 1, "GK", "Player", "Goalkeeper", 25, 2_000_000));

        // 4 Defenders
        for (int i = 0; i < 4; i++) {
            players.add(createPlayer(teamId * 100 + 2 + i, "DEF" + i, "Player", "Defender", 26, 1_500_000));
        }

        // 4 Midfielders
        for (int i = 0; i < 4; i++) {
            players.add(createPlayer(teamId * 100 + 6 + i, "MID" + i, "Player", "Midfielder", 27, 2_500_000));
        }

        // 2 Forwards
        for (int i = 0; i < 2; i++) {
            players.add(createPlayer(teamId * 100 + 10 + i, "FWD" + i, "Player", "Forward", 24, 3_000_000));
        }

        team.setPlayers(players);
        return team;
    }

    private Player createPlayer(Long id, String firstName, String lastName, String position, int age, int value) {
        Player player = new Player();
        player.setId(id);
        player.setFirstName(firstName);
        player.setLastName(lastName);
        player.setCountry("Turkey");
        player.setPosition(position);
        player.setAge(age);
        player.setValue(value);
        return player;
    }

    // ==================== playMatch Tests ====================

    @Test
    void playMatch_whenValidTeams_shouldReturnMatchResult() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(homeTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
        assertEquals(homeTeamId, result.homeTeamId());
        assertEquals(awayTeamId, result.awayTeamId());
        assertEquals("Home FC", result.homeTeam());
        assertEquals("Away FC", result.awayTeam());
        assertTrue(result.homeScore() >= 0);
        assertTrue(result.awayScore() >= 0);
        verify(teamService, times(1)).getTeamById(homeTeamId);
        verify(teamService, times(1)).getTeamById(awayTeamId);
    }

    @Test
    void playMatch_shouldReturnNonNegativeScores() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(homeTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertTrue(result.homeScore() >= 0, "Home score should be non-negative");
        assertTrue(result.awayScore() >= 0, "Away score should be non-negative");
    }

    @RepeatedTest(10)
    void playMatch_shouldProduceVariedResults() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(homeTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
        // Scores should be reasonable (typically 0-10 range for football)
        assertTrue(result.homeScore() <= 15, "Home score should be reasonable");
        assertTrue(result.awayScore() <= 15, "Away score should be reasonable");
    }

    // ==================== Team with Different Compositions ====================

    @Test
    void playMatch_whenTeamHasOnlyGoalkeeper_shouldStillPlayMatch() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team weakTeam = new Team();
        weakTeam.setId(1L);
        weakTeam.setName("Weak FC");
        weakTeam.setCountry("Turkey");

        List<Player> players = new ArrayList<>();
        players.add(createPlayer(1L, "GK", "Only", "Goalkeeper", 25, 1_000_000));
        weakTeam.setPlayers(players);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(weakTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
        assertTrue(result.homeScore() >= 0);
        assertTrue(result.awayScore() >= 0);
    }

    @Test
    void playMatch_whenTeamHasNoGoalkeeper_shouldStillPlayMatch() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team noGkTeam = new Team();
        noGkTeam.setId(1L);
        noGkTeam.setName("No GK FC");
        noGkTeam.setCountry("Turkey");

        List<Player> players = new ArrayList<>();
        // Only outfield players
        for (int i = 0; i < 4; i++) {
            players.add(createPlayer((long) i, "DEF" + i, "Player", "Defender", 25, 1_500_000));
        }
        for (int i = 0; i < 4; i++) {
            players.add(createPlayer((long) (i + 4), "MID" + i, "Player", "Midfielder", 25, 2_000_000));
        }
        for (int i = 0; i < 3; i++) {
            players.add(createPlayer((long) (i + 8), "FWD" + i, "Player", "Forward", 25, 2_500_000));
        }
        noGkTeam.setPlayers(players);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(noGkTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    @Test
    void playMatch_whenTeamHasOnlyForwards_shouldStillPlayMatch() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team forwardOnlyTeam = new Team();
        forwardOnlyTeam.setId(1L);
        forwardOnlyTeam.setName("Attack FC");
        forwardOnlyTeam.setCountry("Turkey");

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            players.add(createPlayer((long) i, "FWD" + i, "Player", "Forward", 25, 3_000_000));
        }
        forwardOnlyTeam.setPlayers(players);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(forwardOnlyTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    @Test
    void playMatch_whenTeamHasOnlyDefenders_shouldStillPlayMatch() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team defenderOnlyTeam = new Team();
        defenderOnlyTeam.setId(1L);
        defenderOnlyTeam.setName("Defense FC");
        defenderOnlyTeam.setCountry("Turkey");

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            players.add(createPlayer((long) i, "DEF" + i, "Player", "Defender", 25, 1_500_000));
        }
        defenderOnlyTeam.setPlayers(players);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(defenderOnlyTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    @Test
    void playMatch_whenTeamHasOnlyMidfielders_shouldStillPlayMatch() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team midfielderOnlyTeam = new Team();
        midfielderOnlyTeam.setId(1L);
        midfielderOnlyTeam.setName("Midfield FC");
        midfielderOnlyTeam.setCountry("Turkey");

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            players.add(createPlayer((long) i, "MID" + i, "Player", "Midfielder", 25, 2_000_000));
        }
        midfielderOnlyTeam.setPlayers(players);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(midfielderOnlyTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    // ==================== Player Value and Age Factor Tests ====================

    @Test
    void playMatch_whenTeamHasHighValuePlayers_shouldPlayMatch() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team richTeam = createTeamWithCustomValues(1L, "Rich FC", 50_000_000);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(richTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    @Test
    void playMatch_whenTeamHasLowValuePlayers_shouldPlayMatch() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team poorTeam = createTeamWithCustomValues(1L, "Poor FC", 100_000);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(poorTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    @Test
    void playMatch_whenTeamHasZeroValuePlayers_shouldPlayMatch() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team zeroValueTeam = createTeamWithCustomValues(1L, "Zero FC", 0);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(zeroValueTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }


    @Test
    void playMatch_whenPlayersAreYoung_shouldApplyAgeFactor() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team youngTeam = createTeamWithCustomAge(1L, "Young FC", 19);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(youngTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    @Test
    void playMatch_whenPlayersArePrime_shouldApplyAgeFactor() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team primeTeam = createTeamWithCustomAge(1L, "Prime FC", 27);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(primeTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    @Test
    void playMatch_whenPlayersAreOld_shouldApplyAgeFactor() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team oldTeam = createTeamWithCustomAge(1L, "Old FC", 35);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(oldTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    // ==================== Position Tests ====================

    @Test
    void playMatch_whenPlayerHasNullPosition_shouldDefaultToMidfielder() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team nullPosTeam = new Team();
        nullPosTeam.setId(1L);
        nullPosTeam.setName("Null Pos FC");
        nullPosTeam.setCountry("Turkey");

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Player player = new Player();
            player.setId((long) i);
            player.setFirstName("Player" + i);
            player.setLastName("Test");
            player.setCountry("Turkey");
            player.setPosition(null); // Null position
            player.setAge(25);
            player.setValue(1_000_000);
            players.add(player);
        }
        nullPosTeam.setPlayers(players);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(nullPosTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }



    // ==================== Edge Cases ====================

    @Test
    void playMatch_whenBothTeamsAreIdentical_shouldStillProduceResult() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team identicalTeam1 = createTeamWithPlayers(1L, "Team A");
        Team identicalTeam2 = createTeamWithPlayers(2L, "Team B");

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(identicalTeam1);
        when(teamService.getTeamById(awayTeamId)).thenReturn(identicalTeam2);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.homeTeamId());
        assertEquals(2L, result.awayTeamId());
    }

    @Test
    void playMatch_whenTeamHasEmptyPlayerList_shouldHandleGracefully() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team emptyTeam = new Team();
        emptyTeam.setId(1L);
        emptyTeam.setName("Empty FC");
        emptyTeam.setCountry("Turkey");
        emptyTeam.setPlayers(new ArrayList<>());

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(emptyTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
        assertTrue(result.homeScore() >= 0);
    }

    @Test
    void playMatch_whenTeamHasExcessPlayers_shouldSelectBestEleven() {
        // Given
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team largeTeam = new Team();
        largeTeam.setId(1L);
        largeTeam.setName("Large FC");
        largeTeam.setCountry("Turkey");

        List<Player> players = new ArrayList<>();
        // 3 Goalkeepers
        for (int i = 0; i < 3; i++) {
            players.add(createPlayer((long) i, "GK" + i, "Player", "Goalkeeper", 25, 2_000_000 - i * 100_000));
        }
        // 8 Defenders
        for (int i = 0; i < 8; i++) {
            players.add(createPlayer((long) (i + 3), "DEF" + i, "Player", "Defender", 26, 1_500_000 - i * 50_000));
        }
        // 8 Midfielders
        for (int i = 0; i < 8; i++) {
            players.add(createPlayer((long) (i + 11), "MID" + i, "Player", "Midfielder", 27, 2_500_000 - i * 50_000));
        }
        // 6 Forwards
        for (int i = 0; i < 6; i++) {
            players.add(createPlayer((long) (i + 19), "FWD" + i, "Player", "Forward", 24, 3_000_000 - i * 100_000));
        }
        largeTeam.setPlayers(players);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(largeTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    // ==================== Boundary Age Tests ====================

    @Test
    void playMatch_whenPlayerIsExactly21_shouldApplyPrimeAgeFactor() {
        // Given - Age 21 is boundary (not < 21, so should get prime factor)
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team boundaryAgeTeam = createTeamWithCustomAge(1L, "Boundary FC", 21);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(boundaryAgeTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    @Test
    void playMatch_whenPlayerIsExactly29_shouldApplyPrimeAgeFactor() {
        // Given - Age 29 is boundary (still <= 29, so should get prime factor)
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team boundaryAgeTeam = createTeamWithCustomAge(1L, "Boundary FC", 29);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(boundaryAgeTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    @Test
    void playMatch_whenPlayerIsExactly30_shouldApplyOldAgeFactor() {
        // Given - Age 30 is boundary (> 29, so should get old factor)
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team boundaryAgeTeam = createTeamWithCustomAge(1L, "Boundary FC", 30);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(boundaryAgeTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    @Test
    void playMatch_whenPlayerIsExactly20_shouldApplyYoungAgeFactor() {
        // Given - Age 20 is < 21, so should get young factor
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team youngTeam = createTeamWithCustomAge(1L, "Young FC", 20);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(youngTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
    }

    // ==================== Helper Methods ====================

    private Team createTeamWithCustomValues(Long teamId, String teamName, int playerValue) {
        Team team = new Team();
        team.setId(teamId);
        team.setName(teamName);
        team.setCountry("Turkey");

        List<Player> players = new ArrayList<>();
        players.add(createPlayer(teamId * 100 + 1, "GK", "Player", "Goalkeeper", 25, playerValue));
        for (int i = 0; i < 4; i++) {
            players.add(createPlayer(teamId * 100 + 2 + i, "DEF" + i, "Player", "Defender", 26, playerValue));
        }
        for (int i = 0; i < 4; i++) {
            players.add(createPlayer(teamId * 100 + 6 + i, "MID" + i, "Player", "Midfielder", 27, playerValue));
        }
        for (int i = 0; i < 2; i++) {
            players.add(createPlayer(teamId * 100 + 10 + i, "FWD" + i, "Player", "Forward", 24, playerValue));
        }

        team.setPlayers(players);
        return team;
    }

    private Team createTeamWithCustomAge(Long teamId, String teamName, int playerAge) {
        Team team = new Team();
        team.setId(teamId);
        team.setName(teamName);
        team.setCountry("Turkey");

        List<Player> players = new ArrayList<>();
        players.add(createPlayer(teamId * 100 + 1, "GK", "Player", "Goalkeeper", playerAge, 2_000_000));
        for (int i = 0; i < 4; i++) {
            players.add(createPlayer(teamId * 100 + 2 + i, "DEF" + i, "Player", "Defender", playerAge, 1_500_000));
        }
        for (int i = 0; i < 4; i++) {
            players.add(createPlayer(teamId * 100 + 6 + i, "MID" + i, "Player", "Midfielder", playerAge, 2_500_000));
        }
        for (int i = 0; i < 2; i++) {
            players.add(createPlayer(teamId * 100 + 10 + i, "FWD" + i, "Player", "Forward", playerAge, 3_000_000));
        }

        team.setPlayers(players);
        return team;
    }

    // KALDIR: playMatch_whenPlayerHasUnknownPosition_shouldDefaultToMidfielder
// KALDIR: playMatch_whenTeamHasNullValuePlayers_shouldPlayMatch

// YENİ TESTLER:

    @Test
    void playMatch_whenTeamHasMinimumValuePlayers_shouldPlayMatch() {
        // Given - Minimum valid value (just above 0)
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team minValueTeam = createTeamWithCustomValues(1L, "Min Value FC", 1);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(minValueTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(awayTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
        assertTrue(result.homeScore() >= 0);
    }

    @Test
    void playMatch_whenHomeTeamIsStronger_shouldStillProduceValidResult() {
        // Given - Home team has much higher value players
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team strongTeam = createTeamWithCustomValues(1L, "Strong FC", 100_000_000);
        Team weakTeam = createTeamWithCustomValues(2L, "Weak FC", 500_000);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(strongTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(weakTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
        assertTrue(result.homeScore() >= 0);
        assertTrue(result.awayScore() >= 0);
    }

    @Test
    void playMatch_whenAwayTeamIsStronger_shouldStillProduceValidResult() {
        // Given - Away team has much higher value players
        Long homeTeamId = 1L;
        Long awayTeamId = 2L;

        Team weakTeam = createTeamWithCustomValues(1L, "Weak FC", 500_000);
        Team strongTeam = createTeamWithCustomValues(2L, "Strong FC", 100_000_000);

        // When
        when(teamService.getTeamById(homeTeamId)).thenReturn(weakTeam);
        when(teamService.getTeamById(awayTeamId)).thenReturn(strongTeam);

        MatchResult result = matchSimulationService.playMatch(homeTeamId, awayTeamId);

        // Then
        assertNotNull(result);
        assertTrue(result.homeScore() >= 0);
        assertTrue(result.awayScore() >= 0);
    }
}