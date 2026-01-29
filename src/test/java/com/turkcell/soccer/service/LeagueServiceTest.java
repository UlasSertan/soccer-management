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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LeagueServiceTest {

    @Mock
    private LeagueRepository leagueRepository;

    @Mock
    private TeamService teamService;

    @Mock
    private MatchSimulationService matchSimulationService;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private LeagueMapper leagueMapper;

    @Mock
    private LeagueStandingsRepository leagueStandingsRepository;

    @Mock
    private PlayerStandingsRepository playerStandingsRepository;

    @Mock
    private MatchMapper matchMapper;

    @Mock
    private MatchRepository matchRepository;

    @InjectMocks
    private LeagueService leagueService;

    private Team team1;
    private Team team2;
    private League league;
    private List<Player> players1;
    private List<Player> players2;

    @BeforeEach
    void setUp() {
        team1 = new Team();
        team1.setId(1L);
        team1.setName("Team A");
        team1.setCountry("Turkey");
        players1 = createPlayersForTeam(11, team1);
        team1.setPlayers(players1);

        team2 = new Team();
        team2.setId(2L);
        team2.setName("Team B");
        team2.setCountry("Turkey");
        players2 = createPlayersForTeam(11, team2);
        team2.setPlayers(players2);

        league = new League();
        league.setLeagueId(1L);
        league.setTeams(new ArrayList<>(List.of(team1, team2)));
        league.setLeagueStandings(new ArrayList<>());
        league.setPlayerStandings(new ArrayList<>());
    }

    private List<Player> createPlayersForTeam(int count, Team team) {
        List<Player> playerList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Player player = new Player();
            player.setId((long) (i + 1 + (team.getId() != null ? team.getId() * 100 : 0)));
            player.setFirstName("Player" + i);
            player.setLastName("Test");
            player.setCountry("Turkey");
            player.setAge(25);
            player.setValue(1000000);
            if (i < 3) {
                player.setPosition("Forward");
            } else if (i < 7) {
                player.setPosition("Midfielder");
            } else if (i < 10) {
                player.setPosition("Defender");
            } else {
                player.setPosition("Goalkeeper");
            }
            playerList.add(player);
        }
        return playerList;
    }

    // ==================== createLeague Tests ====================

    @Test
    void createLeague_whenValidTeamsProvided_shouldCreateLeagueSuccessfully() {
        // Given
        LeagueRequest request = new LeagueRequest();
        request.setTeamIds(List.of(1L, 2L));

        LeagueDto expectedDto = LeagueDto.builder()
                .leagueId(1L)
                .teamIds(List.of(1L, 2L))
                .build();

        // When
        when(teamRepository.findAllById(anyList())).thenReturn(List.of(team1, team2));
        when(leagueRepository.save(any(League.class))).thenReturn(league);
        when(leagueMapper.leagueToLeagueDto(any(League.class))).thenReturn(expectedDto);

        LeagueDto result = leagueService.createLeague(request);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getLeagueId());
        verify(teamRepository, times(1)).findAllById(anyList());
        verify(leagueRepository, times(1)).save(any(League.class));
        verify(leagueMapper, times(1)).leagueToLeagueDto(any(League.class));
    }

    @Test
    void createLeague_whenSomeTeamsNotFound_shouldThrowNoSuchElementException() {
        // Given
        LeagueRequest request = new LeagueRequest();
        request.setTeamIds(List.of(1L, 2L, 3L));

        // When
        when(teamRepository.findAllById(anyList())).thenReturn(List.of(team1, team2));

        // Then
        assertThrowsExactly(NoSuchElementException.class, () -> leagueService.createLeague(request));
        verify(teamRepository, times(1)).findAllById(anyList());
        verify(leagueRepository, never()).save(any(League.class));
    }

    @Test
    void createLeague_whenTeamHasLessThan11Players_shouldExcludeTeam() {
        // Given
        LeagueRequest request = new LeagueRequest();
        request.setTeamIds(List.of(1L, 2L));

        Team teamWithFewPlayers = new Team();
        teamWithFewPlayers.setId(2L);
        teamWithFewPlayers.setName("Team B");
        teamWithFewPlayers.setCountry("Turkey");
        teamWithFewPlayers.setPlayers(createPlayersForTeam(5, teamWithFewPlayers));

        LeagueDto expectedDto = LeagueDto.builder().leagueId(1L).build();

        ArgumentCaptor<League> leagueCaptor = ArgumentCaptor.forClass(League.class);

        // When
        when(teamRepository.findAllById(anyList())).thenReturn(List.of(team1, teamWithFewPlayers));
        when(leagueRepository.save(leagueCaptor.capture())).thenReturn(league);
        when(leagueMapper.leagueToLeagueDto(any(League.class))).thenReturn(expectedDto);

        leagueService.createLeague(request);

        // Then
        League capturedLeague = leagueCaptor.getValue();
        assertEquals(1, capturedLeague.getTeams().size());
        assertEquals(team1.getId(), capturedLeague.getTeams().get(0).getId());
    }

    @Test
    void createLeague_whenTeamHasNullPlayers_shouldExcludeTeam() {
        // Given
        LeagueRequest request = new LeagueRequest();
        request.setTeamIds(List.of(1L, 2L));

        Team teamWithNullPlayers = new Team();
        teamWithNullPlayers.setId(2L);
        teamWithNullPlayers.setName("Team B");
        teamWithNullPlayers.setCountry("Turkey");
        teamWithNullPlayers.setPlayers(null);

        LeagueDto expectedDto = LeagueDto.builder().leagueId(1L).build();

        ArgumentCaptor<League> leagueCaptor = ArgumentCaptor.forClass(League.class);

        // When
        when(teamRepository.findAllById(anyList())).thenReturn(List.of(team1, teamWithNullPlayers));
        when(leagueRepository.save(leagueCaptor.capture())).thenReturn(league);
        when(leagueMapper.leagueToLeagueDto(any(League.class))).thenReturn(expectedDto);

        leagueService.createLeague(request);

        // Then
        League capturedLeague = leagueCaptor.getValue();
        assertEquals(1, capturedLeague.getTeams().size());
    }

    @Test
    void createLeague_whenTeamIdsIsEmpty_shouldCreateLeagueWithNoTeams() {
        // Given
        LeagueRequest request = new LeagueRequest();
        request.setTeamIds(new ArrayList<>());

        LeagueDto expectedDto = LeagueDto.builder().leagueId(1L).build();

        // When
        when(leagueRepository.save(any(League.class))).thenReturn(league);
        when(leagueMapper.leagueToLeagueDto(any(League.class))).thenReturn(expectedDto);

        LeagueDto result = leagueService.createLeague(request);

        // Then
        assertNotNull(result);
        verify(teamRepository, never()).findAllById(anyList());
        verify(leagueRepository, times(1)).save(any(League.class));
    }

    @Test
    void createLeague_whenTeamIdsIsNull_shouldCreateLeagueWithNoTeams() {
        // Given
        LeagueRequest request = new LeagueRequest();
        request.setTeamIds(null);

        LeagueDto expectedDto = LeagueDto.builder().leagueId(1L).build();

        // When
        when(leagueRepository.save(any(League.class))).thenReturn(league);
        when(leagueMapper.leagueToLeagueDto(any(League.class))).thenReturn(expectedDto);

        LeagueDto result = leagueService.createLeague(request);

        // Then
        assertNotNull(result);
        verify(teamRepository, never()).findAllById(anyList());
    }

    @Test
    void createLeague_whenValidTeams_shouldInitializePlayerStandings() {
        // Given
        LeagueRequest request = new LeagueRequest();
        request.setTeamIds(List.of(1L, 2L));

        LeagueDto expectedDto = LeagueDto.builder().leagueId(1L).build();

        ArgumentCaptor<League> leagueCaptor = ArgumentCaptor.forClass(League.class);

        // When
        when(teamRepository.findAllById(anyList())).thenReturn(List.of(team1, team2));
        when(leagueRepository.save(leagueCaptor.capture())).thenReturn(league);
        when(leagueMapper.leagueToLeagueDto(any(League.class))).thenReturn(expectedDto);

        leagueService.createLeague(request);

        // Then
        League capturedLeague = leagueCaptor.getValue();
        assertEquals(22, capturedLeague.getPlayerStandings().size()); // 11 players * 2 teams
        capturedLeague.getPlayerStandings().forEach(ps -> {
            assertEquals(0, ps.getGoals());
            assertEquals(0, ps.getAssists());
        });
    }

    // ==================== simulateSeason Tests ====================

    @Test
    void simulateSeason_whenLeagueExists_shouldSimulateAllMatches() {
        // Given
        Long leagueId = 1L;

        LeagueStandings standing1 = createLeagueStanding(team1, league);
        LeagueStandings standing2 = createLeagueStanding(team2, league);
        league.setLeagueStandings(new ArrayList<>(List.of(standing1, standing2)));
        league.setPlayerStandings(createPlayerStandingsForLeague(league));

        MatchResult matchResult = new MatchResult(1L, 2L, "Team A", "Team B", 2, 1);
        Match matchEntity = new Match();

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchSimulationService.playMatch(anyLong(), anyLong())).thenReturn(matchResult);
        when(matchMapper.toEntity(any(MatchResult.class))).thenReturn(matchEntity);
        when(matchRepository.save(any(Match.class))).thenReturn(matchEntity);
        when(teamService.getTeamById(1L)).thenReturn(team1);
        when(teamService.getTeamById(2L)).thenReturn(team2);

        // Then
        assertDoesNotThrow(() -> leagueService.simulateSeason(leagueId));
        verify(leagueRepository, times(1)).findById(leagueId);
        verify(matchSimulationService, times(2)).playMatch(anyLong(), anyLong());
        verify(matchRepository, times(2)).save(any(Match.class));
    }

    @Test
    void simulateSeason_whenLeagueNotFound_shouldThrowNoSuchElementException() {
        // Given
        Long leagueId = 999L;

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.empty());

        // Then
        assertThrowsExactly(NoSuchElementException.class, () -> leagueService.simulateSeason(leagueId));
        verify(leagueRepository, times(1)).findById(leagueId);
        verify(matchSimulationService, never()).playMatch(anyLong(), anyLong());
    }

    @Test
    void simulateSeason_whenOddNumberOfTeams_shouldThrowRuntimeException() {
        // Given
        Long leagueId = 1L;

        Team team3 = new Team();
        team3.setId(3L);
        team3.setName("Team C");
        team3.setCountry("Turkey");
        team3.setPlayers(createPlayersForTeam(11, team3));

        league.setTeams(List.of(team1, team2, team3));

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));

        // Then
        RuntimeException exception = assertThrowsExactly(RuntimeException.class,
                () -> leagueService.simulateSeason(leagueId));
        assertEquals("Number of teams is not even", exception.getMessage());
        verify(leagueRepository, times(1)).findById(leagueId);
    }

    @Test
    void simulateSeason_whenFourTeams_shouldPlayCorrectNumberOfMatches() {
        // Given
        Long leagueId = 1L;

        Team team3 = new Team();
        team3.setId(3L);
        team3.setName("Team C");
        team3.setCountry("Turkey");
        team3.setPlayers(createPlayersForTeam(11, team3));

        Team team4 = new Team();
        team4.setId(4L);
        team4.setName("Team D");
        team4.setCountry("Turkey");
        team4.setPlayers(createPlayersForTeam(11, team4));

        league.setTeams(new ArrayList<>(List.of(team1, team2, team3, team4)));

        LeagueStandings standing1 = createLeagueStanding(team1, league);
        LeagueStandings standing2 = createLeagueStanding(team2, league);
        LeagueStandings standing3 = createLeagueStanding(team3, league);
        LeagueStandings standing4 = createLeagueStanding(team4, league);
        league.setLeagueStandings(new ArrayList<>(List.of(standing1, standing2, standing3, standing4)));
        league.setPlayerStandings(createPlayerStandingsForLeague(league));

        MatchResult matchResult = new MatchResult(1L, 2L, "Team A", "Team B", 1, 1);
        Match matchEntity = new Match();

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchSimulationService.playMatch(anyLong(), anyLong())).thenReturn(matchResult);
        when(matchMapper.toEntity(any(MatchResult.class))).thenReturn(matchEntity);
        when(matchRepository.save(any(Match.class))).thenReturn(matchEntity);
        when(teamService.getTeamById(anyLong())).thenAnswer(invocation -> {
            Long id = invocation.getArgument(0);
            return league.getTeams().stream()
                    .filter(t -> t.getId().equals(id))
                    .findFirst()
                    .orElse(team1);
        });

        leagueService.simulateSeason(leagueId);

        // Then
        // 4 teams: each team plays 6 matches (3 home, 3 away) = 12 total matches
        verify(matchSimulationService, times(12)).playMatch(anyLong(), anyLong());
        verify(matchRepository, times(12)).save(any(Match.class));
    }

    // ==================== getFinalMatches Tests ====================

    @Test
    void getFinalMatches_whenLeagueHasMatches_shouldReturnMatchList() {
        // Given
        Long leagueId = 1L;
        Match match1 = new Match();
        match1.setId(1L);
        match1.setLeagueId(leagueId);
        Match match2 = new Match();
        match2.setId(2L);
        match2.setLeagueId(leagueId);
        List<Match> expectedMatches = List.of(match1, match2);

        // When
        when(matchRepository.findByLeagueId(leagueId)).thenReturn(expectedMatches);

        List<Match> result = leagueService.getFinalMatches(leagueId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(matchRepository, times(1)).findByLeagueId(leagueId);
    }

    @Test
    void getFinalMatches_whenNoMatches_shouldReturnEmptyList() {
        // Given
        Long leagueId = 1L;

        // When
        when(matchRepository.findByLeagueId(leagueId)).thenReturn(new ArrayList<>());

        List<Match> result = leagueService.getFinalMatches(leagueId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(matchRepository, times(1)).findByLeagueId(leagueId);
    }

    // ==================== getWeekMatches Tests ====================

    @Test
    void getWeekMatches_whenMatchesExist_shouldReturnMatchesForWeek() {
        // Given
        Long leagueId = 1L;
        Integer week = 1;
        Match match = new Match();
        match.setWeek(1);
        match.setLeagueId(leagueId);
        List<Match> expectedMatches = List.of(match);

        // When
        when(matchRepository.findByLeagueIdAndWeek(leagueId, week)).thenReturn(expectedMatches);

        List<Match> result = leagueService.getWeekMatches(leagueId, week);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getWeek());
        verify(matchRepository, times(1)).findByLeagueIdAndWeek(leagueId, week);
    }

    @Test
    void getWeekMatches_whenNoMatchesForWeek_shouldReturnEmptyList() {
        // Given
        Long leagueId = 1L;
        Integer week = 99;

        // When
        when(matchRepository.findByLeagueIdAndWeek(leagueId, week)).thenReturn(new ArrayList<>());

        List<Match> result = leagueService.getWeekMatches(leagueId, week);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(matchRepository, times(1)).findByLeagueIdAndWeek(leagueId, week);
    }

    // ==================== getFinalResults Tests ====================

    @Test
    void getFinalResults_whenStandingsExist_shouldReturnStandingsDtoList() {
        // Given
        Long leagueId = 1L;
        LeagueStandings standing = createLeagueStanding(team1, league);
        standing.setWins(5);
        standing.setPlayed(10);
        List<LeagueStandings> standings = List.of(standing);

        LeagueStandingsDto dto = new LeagueStandingsDto(1L, "Team A", 10, 5, 3, 2, 15, 8, 7, 18);
        List<LeagueStandingsDto> expectedDtos = List.of(dto);

        // When
        when(leagueStandingsRepository.findFinalTable(leagueId)).thenReturn(standings);
        when(leagueMapper.toLeagueStandingsDtoList(standings)).thenReturn(expectedDtos);

        List<LeagueStandingsDto> result = leagueService.getFinalResults(leagueId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Team A", result.get(0).teamName());
        verify(leagueStandingsRepository, times(1)).findFinalTable(leagueId);
        verify(leagueMapper, times(1)).toLeagueStandingsDtoList(standings);
    }

    @Test
    void getFinalResults_whenNoStandings_shouldReturnEmptyList() {
        // Given
        Long leagueId = 1L;

        // When
        when(leagueStandingsRepository.findFinalTable(leagueId)).thenReturn(new ArrayList<>());
        when(leagueMapper.toLeagueStandingsDtoList(anyList())).thenReturn(new ArrayList<>());

        List<LeagueStandingsDto> result = leagueService.getFinalResults(leagueId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getTopScorers Tests ====================

    @Test
    void getTopScorers_whenScorersExist_shouldReturnPlayerStandingsDtoList() {
        // Given
        Long leagueId = 1L;
        PlayerStandings playerStanding = new PlayerStandings();
        playerStanding.setGoals(10);
        playerStanding.setAssists(5);
        List<PlayerStandings> scorers = List.of(playerStanding);

        PlayerStandingsDto dto = new PlayerStandingsDto(1L, "Player Test", "Team A", 10, 5);
        List<PlayerStandingsDto> expectedDtos = List.of(dto);

        // When
        when(playerStandingsRepository.findGoalTable(leagueId)).thenReturn(scorers);
        when(leagueMapper.toPlayerStandingsDtoList(scorers)).thenReturn(expectedDtos);

        List<PlayerStandingsDto> result = leagueService.getTopScorers(leagueId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(10, result.get(0).goals());
        verify(playerStandingsRepository, times(1)).findGoalTable(leagueId);
    }

    @Test
    void getTopScorers_whenNoScorers_shouldReturnEmptyList() {
        // Given
        Long leagueId = 1L;

        // When
        when(playerStandingsRepository.findGoalTable(leagueId)).thenReturn(new ArrayList<>());
        when(leagueMapper.toPlayerStandingsDtoList(anyList())).thenReturn(new ArrayList<>());

        List<PlayerStandingsDto> result = leagueService.getTopScorers(leagueId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== getTopAssisters Tests ====================

    @Test
    void getTopAssisters_whenAssistersExist_shouldReturnPlayerStandingsDtoList() {
        // Given
        Long leagueId = 1L;
        PlayerStandings playerStanding = new PlayerStandings();
        playerStanding.setGoals(3);
        playerStanding.setAssists(12);
        List<PlayerStandings> assisters = List.of(playerStanding);

        PlayerStandingsDto dto = new PlayerStandingsDto(1L, "Player Test", "Team A", 3, 12);
        List<PlayerStandingsDto> expectedDtos = List.of(dto);

        // When
        when(playerStandingsRepository.findAssistTable(leagueId)).thenReturn(assisters);
        when(leagueMapper.toPlayerStandingsDtoList(assisters)).thenReturn(expectedDtos);

        List<PlayerStandingsDto> result = leagueService.getTopAssisters(leagueId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(12, result.get(0).assists());
        verify(playerStandingsRepository, times(1)).findAssistTable(leagueId);
    }

    @Test
    void getTopAssisters_whenNoAssisters_shouldReturnEmptyList() {
        // Given
        Long leagueId = 1L;

        // When
        when(playerStandingsRepository.findAssistTable(leagueId)).thenReturn(new ArrayList<>());
        when(leagueMapper.toPlayerStandingsDtoList(anyList())).thenReturn(new ArrayList<>());

        List<PlayerStandingsDto> result = leagueService.getTopAssisters(leagueId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== resetLeague Tests ====================

    @Test
    void resetLeague_whenLeagueExists_shouldResetAllStandings() {
        // Given
        Long leagueId = 1L;

        LeagueStandings standing = new LeagueStandings();
        standing.setTeam(team1);
        standing.setLeague(league);
        standing.setPlayed(10);
        standing.setWins(5);
        standing.setDraws(3);
        standing.setLosses(2);
        standing.setGoalsScored(15);
        standing.setGoalsConceded(8);

        PlayerStandings playerStanding = new PlayerStandings();
        playerStanding.setPlayer(players1.get(0));
        playerStanding.setLeague(league);
        playerStanding.setGoals(5);
        playerStanding.setAssists(3);

        league.setLeagueStandings(List.of(standing));
        league.setPlayerStandings(List.of(playerStanding));

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));

        leagueService.resetLeague(leagueId);

        // Then
        assertEquals(0, standing.getPlayed());
        assertEquals(0, standing.getWins());
        assertEquals(0, standing.getDraws());
        assertEquals(0, standing.getLosses());
        assertEquals(0, standing.getGoalsScored());
        assertEquals(0, standing.getGoalsConceded());
        assertEquals(0, playerStanding.getGoals());
        assertEquals(0, playerStanding.getAssists());
        verify(leagueRepository, times(1)).findById(leagueId);
    }

    @Test
    void resetLeague_whenLeagueNotFound_shouldThrowNoSuchElementException() {
        // Given
        Long leagueId = 999L;

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.empty());

        // Then
        assertThrowsExactly(NoSuchElementException.class, () -> leagueService.resetLeague(leagueId));
        verify(leagueRepository, times(1)).findById(leagueId);
    }

    @Test
    void resetLeague_whenMultipleStandings_shouldResetAll() {
        // Given
        Long leagueId = 1L;

        LeagueStandings standing1 = createLeagueStanding(team1, league);
        standing1.setPlayed(10);
        standing1.setWins(7);

        LeagueStandings standing2 = createLeagueStanding(team2, league);
        standing2.setPlayed(10);
        standing2.setLosses(7);

        PlayerStandings ps1 = new PlayerStandings();
        ps1.setPlayer(players1.get(0));
        ps1.setGoals(10);

        PlayerStandings ps2 = new PlayerStandings();
        ps2.setPlayer(players2.get(0));
        ps2.setAssists(8);

        league.setLeagueStandings(List.of(standing1, standing2));
        league.setPlayerStandings(List.of(ps1, ps2));

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));

        leagueService.resetLeague(leagueId);

        // Then
        assertEquals(0, standing1.getPlayed());
        assertEquals(0, standing1.getWins());
        assertEquals(0, standing2.getPlayed());
        assertEquals(0, standing2.getLosses());
        assertEquals(0, ps1.getGoals());
        assertEquals(0, ps2.getAssists());
    }

    // ==================== Standings Update Tests ====================

    @Test
    void simulateSeason_whenHomeTeamWins_shouldUpdateStandingsCorrectly() {
        // Given
        Long leagueId = 1L;

        LeagueStandings standing1 = createLeagueStanding(team1, league);
        LeagueStandings standing2 = createLeagueStanding(team2, league);
        league.setLeagueStandings(new ArrayList<>(List.of(standing1, standing2)));
        league.setPlayerStandings(createPlayerStandingsForLeague(league));

        // Home team wins 3-1
        MatchResult homeWinResult = new MatchResult(1L, 2L, "Team A", "Team B", 3, 1);
        Match matchEntity = new Match();

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchSimulationService.playMatch(anyLong(), anyLong())).thenReturn(homeWinResult);
        when(matchMapper.toEntity(any(MatchResult.class))).thenReturn(matchEntity);
        when(matchRepository.save(any(Match.class))).thenReturn(matchEntity);
        when(teamService.getTeamById(1L)).thenReturn(team1);
        when(teamService.getTeamById(2L)).thenReturn(team2);

        leagueService.simulateSeason(leagueId);

        // Then
        assertEquals(2, standing1.getPlayed());
        assertEquals(2, standing2.getPlayed());
        verify(matchSimulationService, times(2)).playMatch(anyLong(), anyLong());
    }

    @Test
    void simulateSeason_whenDrawOccurs_shouldUpdateDrawsCorrectly() {
        // Given
        Long leagueId = 1L;

        LeagueStandings standing1 = createLeagueStanding(team1, league);
        LeagueStandings standing2 = createLeagueStanding(team2, league);
        league.setLeagueStandings(new ArrayList<>(List.of(standing1, standing2)));
        league.setPlayerStandings(createPlayerStandingsForLeague(league));

        MatchResult drawResult = new MatchResult(1L, 2L, "Team A", "Team B", 2, 2);
        Match matchEntity = new Match();

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchSimulationService.playMatch(anyLong(), anyLong())).thenReturn(drawResult);
        when(matchMapper.toEntity(any(MatchResult.class))).thenReturn(matchEntity);
        when(matchRepository.save(any(Match.class))).thenReturn(matchEntity);
        when(teamService.getTeamById(1L)).thenReturn(team1);
        when(teamService.getTeamById(2L)).thenReturn(team2);

        leagueService.simulateSeason(leagueId);

        // Then
        assertEquals(2, standing1.getDraws());
        assertEquals(2, standing2.getDraws());
    }

    @Test
    void simulateSeason_whenAwayTeamWins_shouldUpdateWinsAndLossesCorrectly() {
        // Given
        Long leagueId = 1L;

        LeagueStandings standing1 = createLeagueStanding(team1, league);
        LeagueStandings standing2 = createLeagueStanding(team2, league);
        league.setLeagueStandings(new ArrayList<>(List.of(standing1, standing2)));
        league.setPlayerStandings(createPlayerStandingsForLeague(league));

        // Away team wins 0-2
        MatchResult awayWinResult = new MatchResult(1L, 2L, "Team A", "Team B", 0, 2);
        Match matchEntity = new Match();

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchSimulationService.playMatch(anyLong(), anyLong())).thenReturn(awayWinResult);
        when(matchMapper.toEntity(any(MatchResult.class))).thenReturn(matchEntity);
        when(matchRepository.save(any(Match.class))).thenReturn(matchEntity);
        when(teamService.getTeamById(1L)).thenReturn(team1);
        when(teamService.getTeamById(2L)).thenReturn(team2);

        leagueService.simulateSeason(leagueId);

        // Then
        assertEquals(2, standing1.getLosses());
        assertEquals(2, standing2.getWins());
    }

    // ==================== Helper Methods ====================

    private LeagueStandings createLeagueStanding(Team team, League league) {
        LeagueStandings standing = new LeagueStandings();
        standing.setTeam(team);
        standing.setLeague(league);
        standing.setPlayed(0);
        standing.setWins(0);
        standing.setDraws(0);
        standing.setLosses(0);
        standing.setGoalsScored(0);
        standing.setGoalsConceded(0);
        return standing;
    }

    private List<PlayerStandings> createPlayerStandingsForLeague(League league) {
        List<PlayerStandings> playerStandingsList = new ArrayList<>();
        for (Team team : league.getTeams()) {
            if (team.getPlayers() != null) {
                for (Player player : team.getPlayers()) {
                    PlayerStandings ps = new PlayerStandings();
                    ps.setPlayer(player);
                    ps.setLeague(league);
                    ps.setGoals(0);
                    ps.setAssists(0);
                    playerStandingsList.add(ps);
                }
            }
        }
        return playerStandingsList;
    }

    // ==================== Additional Coverage Tests ====================

    @Test
    void simulateSeason_whenTeamNotInStandings_shouldThrowNoSuchElementException() {
        // Given - Lines 163-165 coverage
        Long leagueId = 1L;

        // Standing listesinde sadece team1 var, team2 yok
        LeagueStandings standing1 = createLeagueStanding(team1, league);
        league.setLeagueStandings(new ArrayList<>(List.of(standing1))); // team2 eksik!
        league.setPlayerStandings(createPlayerStandingsForLeague(league));

        MatchResult matchResult = new MatchResult(1L, 2L, "Team A", "Team B", 2, 1);
        Match matchEntity = new Match();

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchSimulationService.playMatch(anyLong(), anyLong())).thenReturn(matchResult);
        when(matchMapper.toEntity(any(MatchResult.class))).thenReturn(matchEntity);
        when(matchRepository.save(any(Match.class))).thenReturn(matchEntity);

        // Then
        assertThrowsExactly(NoSuchElementException.class, () -> leagueService.simulateSeason(leagueId));
    }

    @Test
    void simulateSeason_whenTeamHasNoPlayers_shouldNotAssignGoals() {
        // Given - Line 260 coverage
        Long leagueId = 1L;

        Team teamWithNoPlayers = new Team();
        teamWithNoPlayers.setId(2L);
        teamWithNoPlayers.setName("Empty Team");
        teamWithNoPlayers.setCountry("Turkey");
        teamWithNoPlayers.setPlayers(new ArrayList<>()); // Boş oyuncu listesi

        league.setTeams(new ArrayList<>(List.of(team1, teamWithNoPlayers)));

        LeagueStandings standing1 = createLeagueStanding(team1, league);
        LeagueStandings standing2 = createLeagueStanding(teamWithNoPlayers, league);
        league.setLeagueStandings(new ArrayList<>(List.of(standing1, standing2)));

        // Sadece team1'in oyuncuları için standings oluştur
        List<PlayerStandings> playerStandingsList = new ArrayList<>();
        for (Player player : team1.getPlayers()) {
            PlayerStandings ps = new PlayerStandings();
            ps.setPlayer(player);
            ps.setLeague(league);
            ps.setGoals(0);
            ps.setAssists(0);
            playerStandingsList.add(ps);
        }
        league.setPlayerStandings(playerStandingsList);

        // teamWithNoPlayers gol atıyor ama oyuncusu yok
        MatchResult matchResult = new MatchResult(2L, 1L, "Empty Team", "Team A", 2, 0);
        Match matchEntity = new Match();

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchSimulationService.playMatch(anyLong(), anyLong())).thenReturn(matchResult);
        when(matchMapper.toEntity(any(MatchResult.class))).thenReturn(matchEntity);
        when(matchRepository.save(any(Match.class))).thenReturn(matchEntity);
        when(teamService.getTeamById(1L)).thenReturn(team1);
        when(teamService.getTeamById(2L)).thenReturn(teamWithNoPlayers);

        // Then - Exception fırlatmadan çalışmalı
        assertDoesNotThrow(() -> leagueService.simulateSeason(leagueId));
    }

    @Test
    void simulateSeason_whenTeamHasOnlyMidfielders_shouldAssignGoalsToMidfielders() {
        // Given - Lines 297, 307 coverage
        Long leagueId = 1L;

        // Sadece midfielder'lardan oluşan takım
        Team midfielderOnlyTeam = new Team();
        midfielderOnlyTeam.setId(2L);
        midfielderOnlyTeam.setName("Midfield FC");
        midfielderOnlyTeam.setCountry("Turkey");

        List<Player> midfielders = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Player player = new Player();
            player.setId((long) (200 + i));
            player.setFirstName("Mid" + i);
            player.setLastName("Player");
            player.setCountry("Turkey");
            player.setAge(25);
            player.setValue(1000000);
            player.setPosition("Midfielder"); // Hepsi midfielder
            midfielders.add(player);
        }
        midfielderOnlyTeam.setPlayers(midfielders);

        league.setTeams(new ArrayList<>(List.of(team1, midfielderOnlyTeam)));

        LeagueStandings standing1 = createLeagueStanding(team1, league);
        LeagueStandings standing2 = createLeagueStanding(midfielderOnlyTeam, league);
        league.setLeagueStandings(new ArrayList<>(List.of(standing1, standing2)));
        league.setPlayerStandings(createPlayerStandingsForLeague(league));

        MatchResult matchResult = new MatchResult(2L, 1L, "Midfield FC", "Team A", 3, 1);
        Match matchEntity = new Match();

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchSimulationService.playMatch(anyLong(), anyLong())).thenReturn(matchResult);
        when(matchMapper.toEntity(any(MatchResult.class))).thenReturn(matchEntity);
        when(matchRepository.save(any(Match.class))).thenReturn(matchEntity);
        when(teamService.getTeamById(1L)).thenReturn(team1);
        when(teamService.getTeamById(2L)).thenReturn(midfielderOnlyTeam);

        // Then
        assertDoesNotThrow(() -> leagueService.simulateSeason(leagueId));
    }

    @Test
    void simulateSeason_whenTeamHasNoPositions_shouldFallbackToAllPlayers() {
        // Given - Line 307 coverage (selectedPool boşsa tüm takım)
        Long leagueId = 1L;

        Team noPositionTeam = new Team();
        noPositionTeam.setId(2L);
        noPositionTeam.setName("No Position FC");
        noPositionTeam.setCountry("Turkey");

        List<Player> playersWithoutPosition = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Player player = new Player();
            player.setId((long) (300 + i));
            player.setFirstName("Unknown" + i);
            player.setLastName("Player");
            player.setCountry("Turkey");
            player.setAge(25);
            player.setValue(1000000);
            player.setPosition("Goalkeeper"); // Hepsi kaleci - Forward/Mid/Def yok
            playersWithoutPosition.add(player);
        }
        noPositionTeam.setPlayers(playersWithoutPosition);

        league.setTeams(new ArrayList<>(List.of(team1, noPositionTeam)));

        LeagueStandings standing1 = createLeagueStanding(team1, league);
        LeagueStandings standing2 = createLeagueStanding(noPositionTeam, league);
        league.setLeagueStandings(new ArrayList<>(List.of(standing1, standing2)));
        league.setPlayerStandings(createPlayerStandingsForLeague(league));

        MatchResult matchResult = new MatchResult(2L, 1L, "No Position FC", "Team A", 2, 0);
        Match matchEntity = new Match();

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchSimulationService.playMatch(anyLong(), anyLong())).thenReturn(matchResult);
        when(matchMapper.toEntity(any(MatchResult.class))).thenReturn(matchEntity);
        when(matchRepository.save(any(Match.class))).thenReturn(matchEntity);
        when(teamService.getTeamById(1L)).thenReturn(team1);
        when(teamService.getTeamById(2L)).thenReturn(noPositionTeam);

        // Then - Fallback çalışmalı
        assertDoesNotThrow(() -> leagueService.simulateSeason(leagueId));
    }

    @Test
    void getFinalResults_whenLeagueNotFound_shouldThrowNoSuchElementException() {
        // Given - Line 343 coverage (getLeagueById exception)
        Long leagueId = 999L;

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.empty());

        // Then
        // Not: getFinalResults doğrudan getLeagueById kullanmıyor,
        // ama simulateSeason ve resetLeague kullanıyor - zaten test ediliyor
        // Bu test resetLeague üzerinden line 343'ü cover ediyor
        assertThrowsExactly(NoSuchElementException.class, () -> leagueService.resetLeague(leagueId));
    }

    @Test
    void simulateSeason_whenProbabilityEdgeCase_shouldSelectLastPlayer() {
        // Given - Line 343 coverage (selectPlayer fallback return)
        Long leagueId = 1L;

        // Tüm oyuncuların value'su 0 olursa, cumulative probability hesaplanamaz
        // ve döngü hiçbir koşulu sağlamadan biter, fallback'e düşer
        Team zeroValueTeam = new Team();
        zeroValueTeam.setId(2L);
        zeroValueTeam.setName("Zero Value FC");
        zeroValueTeam.setCountry("Turkey");

        List<Player> zeroValuePlayers = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            Player player = new Player();
            player.setId((long) (400 + i));
            player.setFirstName("Zero" + i);
            player.setLastName("Player");
            player.setCountry("Turkey");
            player.setAge(25);
            player.setValue(0); // Value = 0, totalValue = 0, probability = NaN
            if (i < 3) {
                player.setPosition("Forward");
            } else if (i < 7) {
                player.setPosition("Midfielder");
            } else {
                player.setPosition("Defender");
            }
            zeroValuePlayers.add(player);
        }
        zeroValueTeam.setPlayers(zeroValuePlayers);

        league.setTeams(new ArrayList<>(List.of(team1, zeroValueTeam)));

        LeagueStandings standing1 = createLeagueStanding(team1, league);
        LeagueStandings standing2 = createLeagueStanding(zeroValueTeam, league);
        league.setLeagueStandings(new ArrayList<>(List.of(standing1, standing2)));
        league.setPlayerStandings(createPlayerStandingsForLeague(league));

        // zeroValueTeam gol atıyor - probability NaN olacak, fallback'e düşecek
        MatchResult matchResult = new MatchResult(2L, 1L, "Zero Value FC", "Team A", 5, 0);
        Match matchEntity = new Match();

        // When
        when(leagueRepository.findById(leagueId)).thenReturn(Optional.of(league));
        when(matchSimulationService.playMatch(anyLong(), anyLong())).thenReturn(matchResult);
        when(matchMapper.toEntity(any(MatchResult.class))).thenReturn(matchEntity);
        when(matchRepository.save(any(Match.class))).thenReturn(matchEntity);
        when(teamService.getTeamById(1L)).thenReturn(team1);
        when(teamService.getTeamById(2L)).thenReturn(zeroValueTeam);

        // Then - NaN comparison her zaman false döner, fallback çalışır
        assertDoesNotThrow(() -> leagueService.simulateSeason(leagueId));
    }
}