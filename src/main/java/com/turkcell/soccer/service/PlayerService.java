package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.PlayerDto;
import com.turkcell.soccer.dto.request.PlayerRequest;
import com.turkcell.soccer.dto.response.PlayerResponse;
import com.turkcell.soccer.exception.NoSuchPlayerException;
import com.turkcell.soccer.exception.NoSuchTeamException;
import com.turkcell.soccer.mapper.TeamMapper;
import com.turkcell.soccer.model.Player;
import com.turkcell.soccer.model.Team;
import com.turkcell.soccer.model.TransferList;
import com.turkcell.soccer.repository.PlayerRepository;
import com.turkcell.soccer.mapper.PlayerMapper;
import com.turkcell.soccer.repository.TeamRepository;
import com.turkcell.soccer.repository.TransferListRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Slf4j
@Service
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final PlayerMapper playerMapper;
    private final TeamService teamService;
    private final TeamRepository teamRepository;

    @Autowired
    public PlayerService(PlayerRepository playerRepository, PlayerMapper playerMapper,
                         TeamRepository teamRepository, TeamService teamService) {
        this.playerRepository = playerRepository;
        this.playerMapper = playerMapper;
        this.teamRepository = teamRepository;
        this.teamService = teamService;
    }

    @Transactional
    public List<PlayerDto> getAllPlayers() {
        Team team = teamService.getTeam();
        return playerMapper.toPlayerDtoList(team.getPlayers());
    }

    @Transactional
    public PlayerDto getPlayer(Long id) {
        Player player = getPlayerFromRepo(id);

        return playerMapper.playerToDto(player);
    }

    @Transactional
    public PlayerResponse createPlayer(PlayerRequest playerRequest) {
        Player player = new Player();

        setPlayerFields(playerRequest, player);
        playerRepository.save(player);

        log.info("Player created: ID: {} Name: {} {} TeamID: {}", player.getId(), player.getFirstName(),
                player.getLastName(), player.getTeam().getId());
        return playerMapper.playerToResponse(player);

    }


    @Transactional
    public PlayerResponse updatePlayer(Long id, PlayerRequest request) {
        Player player = getPlayerFromRepo(id);

        setPlayerFields(request, player);
        playerRepository.save(player);
        log.info("Player Updated: ID: {}, Name: {} {}", player.getId(), player.getFirstName(), player.getLastName());

        return playerMapper.playerToResponse(player);
    }

    @Transactional
    public void deletePlayer(Long id) {
        Player player = getPlayerFromRepo(id);
        player.getTeam().removePlayer(player);
        TransferList transferList = player.getTransferList();
        if (transferList != null) {
            transferList.setPlayer(null);
        }
        playerRepository.deleteById(id);
        log.info("Player deleted: ID: {}, Name: {} {}", player.getId(), player.getFirstName(), player.getLastName());
    }

    private void setPlayerFields(PlayerRequest playerRequest, Player player) {
        Team team = teamRepository.findByName(playerRequest.getTeam()).orElse(null);
        if (team == null) {
            log.warn("Team not found: Name: {}", playerRequest.getTeam());
            throw new NoSuchTeamException(playerRequest.getTeam());
        }

        player.setFirstName(playerRequest.getFirstName());
        player.setLastName(playerRequest.getLastName());
        player.setCountry(playerRequest.getCountry());
        player.setAge(playerRequest.getAge());
        player.setPosition(playerRequest.getPosition());
        player.setValue(playerRequest.getValue());
        player.setTeam(team);
        log.debug("Player fields set: ID: {}, Name: {} {}, Country: {}, Age: {}, Position: {}, Value: {}, Team: {}",
                player.getId(), player.getFirstName(), player.getLastName(), player.getCountry(),
                player.getAge(), player.getPosition(), player.getValue(), player.getTeam().getName());
    }

    private Player getPlayerFromRepo(Long playerId) {
        Player player = playerRepository.findById(playerId).orElse(null);

        if (player == null) {
            log.warn("Player lookup failed: Player with id {} not found", playerId);
            throw new NoSuchPlayerException("Player with id " + playerId + " not found");
        }

        return player;
    }


}
