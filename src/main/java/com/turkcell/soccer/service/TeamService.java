package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.request.AdminTeamUpdateRequest;
import com.turkcell.soccer.dto.request.TeamRequest;
import com.turkcell.soccer.dto.request.TeamUpdateRequest;
import com.turkcell.soccer.dto.response.AdminTeamResponse;
import com.turkcell.soccer.dto.response.TeamInfoResponse;
import com.turkcell.soccer.dto.response.TeamResponse;
import com.turkcell.soccer.dto.response.TeamUpdateResponse;
import com.turkcell.soccer.exception.NoSuchTeamException;
import com.turkcell.soccer.factory.RandomPlayerGeneration;
import com.turkcell.soccer.mapper.TeamMapper;
import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.model.Team;
import com.turkcell.soccer.repository.AccountRepository;
import com.turkcell.soccer.repository.TeamRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
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
            log.info("Create request ignored. User '{}' already has a team. Team ID: {}",
                    account.getUsername(), team.getId());
            return teamMapper.toTeamResponse(account.getTeam());
        }

        // Create the team
        team = new Team();
        team.setName(teamRequest.getName());
        team.setCountry(teamRequest.getCountry());
        team.setPlayers(RandomPlayerGeneration.initializeSquad(team));

        log.debug("Preparing team: Name: {}, Country: {}", team.getName(), team.getCountry());

        Team saved = teamRepository.save(team);
        log.info("Team saved: ID: {}, Name: {}", saved.getId(), saved.getName());
        // Save the changes in account to the DB (Addition of a team)
        account.setTeam(team);
        log.debug("Preparing accounts team field: Account: {}, TeamID: {}", account.getUsername(), team.getId());
        // Save the team to the DB
        accountRepository.save(account);
        log.info("Account saved: ID: {}, Name: {}", account.getId(), account.getUsername());

        return teamMapper.toTeamResponse(saved);

    }

    @Transactional
    public TeamInfoResponse getTeamInfo() {
        Team team = getTeam();
        return teamMapper.toTeamInfoResponse(team);
    }

    @Transactional
    public List<TeamInfoResponse> getAllTeamsInfo() {
        List<Team> allTeams = teamRepository.findAll();

        return allTeams.stream()
                .map(teamMapper::toTeamInfoResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminTeamResponse getTeamInfo(Long teamId) {
        Team team = getTeamFromRepo(teamId);


        return teamMapper.toAdminTeamResponse(team);
    }

    @Transactional
    public TeamUpdateResponse updateTeam(TeamUpdateRequest teamRequest) {
        Team team = getTeam();

        updateNameAndCountry(team, teamRequest.getName(), teamRequest.getCountry());
        Team saved = teamRepository.save(team);
        log.info("Team saved: ID: {}, Name: {}", saved.getId(), saved.getName());

        return teamMapper.toTeamUpdateResponse(saved);
    }

    @Transactional
    public AdminTeamResponse updateTeam(Long teamId, AdminTeamUpdateRequest teamRequest) {
        Team team = getTeamFromRepo(teamId);

        updateNameAndCountry(team, teamRequest.getName(), teamRequest.getCountry());

        if  (teamRequest.getBudget() != null) {
            log.debug("Budget will be updated: ID: {}, Old Budget: {}", team.getId(), team.getBudget());
            team.setBudget(teamRequest.getBudget());
            log.debug("Budget updated: ID: {}, New Budget: {}", team.getId(), team.getBudget());
        }
        Team saved = teamRepository.save(team);
        log.info("Team saved: ID: {}, Name: {}", saved.getId(), saved.getName());

        return teamMapper.toAdminTeamResponse(saved);
    }

    @Transactional
    public void deleteTeam() {
        Account account = accountService.getAccount();
        Team team = account.getTeam();

        if (team == null) {
            log.warn("Account does not have a team: ID: {}", account.getId());
            throw new NoSuchTeamException("Team with account: " +  account.getId() + " does not exist");
        }

        account.setTeam(null);
        accountRepository.save(account); // Break the link from account to team
        log.info("Accounts team deleted: Account ID: {}", account.getId());
        teamRepository.delete(team);
        log.info("Team deleted: ID: {}", team.getId());
    }

    @Transactional
    public void deleteTeam(Long teamId) {
        Team team = getTeamFromRepo(teamId);
        Optional<Account> accountOptional = accountRepository.findByTeam(team);

        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            account.setTeam(null);
            accountRepository.save(account);
            log.info("Accounts team deleted: Account ID: {}", account.getId());
        }

        teamRepository.delete(team);
        log.info("Team deleted: ID: {}", team.getId());
    }


    private void updateNameAndCountry(Team team, String name, String country) {
        if (name != null && !name.isBlank()) {
            team.setName(name);
        }
        if (country != null && !country.isBlank()) {
            team.setCountry(country);
        }
        log.debug("Team updated: Team name: {}, country: {}", team.getName(), team.getCountry());
    }

    public Team getTeam() {
        Account account = accountService.getAccount();
        Team team = account.getTeam();
        if (team == null) {
            log.warn("Account does not have a team: ID: {}", account.getId());
            throw new NoSuchTeamException("There is no team");
        }
        return team;
    }

    private Team getTeamFromRepo(Long teamId) {
        Team team  = teamRepository.findById(teamId).orElse(null);
        if (team == null) {
            log.warn("Team not found: ID: {}", teamId);
            throw new NoSuchTeamException("Team with id " + teamId + " does not exist");
        }
        return team;
    }

}
