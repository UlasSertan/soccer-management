package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.TransferListDto;
import com.turkcell.soccer.dto.TransferListFilter;
import com.turkcell.soccer.dto.request.TransferListRequest;
import com.turkcell.soccer.dto.request.PurchaseRequest;
import com.turkcell.soccer.dto.response.TransferListAdditionResponse;
import com.turkcell.soccer.dto.response.TransferListInfoResponse;
import com.turkcell.soccer.dto.response.PurchaseResponse;
import com.turkcell.soccer.exception.BadInputException;
import com.turkcell.soccer.exception.NoSuchPlayerException;
import com.turkcell.soccer.exception.PlayerNotInTransferListException;
import com.turkcell.soccer.mapper.TransferListMapper;
import com.turkcell.soccer.model.*;
import com.turkcell.soccer.repository.PlayerRepository;
import com.turkcell.soccer.repository.PurchaseRepository;
import com.turkcell.soccer.repository.TeamRepository;
import com.turkcell.soccer.repository.TransferListRepository;
import com.turkcell.soccer.security.common.TransferListSecurity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class TransferListService {

    private final TeamRepository teamRepository;
    private final PurchaseRepository purchaseRepository;
    TransferListRepository transferListRepository;
    TeamService teamService;
    PlayerRepository playerRepository;
    TransferListMapper transferListMapper;
    TransferListSecurity transferListSecurity;

    @Autowired
    public TransferListService(TransferListRepository transferListRepository, TeamService teamService,
                               PlayerRepository playerRepository, TransferListMapper transferListMapper,
                               TransferListSecurity transferListSecurity, TeamRepository teamRepository, PurchaseRepository purchaseRepository) {
        this.transferListRepository = transferListRepository;
        this.teamService = teamService;
        this.playerRepository = playerRepository;
        this.transferListMapper = transferListMapper;
        this.transferListSecurity = transferListSecurity;
        this.teamRepository = teamRepository;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public TransferListAdditionResponse addToTransferList(TransferListRequest.Add request) {
        Team team = teamService.getTeam();
        Player player = playerRepository.findByIdAndTeam_Id(
                request.getPlayerId(), team.getId()
        ).orElseThrow(
                () -> new NoSuchPlayerException("Player not found!")
        );

        if (transferListRepository.existsByPlayer(player)) {
            throw new IllegalStateException("This player is already on the transfer list!");
        }

        if (request.getPrice() < 0) {
            throw new BadInputException("Price cannot be less than 0");
        }

        TransferList transferList = new TransferList();
        transferList.setPrice(request.getPrice());
        transferList.setPlayer(player);
        TransferList saved = transferListRepository.save(transferList);

        return transferListMapper.toAdditionResponse(saved);
    }


    @Transactional
    public TransferListInfoResponse getTransferList(TransferListFilter filter) {

        Specification<TransferList> spec = Specification
                .where(TransferSpecification.hasPlayerName(filter.getPlayerName()))
                .and(TransferSpecification.hasTeamName(filter.getTeamName()))
                .and(TransferSpecification.hasCountry(filter.getCountry()))
                .and(TransferSpecification.minValue(filter.getMinPrice()))
                .and(TransferSpecification.maxValue(filter.getMaxPrice()));

        List<TransferList> transferLists = transferListRepository.findAll(spec);

        return transferListMapper.toTransferListInfoResponse(
                transferListMapper.transferListToDtoList(transferLists)
        );

    }

    @Transactional
    public TransferListInfoResponse updateTransferList(Long playerId, TransferListRequest.UpdatePrice request) {
        TransferList transferList = transferListSecurity.getListingIfAuthorized(playerId);

        transferList.setPrice(request.getPrice());
        TransferList saved = transferListRepository.save(transferList);

        List<TransferListDto> players = new ArrayList<>();
        players.add(transferListMapper.transferListToDto(saved));

        return transferListMapper.toTransferListInfoResponse(players);
    }

    @Transactional
    public void deleteTransferList(Long playerId) {
        TransferList transferList = transferListSecurity.getListingIfAuthorized(playerId);
        Player player = transferList.getPlayer();
        transferList.setPlayer(null);
        player.setTransferList(null);
        transferListRepository.delete(transferList);
    }

    @Transactional
    public PurchaseResponse purchasePlayer(Long playerId) {
        Team buyerTeam = teamService.getTeam();
        int budget = buyerTeam.getBudget();

        TransferList transferListEntry = transferListRepository.findByPlayer_Id(playerId);
        if  (transferListEntry == null) {
            throw new PlayerNotInTransferListException("Player not in transfer list!");
        }

        if (buyerTeam.getId().equals(transferListEntry.getPlayer().getTeam().getId())) {
            throw new IllegalStateException("You cannot buy your own player!");
        }

        if (budget < transferListEntry.getPrice()) {
            throw new  BadInputException("Your do not have enough budget to buy this player!");
        }
        Player player = transferListEntry.getPlayer();
        Team sellerTeam = transferListEntry.getPlayer().getTeam();

        budget -= transferListEntry.getPrice();
        buyerTeam.setBudget(budget);
        sellerTeam.setBudget(sellerTeam.getBudget() + transferListEntry.getPrice());

        Random random = new Random();
        int increasePercent = random.nextInt(10, 101);
        int playerValue = player.getValue();

        player.setValue((increasePercent + 100) * playerValue / 100);
        player.setTeam(buyerTeam);
        player.setTransferList(null);
        playerRepository.save(player);

        sellerTeam.setPlayerCount(sellerTeam.getPlayerCount() - 1);
        buyerTeam.setPlayerCount(buyerTeam.getPlayerCount() + 1);
        teamRepository.save(sellerTeam);
        teamRepository.save(buyerTeam);

        Purchase purchase = new Purchase();
        purchase.setPlayerId(playerId);
        purchase.setSellerId(sellerTeam.getId());
        purchase.setPrice(transferListEntry.getPrice());
        purchase.setBuyerId(buyerTeam.getId());
        purchase.setCreatedAt(LocalDateTime.now());
        player.setTransferList(null);
        transferListEntry.setPlayer(null);
        transferListRepository.delete(transferListEntry);

        purchaseRepository.save(purchase);
        return transferListMapper.toPurchaseResponse(purchase);

    }


}
