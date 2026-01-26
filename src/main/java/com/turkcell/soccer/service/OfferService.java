package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.request.OfferCreationRequest;
import com.turkcell.soccer.dto.request.OfferUpdateRequest;
import com.turkcell.soccer.dto.response.OfferCreationResponse;
import com.turkcell.soccer.dto.response.OfferUpdateResponse;
import com.turkcell.soccer.exception.BadInputException;
import com.turkcell.soccer.exception.NoSuchPlayerException;
import com.turkcell.soccer.exception.NoSuchTeamException;
import com.turkcell.soccer.mapper.OfferMapper;
import com.turkcell.soccer.model.Offer;
import com.turkcell.soccer.model.Player;
import com.turkcell.soccer.model.Purchase;
import com.turkcell.soccer.model.Team;
import com.turkcell.soccer.repository.OfferRepository;
import com.turkcell.soccer.repository.PurchaseRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;

@Service
@Slf4j
public class OfferService {

    private final OfferRepository offerRepository;
    private final TeamService teamService;
    private final PlayerService playerService;
    private final OfferMapper offerMapper;
    private final PurchaseRepository purchaseRepository;

    @Autowired
    public OfferService(OfferRepository offerRepository, TeamService teamService,
                        PlayerService playerService, OfferMapper offerMapper,
                        PurchaseRepository purchaseRepository) {
        this.offerRepository = offerRepository;
        this.teamService = teamService;
        this.playerService = playerService;
        this.offerMapper = offerMapper;
        this.purchaseRepository = purchaseRepository;
    }

    @Transactional
    public OfferCreationResponse createOffer(OfferCreationRequest request) {

        Team sellerTeam = teamService.getTeamById(request.getSellerTeamId());
        if (sellerTeam == null) {
            log.warn("Team not found: ID: {}", request.getSellerTeamId());
            throw new NoSuchTeamException("Team not found");
        }
        Team buyerTeam = teamService.getTeam();
        if (sellerTeam.getId().equals(buyerTeam.getId())) {
            throw new IllegalArgumentException("You cannot make an offer to your own player.");
        }
        Player player = playerService.getPlayerByIdAndTeam(request.getPlayerId(), sellerTeam.getId());
        if (player == null) {
            log.warn("Player not found: ID: {} for Team: ID: {}", request.getPlayerId(), sellerTeam.getId());
            throw new NoSuchPlayerException("Player not found in that team");
        }

        Offer offer = new Offer();
        offer.setSellerTeam(sellerTeam);
        offer.setBuyerTeam(buyerTeam);
        offer.setPlayer(player);
        offer.setOfferedPrice(request.getOfferedPrice());
        offer.setCreatedAt(LocalDateTime.now());
        offer.setStatus(Offer.OfferStatus.PENDING);
        log.debug("Creating offer: ID: {}, Buyer: {}, Seller: {}, Player: {}, Price: {}, Time: {}",
                offer.getOfferId(), buyerTeam.getId(), sellerTeam.getId(), offer.getOfferedPrice(),
                offer.getCreatedAt(), offer.getCreatedAt());
        offerRepository.save(offer);

        log.info("Offer created successfully with ID: {}", offer.getOfferId());
        return offerMapper.toOfferCreationResponse(offer);
    }

    @Transactional
    public OfferCreationResponse getOffer(Long id) {
        Offer offer = checkOffer(id);
        return offerMapper.toOfferCreationResponse(offer);
    }

    @Transactional
    public List<OfferCreationResponse> getAllOffers(Long playerId) {
        if (playerService.getPlayerById(playerId) == null) {
            log.warn("Player not found: ID: {}", playerId);
            throw new NoSuchPlayerException("Player not found");
        }
        List<Offer> offers = offerRepository.findAllByPlayer_Id(playerId);
        return offerMapper.toOfferCreationResponseList(offers);
    }

    @Transactional
    public List<OfferCreationResponse> getAllOffersByTeam(Long teamId) {
        if(teamService.getTeamById(teamId) == null) {
            log.warn("Team not found: ID: {}", teamId);
            throw new NoSuchTeamException("Team not found");
        }
        List<Offer> offers  = offerRepository.findAllBySellerTeam_Id(teamId);
        return offerMapper.toOfferCreationResponseList(offers);
    }

    @Transactional
    public List<OfferCreationResponse> getOutgoingOffers(Long teamId) {
        if(teamService.getTeamById(teamId) == null) {
            log.warn("Team not found: ID: {}", teamId);
            throw new NoSuchTeamException("Team not found");
        }
        List<Offer> offers = offerRepository.findAllByBuyerTeam_Id(teamId);
        return offerMapper.toOfferCreationResponseList(offers);
    }


    @Transactional
    public OfferUpdateResponse updateOffer(OfferUpdateRequest request) {

        Offer offer = checkOffer(request.getOfferId());
        if (offer.getStatus() != Offer.OfferStatus.PENDING) {
            throw new IllegalStateException("Cannot update/cancel/reject an offer that is not in PENDING status.");
        }
        isAuthorizedBuyer(offer);
        offer.setOfferedPrice(request.getOfferedPrice());
        offer.setCreatedAt(LocalDateTime.now());
        log.debug("Updating offer: ID: {}, Price: {}, Time: {}", offer.getOfferId(),
                offer.getOfferedPrice(), offer.getCreatedAt());
        log.info("Offer updated: {}", offer.getOfferId());
        return offerMapper.toOfferUpdateResponse(offerRepository.save(offer));

    }

    @Transactional
    public void cancelOffer(Long id) {
        Offer offer = checkOffer(id);
        if (offer.getStatus() != Offer.OfferStatus.PENDING) {
            throw new IllegalStateException("Cannot update/cancel/reject an offer that is not in PENDING status.");
        }
        isAuthorizedBuyer(offer);
        offer.setStatus(Offer.OfferStatus.CANCELLED);
        log.debug("Offer cancelled: {}, Status: {}", offer.getOfferId(),  offer.getStatus());
        log.info("Offer cancelled: {}", offer.getOfferId());
    }

    @Transactional
    public void rejectOffer(Long id) {
        Offer offer = checkOffer(id);
        if (offer.getStatus() != Offer.OfferStatus.PENDING) {
            throw new IllegalStateException("Cannot update/cancel/reject an offer that is not in PENDING status.");
        }
        isAuthorizedSeller(offer);
        offer.setStatus(Offer.OfferStatus.REJECTED);
        log.debug("Offer rejected: {}, Status: {}", offer.getOfferId(),  offer.getStatus());
        log.info("Offer rejected: {}", offer.getOfferId());
    }

    @Transactional
    public void acceptOffer(Long id) {
        Offer offer = checkOffer(id);
        isAuthorizedSeller(offer);
        if (offer.getStatus() != Offer.OfferStatus.PENDING) {
            log.warn("WARNING: ACCESS TO RESOLVED OFFER");
            throw new AccessDeniedException("Access denied");
        }

        int price =  offer.getOfferedPrice();
        Team buyer = offer.getBuyerTeam();
        Team seller = offer.getSellerTeam();
        Player player = offer.getPlayer();


        if (buyer.getBudget() < offer.getOfferedPrice()) {
            log.warn("Budget: {} is less than Price: {}!", buyer.getBudget(), offer.getOfferedPrice());
            throw new BadInputException("Your do not have enough budget to buy this player!");
        }
        log.debug("Purchase: Budget Before: {}, PlayerID: {}, BuyerID: {}", buyer.getBudget(), player.getId(), buyer.getId());
        log.debug("Purchase: Budget Before: {}, PlayerID: {}, SellerID: {}", seller.getBudget(), player.getId(), seller.getId());

        buyer.setBudget(buyer.getBudget() - price);
        seller.setBudget(seller.getBudget() + price);

        log.debug("Purchase: Budget After: {}, PlayerID: {}, BuyerID: {}", buyer.getBudget(), player.getId(), buyer.getId());
        log.debug("Purchase: Budget After: {}, PlayerID: {}, SellerID: {}", seller.getBudget(), player.getId(), seller.getId());

        Random random = new Random();
        int increasePercent = random.nextInt(10, 101);
        int playerValue = player.getValue();
        log.debug("Purchase Player Value: PlayerID: {}, Value Before: {}", player.getId(), playerValue);
        player.setValue((increasePercent + 100) * playerValue / 100);
        player.setTeam(buyer);
        log.debug("Purchase Player Value: PlayerID: {}, Value After: {}", player.getId(), player.getValue());

        seller.setPlayerCount(seller.getPlayerCount() - 1);
        buyer.setPlayerCount(buyer.getPlayerCount() + 1);
        offer.setStatus(Offer.OfferStatus.ACCEPTED);
        offerRepository.save(offer);

        List<Offer> otherOffers = offerRepository.findAllByPlayer_IdAndStatus(player.getId(), Offer.OfferStatus.PENDING);
        for (Offer other : otherOffers) {
            if (!other.getOfferId().equals(offer.getOfferId())) {
                other.setStatus(Offer.OfferStatus.REJECTED);
            }
        }

        Purchase purchase = new Purchase();
        purchase.setPlayerId(player.getId());
        purchase.setSellerId(seller.getId());
        purchase.setPrice(offer.getOfferedPrice());
        purchase.setBuyerId(buyer.getId());
        purchase.setCreatedAt(LocalDateTime.now());
        purchaseRepository.save(purchase);

        log.debug("Accepted offer saved: OfferID: {},  PlayerID: {}, SellerID: {}, BuyerID: {}, Price: {}, Time: {}", offer.getOfferId(), player.getId(),
              seller.getId(),  buyer.getId(), offer.getOfferedPrice(), LocalDateTime.now());
        log.info("Purchase successful");
    }




    private Offer checkOffer(Long offerId){
        Offer offer = offerRepository.findById(offerId).orElse(null);
        if (offer == null) {
            log.warn("Offer not found: ID: {}", offerId);
            throw new NoSuchElementException("Offer not found");
        }
        return offer;
    }

    private void isAuthorizedBuyer(Offer offer) {
        Team buyerTeam = teamService.getTeam();
        if (!Objects.equals(offer.getBuyerTeam().getId(), buyerTeam.getId())) {
            log.warn("Team with ID: {} cannot update offer with ID: {}", buyerTeam.getId(), offer.getOfferId());
            throw new AccessDeniedException("You do not have permission to perform this action!");
        }
    }

    private void isAuthorizedSeller(Offer offer) {
        Team sellerTeam = teamService.getTeam();
        if (!Objects.equals(offer.getSellerTeam().getId(), sellerTeam.getId())) {
            log.warn("Team with ID: {} cannot update offer with ID: {}", sellerTeam.getId(), offer.getOfferId());
            throw new AccessDeniedException("You do not have permission to perform this action!");
        }
    }





}
