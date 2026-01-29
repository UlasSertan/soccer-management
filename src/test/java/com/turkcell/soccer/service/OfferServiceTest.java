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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;
    @Mock
    private TeamService teamService;
    @Mock
    private PlayerService playerService;
    @Mock
    private OfferMapper offerMapper;
    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private OfferService offerService;

    @Test
    void createOffer_whenValidRequest_shouldReturnOfferCreationResponse() {
        // Given
        Long sellerTeamId = 1L;
        Long buyerTeamId = 2L;
        Long playerId = 1L;
        Integer offeredPrice = 1000000;

        Team sellerTeam = new Team();
        sellerTeam.setId(sellerTeamId);

        Team buyerTeam = new Team();
        buyerTeam.setId(buyerTeamId);

        Player player = new Player();
        player.setId(playerId);
        player.setTeam(sellerTeam);

        OfferCreationRequest request = OfferCreationRequest.builder()
                .sellerTeamId(sellerTeamId)
                .playerId(playerId)
                .offeredPrice(offeredPrice)
                .build();

        Offer savedOffer = new Offer();
        savedOffer.setOfferId(1L);
        savedOffer.setSellerTeam(sellerTeam);
        savedOffer.setBuyerTeam(buyerTeam);
        savedOffer.setPlayer(player);
        savedOffer.setOfferedPrice(offeredPrice);
        savedOffer.setStatus(Offer.OfferStatus.PENDING);

        OfferCreationResponse expectedResponse = OfferCreationResponse.builder()
                .offerId(1L)
                .sellerTeamId(sellerTeamId)
                .buyerTeamId(buyerTeamId)
                .playerId(playerId)
                .offeredPrice(offeredPrice)
                .status(Offer.OfferStatus.PENDING)
                .build();

        // When
        when(teamService.getTeamById(sellerTeamId)).thenReturn(sellerTeam);
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(playerService.getPlayerByIdAndTeam(playerId, sellerTeamId)).thenReturn(player);
        when(offerRepository.save(any(Offer.class))).thenReturn(savedOffer);
        when(offerMapper.toOfferCreationResponse(any(Offer.class))).thenReturn(expectedResponse);

        OfferCreationResponse actualResponse = offerService.createOffer(request);

        // Then
        assertEquals(expectedResponse, actualResponse);
        verify(teamService, times(1)).getTeamById(sellerTeamId);
        verify(teamService, times(1)).getTeam();
        verify(playerService, times(1)).getPlayerByIdAndTeam(playerId, sellerTeamId);
        verify(offerRepository, times(1)).save(any(Offer.class));
        verify(offerMapper, times(1)).toOfferCreationResponse(any(Offer.class));
    }

    @Test
    void createOffer_whenSellerTeamNotFound_shouldThrowNoSuchTeamException() {
        // Given
        OfferCreationRequest request = OfferCreationRequest.builder()
                .sellerTeamId(1L)
                .playerId(1L)
                .offeredPrice(1000000)
                .build();

        // When
        when(teamService.getTeamById(1L)).thenReturn(null);

        // Then
        assertThrowsExactly(NoSuchTeamException.class, () -> offerService.createOffer(request));
        verify(teamService, times(1)).getTeamById(1L);
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void createOffer_whenOfferToOwnPlayer_shouldThrowIllegalArgumentException() {
        // Given
        Long teamId = 1L;
        Team team = new Team();
        team.setId(teamId);

        OfferCreationRequest request = OfferCreationRequest.builder()
                .sellerTeamId(teamId)
                .playerId(1L)
                .offeredPrice(1000000)
                .build();

        // When
        when(teamService.getTeamById(teamId)).thenReturn(team);
        when(teamService.getTeam()).thenReturn(team);

        // Then
        assertThrowsExactly(IllegalArgumentException.class, () -> offerService.createOffer(request));
        verify(teamService, times(1)).getTeamById(teamId);
        verify(teamService, times(1)).getTeam();
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void createOffer_whenPlayerNotFoundInTeam_shouldThrowNoSuchPlayerException() {
        // Given
        Long sellerTeamId = 1L;
        Long buyerTeamId = 2L;
        Long playerId = 1L;

        Team sellerTeam = new Team();
        sellerTeam.setId(sellerTeamId);

        Team buyerTeam = new Team();
        buyerTeam.setId(buyerTeamId);

        OfferCreationRequest request = OfferCreationRequest.builder()
                .sellerTeamId(sellerTeamId)
                .playerId(playerId)
                .offeredPrice(1000000)
                .build();

        // When
        when(teamService.getTeamById(sellerTeamId)).thenReturn(sellerTeam);
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(playerService.getPlayerByIdAndTeam(playerId, sellerTeamId)).thenReturn(null);

        // Then
        assertThrowsExactly(NoSuchPlayerException.class, () -> offerService.createOffer(request));
        verify(playerService, times(1)).getPlayerByIdAndTeam(playerId, sellerTeamId);
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void getOffer_whenOfferExists_shouldReturnOfferCreationResponse() {
        // Given
        Long offerId = 1L;
        Offer offer = new Offer();
        offer.setOfferId(offerId);

        OfferCreationResponse expectedResponse = OfferCreationResponse.builder()
                .offerId(offerId)
                .build();

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));
        when(offerMapper.toOfferCreationResponse(offer)).thenReturn(expectedResponse);

        OfferCreationResponse actualResponse = offerService.getOffer(offerId);

        // Then
        assertEquals(expectedResponse, actualResponse);
        verify(offerRepository, times(1)).findById(offerId);
        verify(offerMapper, times(1)).toOfferCreationResponse(offer);
    }

    @Test
    void getOffer_whenOfferNotFound_shouldThrowNoSuchElementException() {
        // Given
        Long offerId = 1L;

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.empty());

        // Then
        assertThrowsExactly(NoSuchElementException.class, () -> offerService.getOffer(offerId));
        verify(offerRepository, times(1)).findById(offerId);
        verify(offerMapper, never()).toOfferCreationResponse(any(Offer.class));
    }

    @Test
    void getAllOffers_whenPlayerExists_shouldReturnListOfOfferCreationResponse() {
        // Given
        Long playerId = 1L;
        Player player = new Player();
        player.setId(playerId);

        List<Offer> offers = new ArrayList<>();
        Offer offer1 = new Offer();
        offer1.setOfferId(1L);
        offers.add(offer1);

        List<OfferCreationResponse> expectedResponses = new ArrayList<>();
        OfferCreationResponse response1 = OfferCreationResponse.builder()
                .offerId(1L)
                .build();
        expectedResponses.add(response1);

        // When
        when(playerService.getPlayerById(playerId)).thenReturn(player);
        when(offerRepository.findAllByPlayer_Id(playerId)).thenReturn(offers);
        when(offerMapper.toOfferCreationResponseList(offers)).thenReturn(expectedResponses);

        List<OfferCreationResponse> actualResponses = offerService.getAllOffers(playerId);

        // Then
        assertEquals(expectedResponses, actualResponses);
        verify(playerService, times(1)).getPlayerById(playerId);
        verify(offerRepository, times(1)).findAllByPlayer_Id(playerId);
        verify(offerMapper, times(1)).toOfferCreationResponseList(offers);
    }

    @Test
    void getAllOffers_whenPlayerNotFound_shouldThrowNoSuchPlayerException() {
        // Given
        Long playerId = 1L;

        // When
        when(playerService.getPlayerById(playerId)).thenReturn(null);

        // Then
        assertThrowsExactly(NoSuchPlayerException.class, () -> offerService.getAllOffers(playerId));
        verify(playerService, times(1)).getPlayerById(playerId);
        verify(offerRepository, never()).findAllByPlayer_Id(anyLong());
    }

    @Test
    void getAllOffersByTeam_whenTeamExists_shouldReturnListOfOfferCreationResponse() {
        // Given
        Long teamId = 1L;
        Team team = new Team();
        team.setId(teamId);

        List<Offer> offers = new ArrayList<>();
        Offer offer1 = new Offer();
        offer1.setOfferId(1L);
        offers.add(offer1);

        List<OfferCreationResponse> expectedResponses = new ArrayList<>();
        OfferCreationResponse response1 = OfferCreationResponse.builder()
                .offerId(1L)
                .build();
        expectedResponses.add(response1);

        // When
        when(teamService.getTeamById(teamId)).thenReturn(team);
        when(offerRepository.findAllBySellerTeam_Id(teamId)).thenReturn(offers);
        when(offerMapper.toOfferCreationResponseList(offers)).thenReturn(expectedResponses);

        List<OfferCreationResponse> actualResponses = offerService.getAllOffersByTeam(teamId);

        // Then
        assertEquals(expectedResponses, actualResponses);
        verify(teamService, times(1)).getTeamById(teamId);
        verify(offerRepository, times(1)).findAllBySellerTeam_Id(teamId);
        verify(offerMapper, times(1)).toOfferCreationResponseList(offers);
    }

    @Test
    void getAllOffersByTeam_whenTeamNotFound_shouldThrowNoSuchTeamException() {
        // Given
        Long teamId = 1L;

        // When
        when(teamService.getTeamById(teamId)).thenReturn(null);

        // Then
        assertThrowsExactly(NoSuchTeamException.class, () -> offerService.getAllOffersByTeam(teamId));
        verify(teamService, times(1)).getTeamById(teamId);
        verify(offerRepository, never()).findAllBySellerTeam_Id(anyLong());
    }

    @Test
    void getOutgoingOffers_whenTeamExists_shouldReturnListOfOfferCreationResponse() {
        // Given
        Long teamId = 1L;
        Team team = new Team();
        team.setId(teamId);

        List<Offer> offers = new ArrayList<>();
        Offer offer1 = new Offer();
        offer1.setOfferId(1L);
        offers.add(offer1);

        List<OfferCreationResponse> expectedResponses = new ArrayList<>();
        OfferCreationResponse response1 = OfferCreationResponse.builder()
                .offerId(1L)
                .build();
        expectedResponses.add(response1);

        // When
        when(teamService.getTeamById(teamId)).thenReturn(team);
        when(offerRepository.findAllByBuyerTeam_Id(teamId)).thenReturn(offers);
        when(offerMapper.toOfferCreationResponseList(offers)).thenReturn(expectedResponses);

        List<OfferCreationResponse> actualResponses = offerService.getOutgoingOffers(teamId);

        // Then
        assertEquals(expectedResponses, actualResponses);
        verify(teamService, times(1)).getTeamById(teamId);
        verify(offerRepository, times(1)).findAllByBuyerTeam_Id(teamId);
        verify(offerMapper, times(1)).toOfferCreationResponseList(offers);
    }

    @Test
    void getOutgoingOffers_whenTeamNotFound_shouldThrowNoSuchTeamException() {
        // Given
        Long teamId = 1L;

        // When
        when(teamService.getTeamById(teamId)).thenReturn(null);

        // Then
        assertThrowsExactly(NoSuchTeamException.class, () -> offerService.getOutgoingOffers(teamId));
        verify(teamService, times(1)).getTeamById(teamId);
        verify(offerRepository, never()).findAllByBuyerTeam_Id(anyLong());
    }

    @Test
    void updateOffer_whenValidRequest_shouldReturnOfferUpdateResponse() {
        // Given
        Long offerId = 1L;
        Long buyerTeamId = 1L;
        Integer newPrice = 2000000;

        Team buyerTeam = new Team();
        buyerTeam.setId(buyerTeamId);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setBuyerTeam(buyerTeam);
        existingOffer.setStatus(Offer.OfferStatus.PENDING);
        existingOffer.setOfferedPrice(1000000);

        OfferUpdateRequest request = OfferUpdateRequest.builder()
                .offerId(offerId)
                .offeredPrice(newPrice)
                .build();

        Offer updatedOffer = new Offer();
        updatedOffer.setOfferId(offerId);
        updatedOffer.setOfferedPrice(newPrice);

        OfferUpdateResponse expectedResponse = new OfferUpdateResponse();

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(offerRepository.save(any(Offer.class))).thenReturn(updatedOffer);
        when(offerMapper.toOfferUpdateResponse(updatedOffer)).thenReturn(expectedResponse);

        OfferUpdateResponse actualResponse = offerService.updateOffer(request);

        // Then
        assertEquals(expectedResponse, actualResponse);
        verify(offerRepository, times(1)).findById(offerId);
        verify(teamService, times(1)).getTeam();
        verify(offerRepository, times(1)).save(any(Offer.class));
        verify(offerMapper, times(1)).toOfferUpdateResponse(updatedOffer);
    }

    @Test
    void updateOffer_whenOfferNotPending_shouldThrowIllegalStateException() {
        // Given
        Long offerId = 1L;
        Long buyerTeamId = 1L;

        Team buyerTeam = new Team();
        buyerTeam.setId(buyerTeamId);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setBuyerTeam(buyerTeam);
        existingOffer.setStatus(Offer.OfferStatus.ACCEPTED);

        OfferUpdateRequest request = OfferUpdateRequest.builder()
                .offerId(offerId)
                .offeredPrice(2000000)
                .build();

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));

        // Then
        assertThrowsExactly(IllegalStateException.class, () -> offerService.updateOffer(request));
        verify(offerRepository, times(1)).findById(offerId);
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void updateOffer_whenNotAuthorizedBuyer_shouldThrowAccessDeniedException() {
        // Given
        Long offerId = 1L;
        Long buyerTeamId = 1L;
        Long currentTeamId = 2L;

        Team buyerTeam = new Team();
        buyerTeam.setId(buyerTeamId);

        Team currentTeam = new Team();
        currentTeam.setId(currentTeamId);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setBuyerTeam(buyerTeam);
        existingOffer.setStatus(Offer.OfferStatus.PENDING);

        OfferUpdateRequest request = OfferUpdateRequest.builder()
                .offerId(offerId)
                .offeredPrice(2000000)
                .build();

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
        when(teamService.getTeam()).thenReturn(currentTeam);

        // Then
        assertThrowsExactly(AccessDeniedException.class, () -> offerService.updateOffer(request));
        verify(offerRepository, times(1)).findById(offerId);
        verify(teamService, times(1)).getTeam();
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void cancelOffer_whenValidRequest_shouldCancelOffer() {
        // Given
        Long offerId = 1L;
        Long buyerTeamId = 1L;

        Team buyerTeam = new Team();
        buyerTeam.setId(buyerTeamId);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setBuyerTeam(buyerTeam);
        existingOffer.setStatus(Offer.OfferStatus.PENDING);

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
        when(teamService.getTeam()).thenReturn(buyerTeam);

        offerService.cancelOffer(offerId);

        // Then
        assertEquals(Offer.OfferStatus.CANCELLED, existingOffer.getStatus());
        verify(offerRepository, times(1)).findById(offerId);
        verify(teamService, times(1)).getTeam();
    }

    @Test
    void cancelOffer_whenOfferNotPending_shouldThrowIllegalStateException() {
        // Given
        Long offerId = 1L;
        Long buyerTeamId = 1L;

        Team buyerTeam = new Team();
        buyerTeam.setId(buyerTeamId);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setBuyerTeam(buyerTeam);
        existingOffer.setStatus(Offer.OfferStatus.REJECTED);

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));

        // Then
        assertThrowsExactly(IllegalStateException.class, () -> offerService.cancelOffer(offerId));
        verify(offerRepository, times(1)).findById(offerId);
    }

    @Test
    void cancelOffer_whenNotAuthorizedBuyer_shouldThrowAccessDeniedException() {
        // Given
        Long offerId = 1L;
        Long buyerTeamId = 1L;
        Long currentTeamId = 2L;

        Team buyerTeam = new Team();
        buyerTeam.setId(buyerTeamId);

        Team currentTeam = new Team();
        currentTeam.setId(currentTeamId);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setBuyerTeam(buyerTeam);
        existingOffer.setStatus(Offer.OfferStatus.PENDING);

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
        when(teamService.getTeam()).thenReturn(currentTeam);

        // Then
        assertThrowsExactly(AccessDeniedException.class, () -> offerService.cancelOffer(offerId));
        verify(offerRepository, times(1)).findById(offerId);
        verify(teamService, times(1)).getTeam();
    }

    @Test
    void rejectOffer_whenValidRequest_shouldRejectOffer() {
        // Given
        Long offerId = 1L;
        Long sellerTeamId = 1L;

        Team sellerTeam = new Team();
        sellerTeam.setId(sellerTeamId);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setSellerTeam(sellerTeam);
        existingOffer.setStatus(Offer.OfferStatus.PENDING);

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
        when(teamService.getTeam()).thenReturn(sellerTeam);

        offerService.rejectOffer(offerId);

        // Then
        assertEquals(Offer.OfferStatus.REJECTED, existingOffer.getStatus());
        verify(offerRepository, times(1)).findById(offerId);
        verify(teamService, times(1)).getTeam();
    }

    @Test
    void rejectOffer_whenOfferNotPending_shouldThrowIllegalStateException() {
        // Given
        Long offerId = 1L;
        Long sellerTeamId = 1L;

        Team sellerTeam = new Team();
        sellerTeam.setId(sellerTeamId);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setSellerTeam(sellerTeam);
        existingOffer.setStatus(Offer.OfferStatus.ACCEPTED);

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));

        // Then
        assertThrowsExactly(IllegalStateException.class, () -> offerService.rejectOffer(offerId));
        verify(offerRepository, times(1)).findById(offerId);
    }

    @Test
    void rejectOffer_whenNotAuthorizedSeller_shouldThrowAccessDeniedException() {
        // Given
        Long offerId = 1L;
        Long sellerTeamId = 1L;
        Long currentTeamId = 2L;

        Team sellerTeam = new Team();
        sellerTeam.setId(sellerTeamId);

        Team currentTeam = new Team();
        currentTeam.setId(currentTeamId);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setSellerTeam(sellerTeam);
        existingOffer.setStatus(Offer.OfferStatus.PENDING);

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
        when(teamService.getTeam()).thenReturn(currentTeam);

        // Then
        assertThrowsExactly(AccessDeniedException.class, () -> offerService.rejectOffer(offerId));
        verify(offerRepository, times(1)).findById(offerId);
        verify(teamService, times(1)).getTeam();
    }

    @Test
    void acceptOffer_whenValidRequest_shouldAcceptOfferAndCreatePurchase() {
        // Given
        Long offerId = 1L;
        Long sellerTeamId = 1L;
        Long buyerTeamId = 2L;
        Long playerId = 1L;
        Integer offeredPrice = 2000000;

        Team sellerTeam = new Team();
        sellerTeam.setId(sellerTeamId);
        sellerTeam.setBudget(5000000);
        sellerTeam.setPlayerCount(20);

        Team buyerTeam = new Team();
        buyerTeam.setId(buyerTeamId);
        buyerTeam.setBudget(10000000);
        buyerTeam.setPlayerCount(15);

        Player player = new Player();
        player.setId(playerId);
        player.setValue(1000000);
        player.setTeam(sellerTeam);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setSellerTeam(sellerTeam);
        existingOffer.setBuyerTeam(buyerTeam);
        existingOffer.setPlayer(player);
        existingOffer.setOfferedPrice(offeredPrice);
        existingOffer.setStatus(Offer.OfferStatus.PENDING);

        ArgumentCaptor<Offer> offerCaptor = ArgumentCaptor.forClass(Offer.class);
        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
        when(teamService.getTeam()).thenReturn(sellerTeam);
        when(offerRepository.findAllByPlayer_IdAndStatus(playerId, Offer.OfferStatus.PENDING))
                .thenReturn(new ArrayList<>());
        when(offerRepository.save(any(Offer.class))).thenReturn(existingOffer);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(new Purchase());

        offerService.acceptOffer(offerId);

        // Then
        verify(offerRepository, times(1)).save(offerCaptor.capture());
        verify(purchaseRepository, times(1)).save(purchaseCaptor.capture());

        Offer savedOffer = offerCaptor.getValue();
        Purchase savedPurchase = purchaseCaptor.getValue();

        assertEquals(Offer.OfferStatus.ACCEPTED, savedOffer.getStatus());
        assertEquals(10000000 - offeredPrice, buyerTeam.getBudget());
        assertEquals(5000000 + offeredPrice, sellerTeam.getBudget());
        assertEquals(buyerTeam, player.getTeam());
        assertEquals(19, sellerTeam.getPlayerCount());
        assertEquals(16, buyerTeam.getPlayerCount());
        assertTrue(player.getValue() > 1000000); // Value should increase

        assertEquals(playerId, savedPurchase.getPlayerId());
        assertEquals(sellerTeamId, savedPurchase.getSellerId());
        assertEquals(buyerTeamId, savedPurchase.getBuyerId());
        assertEquals(offeredPrice, savedPurchase.getPrice());
        assertNotNull(savedPurchase.getCreatedAt());
    }

    @Test
    void acceptOffer_whenInsufficientBudget_shouldThrowBadInputException() {
        // Given
        Long offerId = 1L;
        Long sellerTeamId = 1L;
        Long buyerTeamId = 2L;
        Integer offeredPrice = 10000000;

        Team sellerTeam = new Team();
        sellerTeam.setId(sellerTeamId);
        sellerTeam.setBudget(5000000);

        Team buyerTeam = new Team();
        buyerTeam.setId(buyerTeamId);
        buyerTeam.setBudget(1000000); // Insufficient budget

        Player player = new Player();
        player.setId(1L);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setSellerTeam(sellerTeam);
        existingOffer.setBuyerTeam(buyerTeam);
        existingOffer.setPlayer(player);
        existingOffer.setOfferedPrice(offeredPrice);
        existingOffer.setStatus(Offer.OfferStatus.PENDING);

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
        when(teamService.getTeam()).thenReturn(sellerTeam);

        // Then
        assertThrowsExactly(BadInputException.class, () -> offerService.acceptOffer(offerId));
        verify(offerRepository, times(1)).findById(offerId);
        verify(offerRepository, never()).save(any(Offer.class));
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void acceptOffer_whenOfferNotPending_shouldThrowAccessDeniedException() {
        // Given
        Long offerId = 1L;
        Long sellerTeamId = 1L;

        Team sellerTeam = new Team();
        sellerTeam.setId(sellerTeamId);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setSellerTeam(sellerTeam);
        existingOffer.setStatus(Offer.OfferStatus.ACCEPTED);

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
        when(teamService.getTeam()).thenReturn(sellerTeam);

        // Then
        assertThrowsExactly(AccessDeniedException.class, () -> offerService.acceptOffer(offerId));
        verify(offerRepository, times(1)).findById(offerId);
        verify(teamService, times(1)).getTeam();
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void acceptOffer_whenNotAuthorizedSeller_shouldThrowAccessDeniedException() {
        // Given
        Long offerId = 1L;
        Long sellerTeamId = 1L;
        Long currentTeamId = 2L;

        Team sellerTeam = new Team();
        sellerTeam.setId(sellerTeamId);

        Team currentTeam = new Team();
        currentTeam.setId(currentTeamId);

        Offer existingOffer = new Offer();
        existingOffer.setOfferId(offerId);
        existingOffer.setSellerTeam(sellerTeam);
        existingOffer.setStatus(Offer.OfferStatus.PENDING);

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(existingOffer));
        when(teamService.getTeam()).thenReturn(currentTeam);

        // Then
        assertThrowsExactly(AccessDeniedException.class, () -> offerService.acceptOffer(offerId));
        verify(offerRepository, times(1)).findById(offerId);
        verify(teamService, times(1)).getTeam();
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void acceptOffer_whenOtherPendingOffersExist_shouldRejectThem() {
        // Given
        Long offerId = 1L;
        Long sellerTeamId = 1L;
        Long buyerTeamId = 2L;
        Long playerId = 1L;
        Integer offeredPrice = 2000000;

        Team sellerTeam = new Team();
        sellerTeam.setId(sellerTeamId);
        sellerTeam.setBudget(5000000);
        sellerTeam.setPlayerCount(20);

        Team buyerTeam = new Team();
        buyerTeam.setId(buyerTeamId);
        buyerTeam.setBudget(10000000);
        buyerTeam.setPlayerCount(15);

        Player player = new Player();
        player.setId(playerId);
        player.setValue(1000000);
        player.setTeam(sellerTeam);

        Offer acceptedOffer = new Offer();
        acceptedOffer.setOfferId(offerId);
        acceptedOffer.setSellerTeam(sellerTeam);
        acceptedOffer.setBuyerTeam(buyerTeam);
        acceptedOffer.setPlayer(player);
        acceptedOffer.setOfferedPrice(offeredPrice);
        acceptedOffer.setStatus(Offer.OfferStatus.PENDING);

        Offer otherOffer1 = new Offer();
        otherOffer1.setOfferId(2L);
        otherOffer1.setStatus(Offer.OfferStatus.PENDING);

        Offer otherOffer2 = new Offer();
        otherOffer2.setOfferId(3L);
        otherOffer2.setStatus(Offer.OfferStatus.PENDING);

        List<Offer> otherOffers = new ArrayList<>();
        otherOffers.add(otherOffer1);
        otherOffers.add(otherOffer2);
        otherOffers.add(acceptedOffer);

        // When
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(acceptedOffer));
        when(teamService.getTeam()).thenReturn(sellerTeam);
        when(offerRepository.findAllByPlayer_IdAndStatus(playerId, Offer.OfferStatus.PENDING))
                .thenReturn(otherOffers);
        when(offerRepository.save(any(Offer.class))).thenReturn(acceptedOffer);
        when(purchaseRepository.save(any(Purchase.class))).thenReturn(new Purchase());

        offerService.acceptOffer(offerId);

        // Then
        assertEquals(Offer.OfferStatus.REJECTED, otherOffer1.getStatus());
        assertEquals(Offer.OfferStatus.REJECTED, otherOffer2.getStatus());
        assertEquals(Offer.OfferStatus.ACCEPTED, acceptedOffer.getStatus());
    }
}