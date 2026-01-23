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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
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
        ).orElse(null);

        if (player == null) {
            log.warn("Player not found with TeamID: {}, playerId: {}", team.getId(), request.getPlayerId());
            throw new NoSuchPlayerException("Player not found!");
        }

        if (transferListRepository.existsByPlayer(player)) {
            log.warn("Player with ID: {} already in the transfer list!", player.getId());
            throw new IllegalStateException("This player is already on the transfer list!");
        }

        if (request.getPrice() < 0) {
            log.warn("Price cannot be negative!");
            throw new BadInputException("Price cannot be less than 0");
        }

        TransferList transferList = new TransferList();
        transferList.setPrice(request.getPrice());
        transferList.setPlayer(player);
        log.debug("Set Transfer list fields: Price: {}, PlayerID: {}",  transferList.getPrice(), player.getId());
        TransferList saved = transferListRepository.save(transferList);
        log.info("Transfer list saved: ID: {}, PlayerID: {}, Price: {}", saved.getId(), player.getId(), transferList.getPrice());
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
        log.debug("Update Transfer List: New Price: {}, PlayerID: {}", transferList.getPrice(), playerId);
        TransferList saved = transferListRepository.save(transferList);
        log.info("Transfer list saved: ID: {}, PlayerID: {}", saved.getId(), playerId);

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
        log.info("Delete Transfer List: ID: {}, PlayerID: {}", transferList.getId(), playerId);
    }

    @Transactional
    public PurchaseResponse purchasePlayer(Long playerId) {
        Team buyerTeam = teamService.getTeam();
        int budget = buyerTeam.getBudget();
        log.debug("Purchase: Budget Before: {}, PlayerID: {}, BuyerID: {}", budget, playerId, buyerTeam.getId());
        TransferList transferListEntry = transferListRepository.findByPlayer_Id(playerId);
        if  (transferListEntry == null) {
            log.warn("Transfer List not found with playerId: {}", playerId);
            throw new PlayerNotInTransferListException("Player not in transfer list!");
        }

        if (buyerTeam.getId().equals(transferListEntry.getPlayer().getTeam().getId())) {
            log.warn("Player with ID: {} already in team with TeamID: {}!", playerId, buyerTeam.getId());
            throw new IllegalStateException("You cannot buy your own player!");
        }

        if (budget < transferListEntry.getPrice()) {
            log.warn("Budget: {} is less than Price: {}!", budget, transferListEntry.getPrice());
            throw new  BadInputException("Your do not have enough budget to buy this player!");
        }
        Player player = transferListEntry.getPlayer();
        Team sellerTeam = transferListEntry.getPlayer().getTeam();
        log.debug("Purchase: Budget Before: {}, PlayerID: {}, SellerID: {}", sellerTeam.getBudget(), playerId, sellerTeam.getId());


        budget -= transferListEntry.getPrice();
        buyerTeam.setBudget(budget);
        log.debug("Purchase: Budget After: {}, PlayerID: {}, BuyerID: {}", buyerTeam.getBudget(), playerId, buyerTeam.getId());
        sellerTeam.setBudget(sellerTeam.getBudget() + transferListEntry.getPrice());
        log.debug("Purchase: Budget After: {}, PlayerID: {}, SellerID: {}", sellerTeam.getBudget(), playerId, sellerTeam.getId());
        log.info("Purchase: Budgets updated");

        Random random = new Random();
        int increasePercent = random.nextInt(10, 101);
        int playerValue = player.getValue();
        log.debug("Purchase Player Value: PlayerID: {}, Value Before: {}", player.getId(), playerValue);

        player.setValue((increasePercent + 100) * playerValue / 100);

        log.debug("Purchase Player Value: PlayerID: {}, Value After: {}", player.getId(), player.getValue());

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
        log.debug("Purchase saved: PurchaseID: {},  PlayerID: {}, SellerID: {}, BuyerID: {}, Price: {}, Time: {}", purchase.getId(), playerId,
                purchase.getSellerId(),  purchase.getBuyerId(), purchase.getPrice(), purchase.getCreatedAt());
        log.info("Purchase successful");
        return transferListMapper.toPurchaseResponse(purchase);

    }


}
