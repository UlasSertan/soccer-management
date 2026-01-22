package com.turkcell.soccer.security.common;

import com.turkcell.soccer.exception.NoSuchTeamException;
import com.turkcell.soccer.exception.PlayerNotInTransferListException;
import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.model.Role;
import com.turkcell.soccer.model.Team;
import com.turkcell.soccer.model.TransferList;
import com.turkcell.soccer.repository.TransferListRepository;
import com.turkcell.soccer.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransferListSecurity {

    private final TransferListRepository transferListRepository;
    private final AccountService accountService;

    public TransferList getListingIfAuthorized(Long playerId) {

        TransferList listedPlayer =  transferListRepository.findByPlayer_Id(playerId);

        if (listedPlayer == null) {
                throw new PlayerNotInTransferListException("Player is not in the transfer list");
        };

        Account account = accountService.getAccount();
        boolean isAdmin = account.getRole().getName().equals(Role.RoleName.ADMIN.authority());

        if (isAdmin)
            return listedPlayer;

        Team team = account.getTeam();
        if (team == null) {
            throw new NoSuchTeamException("The account does not have a team");
        }

        Long teamId = team.getId();
        Long playerTeamId = listedPlayer.getPlayer().getTeam().getId();

        if (!teamId.equals(playerTeamId)) {
            throw new AccessDeniedException("You are not allowed to perform this operation");
        }

        return listedPlayer;


    }

}
