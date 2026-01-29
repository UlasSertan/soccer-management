package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.request.AdminTeamUpdateRequest;
import com.turkcell.soccer.dto.request.TeamRequest;
import com.turkcell.soccer.dto.request.TeamUpdateRequest;
import com.turkcell.soccer.dto.response.AdminTeamResponse;
import com.turkcell.soccer.dto.response.TeamInfoResponse;
import com.turkcell.soccer.dto.response.TeamResponse;
import com.turkcell.soccer.dto.response.TeamUpdateResponse;
import com.turkcell.soccer.exception.NoSuchTeamException;
import com.turkcell.soccer.mapper.TeamMapper;
import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.model.Team;
import com.turkcell.soccer.repository.AccountRepository;
import com.turkcell.soccer.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountService accountService;
    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamService teamService;


    @Test
    void createTeam_whenTeamNotExists_shouldReturnTeamResponse() {
        // Given
        String name = "name";
        String country = "country";
        Account account = new Account(); account.setTeam(null);
        TeamRequest teamRequest = new TeamRequest(name, country);
        Team team =  new Team();
        team.setName(name);
        team.setCountry(country);
        TeamResponse teamResponse = new TeamResponse(1L, name, country, 5_000_000);

        // When
        when(accountService.getAccount()).thenReturn(account);
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamMapper.toTeamResponse(any(Team.class))).thenReturn(teamResponse);

        // Then
        assertEquals(teamResponse, teamService.createTeam(teamRequest));
        verify(accountService, times(1)).getAccount();
        verify(teamRepository, times(1)).save(any(Team.class));
        verify(teamMapper, times(1)).toTeamResponse(any(Team.class));

    }

    @Test
    void createTeam_whenTeamExists_shouldReturnTeamResponse() {
        // Given
        String name = "name";
        String country = "country";
        Team team =  new Team();
        team.setName(name);
        team.setCountry(country);
        Account account = new Account(); account.setTeam(team);
        TeamRequest teamRequest = new TeamRequest(name, country);
        TeamResponse teamResponse = new TeamResponse(1L, name, country, 5_000_000);

        // When
        when(accountService.getAccount()).thenReturn(account);
        when(teamMapper.toTeamResponse(any(Team.class))).thenReturn(teamResponse);

        // Then
        assertEquals(teamResponse, teamService.createTeam(teamRequest));
        verify(accountService, times(1)).getAccount();
        verify(teamMapper, times(1)).toTeamResponse(any(Team.class));

    }

    @Test
    void getTeamInfo_whenTeamNotNull_shouldReturnTeamInfoResponse() {
        // Given
        Team team = new Team();
        Account account = new Account(); account.setTeam(team);
        TeamInfoResponse response = new TeamInfoResponse(1L, "a", "b", 1, 1, new ArrayList<>());
        // When
        when(accountService.getAccount()).thenReturn(account);
        when(teamMapper.toTeamInfoResponse(any(Team.class))).thenReturn(response);

        // Then
        assertEquals(response, teamService.getTeamInfo());
        verify(accountService, times(1)).getAccount();
    }

    @Test
    void getTeamInfo_whenTeamIsNull_shouldThrowNoSuchTeamException() {
        Account account = new Account();
        TeamInfoResponse response = new TeamInfoResponse(1L, "a", "b", 1, 1, new ArrayList<>());
        // When
        when(accountService.getAccount()).thenReturn(account);

        // Then
        assertThrowsExactly(NoSuchTeamException.class, () -> teamService.getTeamInfo());
        verify(accountService, times(1)).getAccount();
    }

    @Test
    void getAllTeamsInfo_whenTeamsNotEmpty_shouldReturnListOfTeamInfoResponse() {
        // Given
        List<Team> teams = new ArrayList<>();
        teams.add(new Team());
        List<TeamInfoResponse> responses = new ArrayList<>();
        TeamInfoResponse response = new TeamInfoResponse(1L, "a", "b", 1, 1, new ArrayList<>());
        responses.add(response);

        // When
        when(teamRepository.findAllQuery()).thenReturn(teams);
        when(teamMapper.toTeamInfoResponse(any(Team.class))).thenReturn(response);

        // Then
        assertEquals(responses, teamService.getAllTeamsInfo());
        verify(teamRepository, times(1)).findAllQuery();
        verify(teamMapper, times(1)).toTeamInfoResponse(any(Team.class));


    }
    @Test
    void getAllTeamsInfo_whenTeamsIsEmpty_shouldReturnEmptyList() {
        // Given
        List<Team> teams = new ArrayList<>();
        List<TeamInfoResponse> responses = new ArrayList<>();


        // When
        when(teamRepository.findAllQuery()).thenReturn(teams);

        // Then
        assertEquals(responses, teamService.getAllTeamsInfo());
        verify(teamRepository, times(1)).findAllQuery();


    }

    @Test
    void adminGetTeamInfo_whenTeamIdExists_shouldReturnTeamInfoResponse() {
        // Given
        Long teamId = 1L;
        Team team = new Team();
        team.setId(teamId);
        AdminTeamResponse response = new AdminTeamResponse(teamId, "a", "b", 1, 1, new ArrayList<>(), 1);
        // When
        when(teamRepository.findByIdQuery(teamId)).thenReturn(team);
        when(teamMapper.toAdminTeamResponse(any(Team.class))).thenReturn(response);
        // Then
        assertEquals(response, teamService.getTeamInfo(teamId));
        verify(teamRepository, times(1)).findByIdQuery(teamId);
    }

    @Test
    void updateTeam_whenTeamExists_shouldReturnTeamUpdateResponse() {
        // Given
        ArgumentCaptor<Team> teamCaptor = ArgumentCaptor.forClass(Team.class);

        Team existingTeam = new Team();
        existingTeam.setId(1L);
        existingTeam.setName("oldName");
        existingTeam.setCountry("oldCountry");

        Account account = new Account();
        account.setTeam(existingTeam);

        TeamUpdateRequest updateRequest = new TeamUpdateRequest("newName", "newCountry");
        TeamUpdateResponse expectedResponse = new TeamUpdateResponse(1L, "newName", "newCountry");

        // When
        when(accountService.getAccount()).thenReturn(account);
        when(teamRepository.save(any(Team.class))).thenReturn(existingTeam);
        when(teamMapper.toTeamUpdateResponse(any(Team.class))).thenReturn(expectedResponse);

        TeamUpdateResponse actualResponse = teamService.updateTeam(updateRequest);

        // Then
        verify(teamRepository).save(teamCaptor.capture());
        Team savedTeam = teamCaptor.getValue();

        assertEquals("newName", savedTeam.getName());
        assertEquals("newCountry", savedTeam.getCountry());
        assertEquals(1L, savedTeam.getId());

        assertEquals(expectedResponse, actualResponse);

    }

    @Test
    void adminUpdateTeam_whenBudgetIsNotNull_shouldReturnAdminTeamResponse() {
        // Given
        Long teamId = 1L;
        Team team = new Team();
        team.setId(teamId);
        AdminTeamUpdateRequest request = new AdminTeamUpdateRequest(teamId, "a", "b", 123);
        AdminTeamResponse response = new AdminTeamResponse(1L, "a", "b", 20, 123, new ArrayList<>(), 100);
        // When
        when(teamRepository.findByIdQuery(teamId)).thenReturn(team);
        when(teamRepository.save(any(Team.class))).thenReturn(team);
        when(teamMapper.toAdminTeamResponse(any(Team.class))).thenReturn(response);
        // Then
        assertEquals(response, teamService.updateTeam(teamId, request));
        verify(teamRepository, times(1)).findByIdQuery(teamId);
        verify(teamRepository, times(1)).save(team);
        verify(teamMapper, times(1)).toAdminTeamResponse(any(Team.class));

    }

    @Test
    void deleteTeam_whenTeamExists_shouldReturn() {
        // Given
        Team team = new Team();
        Account account = new Account();
        account.setTeam(team);
        //When
        when(accountService.getAccount()).thenReturn(account);
        when(accountRepository.save(any(Account.class))).thenReturn(account);
        // Then
        teamService.deleteTeam();
        verify(accountService, times(1)).getAccount();
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void deleteTeam_whenTeamIsNull_shouldThrowNoSuchTeamException() {
        // Given
        Account account = new Account();
        account.setTeam(null);
        //When
        when(accountService.getAccount()).thenReturn(account);
        // Then
        assertThrowsExactly(NoSuchTeamException.class, () -> teamService.deleteTeam());
        verify(accountService, times(1)).getAccount();
    }


    @Test
    void adminDeleteTeam_whenTeamExists_shouldReturn() {
        // Given
        Long teamId = 1L;
        Team team = new Team();
        team.setId(teamId);
        Account account = new Account();
        account.setTeam(team);

        // When
        when(teamRepository.findByIdQuery(teamId)).thenReturn(team);
        when(accountRepository.findByTeam(team)).thenReturn(Optional.of(account));

        // Then
        teamService.deleteTeam(teamId);
        verify(teamRepository, times(1)).findByIdQuery(teamId);
        verify(accountRepository, times(1)).findByTeam(team);
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(teamRepository, times(1)).delete(team);

    }

    @Test
    void adminDeleteTeam_whenTeamIsNull_shouldThrowNoSuchTeamException() {
        // Given
        Long teamId = 1L;

        // When
        when(teamRepository.findByIdQuery(teamId)).thenReturn(null);

        // Then
        assertThrowsExactly(NoSuchTeamException.class, () -> teamService.deleteTeam(teamId));
        verify(teamRepository, times(1)).findByIdQuery(teamId);
    }


    @Test
    void getTeamById_whenTeamExists_shouldReturnTeam() {
        // Given
        Long teamId = 1L;
        Team team = new Team();
        team.setId(teamId);
        // When
        when(teamRepository.findByIdQuery(teamId)).thenReturn(team);
        // Then
        assertEquals(team, teamService.getTeamById(teamId));
        verify(teamRepository, times(1)).findByIdQuery(teamId);
    }
}