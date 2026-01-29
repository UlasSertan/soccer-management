package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.PlayerDto;
import com.turkcell.soccer.dto.request.PlayerRequest;
import com.turkcell.soccer.dto.response.PlayerResponse;
import com.turkcell.soccer.exception.NoSuchPlayerException;
import com.turkcell.soccer.exception.NoSuchTeamException;
import com.turkcell.soccer.mapper.PlayerMapper;
import com.turkcell.soccer.model.Player;
import com.turkcell.soccer.model.Team;
import com.turkcell.soccer.model.TransferList;
import com.turkcell.soccer.repository.PlayerRepository;
import com.turkcell.soccer.repository.TeamRepository;
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
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private PlayerMapper playerMapper;
    @Mock
    private TeamService teamService;
    @Mock
    private TeamRepository teamRepository;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void getAllPlayers_whenTeamExists_shouldReturnPlayerDtoList() {
        // Given
        List<Player> playerList = new ArrayList<>();
        Team team = new Team();
        team.setPlayers(playerList);

        List<PlayerDto> playerDtoList = new ArrayList<>();
        // When
        when(teamService.getTeam()).thenReturn(team);
        when(playerMapper.toPlayerDtoList(team.getPlayers())).thenReturn(playerDtoList);

        //
        assertEquals(playerDtoList, playerService.getAllPlayers());
        verify(teamService, times(1)).getTeam();
        verify(playerMapper, times(1)).toPlayerDtoList(team.getPlayers());
    }

    @Test
    void getPlayer_whenPlayerExists_shouldReturnPlayerDto() {
        // Given
        Player player = new Player();
        player.setId(1L);
        PlayerDto playerDto = new PlayerDto(1L, "a", "b", "c", 1, 1, "a");

        // When
        when(playerRepository.findById(player.getId())).thenReturn(Optional.of(player));
        when(playerMapper.playerToDto(player)).thenReturn(playerDto);

        // Then
        assertEquals(playerDto, playerService.getPlayer(player.getId()));
        verify(playerRepository, times(1)).findById(player.getId());
        verify(playerMapper, times(1)).playerToDto(player);

    }

    @Test
    void getPlayerById_whenPlayerExists_shouldReturnPlayer() {
        // Given
        Long playerId = 1L;
        Player player = new Player();
        player.setId(playerId);

        // When
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // Then
        assertEquals(player, playerService.getPlayerById(playerId));
        verify(playerRepository, times(1)).findById(playerId);
    }

    @Test
    void getPlayerByIdAndTeam_whenPlayerExists_shouldReturnPlayer() {
        // Given
        Player player = new Player(); player.setId(1L);
        Long playerId = 1L;
        Long teamId = 1L;

        // When
        when(playerRepository.findByIdAndTeam_Id(playerId, teamId)).thenReturn(Optional.of(player));
        // Then
        assertEquals(player, playerService.getPlayerByIdAndTeam(playerId, teamId));
        verify(playerRepository, times(1)).findByIdAndTeam_Id(playerId, teamId);
    }

    @Test
    void getPlayerByIdAndTeam_whenPlayerNotExists_shouldReturnPlayer() {
        // Given
        Player player = new Player(); player.setId(1L);
        Long playerId = 1L;
        Long teamId = 1L;

        // When
        when(playerRepository.findByIdAndTeam_Id(playerId, teamId)).thenReturn(Optional.empty());
        // Then
        assertThrowsExactly(NoSuchPlayerException.class, () -> playerService.getPlayerByIdAndTeam(playerId, teamId));
        verify(playerRepository, times(1)).findByIdAndTeam_Id(playerId, teamId);
    }

    @Test
    void createPlayer_whenTeamExists_shouldReturnPlayerResponse() {
        // Given
        Player player = new Player(); player.setId(1L);
        Team team = new Team(); team.setId(1L);
        player.setTeam(team);
        PlayerRequest playerRequest = new PlayerRequest();
        playerRequest.setAge(20);
        playerRequest.setFirstName("a");
        playerRequest.setLastName("b");
        playerRequest.setTeam(team.getName());
        playerRequest.setCountry("a");
        playerRequest.setPosition("Forward");
        playerRequest.setValue(20);
        PlayerResponse playerResponse = new PlayerResponse();

        // When
        when(teamRepository.findByName(team.getName())).thenReturn(Optional.of(team));
        when(playerMapper.playerToResponse(any(Player.class))).thenReturn(playerResponse);

        // Then
        assertEquals(playerResponse, playerService.createPlayer(playerRequest));
        verify(playerMapper, times(1)).playerToResponse(any(Player.class));

    }

    @Test
    void updatePlayer_whenTeamExists_shouldReturnPlayerResponse() {
        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        // Given
        Player player = new Player(); player.setId(1L);
        player.setAge(20);
        Team team = new Team(); team.setId(1L);
        player.setTeam(team);
        PlayerRequest playerRequest = new PlayerRequest();
        playerRequest.setAge(30);
        playerRequest.setFirstName("a");
        playerRequest.setLastName("b");
        playerRequest.setTeam(team.getName());
        playerRequest.setCountry("a");
        playerRequest.setPosition("Forward");
        playerRequest.setValue(20);
        PlayerResponse playerResponse = new PlayerResponse();

        // When
        when(playerRepository.findById(anyLong())).thenReturn(Optional.of(player));
        when(teamRepository.findByName(team.getName())).thenReturn(Optional.of(team));
        when(playerMapper.playerToResponse(any(Player.class))).thenReturn(playerResponse);

        // Then
        assertEquals(playerResponse, playerService.updatePlayer(1L, playerRequest));
        verify(playerRepository, times(1)).findById(1L);
        verify(playerMapper, times(1)).playerToResponse(any(Player.class));
        verify(playerRepository).save(captor.capture());
        Player player1 = captor.getValue();
        assertEquals(30, player1.getAge());

    }

    @Test
    void updatePlayer_whenTeamNotExists_shouldReturnPlayerResponse() {
        ArgumentCaptor<Player> captor = ArgumentCaptor.forClass(Player.class);
        // Given
        Player player = new Player(); player.setId(1L);
        player.setTeam(null);
        PlayerRequest playerRequest = new PlayerRequest();
        playerRequest.setTeam("a");

        // When
        when(playerRepository.findById(anyLong())).thenReturn(Optional.of(player));
        when(teamRepository.findByName("a")).thenReturn(Optional.empty());

        // Then
        assertThrowsExactly(NoSuchTeamException.class, () -> playerService.updatePlayer(1L, playerRequest));
        verify(teamRepository, times(1)).findByName(anyString());
        verify(playerRepository, times(1)).findById(anyLong());
    }

    @Test
    void deletePlayer_whenPlayerExists_shouldDeletePlayer() {
        // Given
        Long playerId = 1L;
        Team team = new Team();
        team.setId(1L);

        Player player = new Player();
        player.setId(playerId);
        player.setFirstName("John");
        player.setLastName("Doe");
        player.setTeam(team);
        player.setTransferList(null);

        // When
        when(playerRepository.findById(anyLong())).thenReturn(Optional.of(player));

        // Then
        playerService.deletePlayer(playerId);
        verify(playerRepository, times(1)).findById(anyLong());
        verify(playerRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void deletePlayer_whenPlayerHasTransferList_shouldSetTransferListPlayerToNull() {
        // Given
        Long playerId = 1L;
        Team team = spy(new Team());
        team.setId(1L);

        TransferList transferList = new TransferList();

        Player player = new Player();
        player.setId(playerId);
        player.setFirstName("John");
        player.setLastName("Doe");
        player.setTeam(team);
        player.setTransferList(transferList);
        transferList.setPlayer(player);

        // When
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        playerService.deletePlayer(playerId);

        // Then
        assertNull(transferList.getPlayer());
        verify(team, times(1)).removePlayer(any(Player.class));
        verify(playerRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void deletePlayer_whenPlayerNotFound_shouldThrowException() {
        // Given
        Long playerId = 1L;

        // When
        when(playerRepository.findById(playerId)).thenReturn(Optional.empty());

        // Then
        assertThrowsExactly(NoSuchPlayerException.class, () -> playerService.deletePlayer(playerId));
        verify(playerRepository, times(0)).deleteById(anyLong());
    }

    @Test
    void deletePlayer_whenPlayerHasNoTransferList_shouldNotThrowNullPointerException() {
        // Given
        Long playerId = 1L;
        Team team = spy(new Team());
        team.setId(1L);

        Player player = new Player();
        player.setId(playerId);
        player.setFirstName("John");
        player.setLastName("Doe");
        player.setTeam(team);
        player.setTransferList(null); // No transfer list

        // When
        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        // Then
        assertDoesNotThrow(() -> playerService.deletePlayer(playerId));
        verify(team, times(1)).removePlayer(any(Player.class));
        verify(playerRepository, times(1)).deleteById(anyLong());
    }
}