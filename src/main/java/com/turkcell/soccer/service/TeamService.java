package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.request.AdminTeamUpdateRequest;
import com.turkcell.soccer.dto.request.TeamRequest;
import com.turkcell.soccer.dto.request.TeamUpdateRequest;
import com.turkcell.soccer.dto.response.AdminTeamResponse;
import com.turkcell.soccer.dto.response.TeamInfoResponse;
import com.turkcell.soccer.dto.response.TeamResponse;
import com.turkcell.soccer.dto.response.TeamUpdateResponse;
import com.turkcell.soccer.exception.AlreadyHasTeamException;
import com.turkcell.soccer.exception.NoSuchTeamException;
import com.turkcell.soccer.factory.RandomPlayerGeneration;
import com.turkcell.soccer.mapper.TeamMapper;
import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.model.Team;
import com.turkcell.soccer.repository.AccountRepository;
import com.turkcell.soccer.repository.TeamRepository;
import com.turkcell.soccer.mapper.PlayerMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final AccountRepository accountRepository;
    private final AccountService accountService;
    private final TeamMapper teamMapper;

    @Autowired
    public TeamService(TeamRepository teamRepository, AccountRepository accountRepository,
                       AccountService accountService, TeamMapper teamMapper) {
        this.accountRepository = accountRepository;
        this.teamRepository = teamRepository;
        this.accountService = accountService;
        this.teamMapper = teamMapper;
    }


    @Transactional
    public TeamResponse createTeam(TeamRequest teamRequest) {
        Account account = accountService.getAccount();

        Team team = account.getTeam();
        if (team != null) {
            return teamMapper.toTeamResponse(account.getTeam());
        }

        // Create the team
        team = new Team();
        team.setName(teamRequest.getName());
        team.setCountry(teamRequest.getCountry());
        team.setPlayers(RandomPlayerGeneration.initializeSquad(team));


        Team saved = teamRepository.save(team);
        // Save the changes in account to the DB (Addition of a team)
        account.setTeam(team);
        // Save the team to the DB
        accountRepository.save(account);

        return teamMapper.toTeamResponse(saved);

    }

    @Transactional
    public TeamInfoResponse getTeamInfo() {
        Team team = getTeam();
        return teamMapper.toTeamInfoResponse(team);
    }

    @Transactional
    public AdminTeamResponse getTeamInfo(Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new NoSuchTeamException("Team with id " + teamId + " does not exist")
        );

        return teamMapper.toAdminTeamResponse(team);
    }

    @Transactional
    public TeamUpdateResponse updateTeam(TeamUpdateRequest teamRequest) {
        Team team = getTeam();

        if (team == null) {
            throw new NoSuchTeamException("Account does not have a team");
        }

        updateNameAndCountry(team, teamRequest.getName(), teamRequest.getCountry());
        Team saved = teamRepository.save(team);

        return teamMapper.toTeamUpdateResponse(saved);
    }

    @Transactional
    public AdminTeamResponse updateTeam(Long teamId, AdminTeamUpdateRequest teamRequest) {
        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new NoSuchTeamException("Team with id " + teamId + " does not exist")
        );

        updateNameAndCountry(team, teamRequest.getName(), teamRequest.getCountry());

        if  (teamRequest.getBudget() != null) {
            team.setBudget(teamRequest.getBudget());
        }
        Team saved = teamRepository.save(team);

        return teamMapper.toAdminTeamResponse(saved);
    }

    @Transactional
    public void deleteTeam() {
        Account account = accountService.getAccount();
        Team team = account.getTeam();

        if (team == null) {
            throw new NoSuchTeamException("Team with account: " +  account.getId() + " does not exist");
        }

        account.setTeam(null);
        accountRepository.save(account); // Break the link from account to team

        teamRepository.delete(team);
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow(
                () -> new NoSuchTeamException("Team with id " + teamId + " does not exist")
        );
        Optional<Account> accountOptional = accountRepository.findByTeam(team);

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            account.setTeam(null);
            accountRepository.save(account);
        }

        teamRepository.delete(team);
    }


    private void updateNameAndCountry(Team team, String name, String country) {
        if (name != null && !name.isBlank()) {
            team.setName(name);
        }
        if (country != null && !country.isBlank()) {
            team.setCountry(country);
        }
    }

    public Team getTeam() {
        Account account = accountService.getAccount();
        Team team = account.getTeam();
        if (team == null)
            throw new NoSuchTeamException("There is no team");
        return team;
    }

}
