package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.PlayerDto;
import com.turkcell.soccer.dto.request.PlayerRequest;
import com.turkcell.soccer.dto.response.PlayerResponse;
import com.turkcell.soccer.exception.NoSuchPlayerException;
import com.turkcell.soccer.exception.NoSuchTeamException;
import com.turkcell.soccer.mapper.TeamMapper;
import com.turkcell.soccer.model.Player;
import com.turkcell.soccer.model.Team;
import com.turkcell.soccer.repository.PlayerRepository;
import com.turkcell.soccer.mapper.PlayerMapper;
import com.turkcell.soccer.repository.TeamRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
        List<PlayerDto> players = getAllPlayers();

        for (PlayerDto player : players) {
            if (player.getId().equals(id)) {
                return player;
            }

        }

        throw new NoSuchPlayerException("Player with name " + id + " not found");

    }

    @Transactional
    public PlayerResponse createPlayer(PlayerRequest playerRequest) {
        Player player = new Player();

        setPlayerFields(playerRequest, player);

        return playerMapper.playerToResponse(player);

    }


    @Transactional
    public PlayerResponse updatePlayer(Long id, PlayerRequest request) {
        Player player = playerRepository.findById(id).orElseThrow(
                () ->  new NoSuchPlayerException("Player with id " + id + " not found")
        );

        setPlayerFields(request, player);

        return playerMapper.playerToResponse(player);
    }

    @Transactional
    public void deletePlayer(Long id) {
        Player player = playerRepository.findById(id).orElseThrow(
                () ->  new NoSuchPlayerException("Player with id " + id + " not found")
        );

        playerRepository.delete(player);
    }

    private void setPlayerFields(PlayerRequest playerRequest, Player player) {
        Team team = teamRepository.findByName(playerRequest.getTeam()).orElseThrow(
                () ->  new NoSuchTeamException(playerRequest.getTeam())
        );

        player.setFirstName(playerRequest.getFirstName());
        player.setLastName(playerRequest.getLastName());
        player.setCountry(playerRequest.getCountry());
        player.setAge(playerRequest.getAge());
        player.setPosition(playerRequest.getPosition());
        player.setValue(playerRequest.getValue());
        player.setTeam(team);
        playerRepository.save(player);
    }



}
