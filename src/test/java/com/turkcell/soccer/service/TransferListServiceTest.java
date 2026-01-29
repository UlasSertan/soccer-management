package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.TransferListDto;
import com.turkcell.soccer.dto.TransferListFilter;
import com.turkcell.soccer.dto.request.TransferListRequest;
import com.turkcell.soccer.dto.response.PurchaseResponse;
import com.turkcell.soccer.dto.response.TransferListAdditionResponse;
import com.turkcell.soccer.dto.response.TransferListInfoResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferListServiceTest {

    @Mock
    private TransferListRepository transferListRepository;

    @Mock
    private TeamService teamService;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TransferListMapper transferListMapper;

    @Mock
    private TransferListSecurity transferListSecurity;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private TransferListService transferListService;

    private Team buyerTeam;
    private Team sellerTeam;
    private Player player;
    private TransferList transferList;

    @BeforeEach
    void setUp() {
        buyerTeam = new Team();
        buyerTeam.setId(1L);
        buyerTeam.setName("Buyer FC");
        buyerTeam.setCountry("Turkey");
        buyerTeam.setBudget(10_000_000);
        buyerTeam.setPlayerCount(20);

        sellerTeam = new Team();
        sellerTeam.setId(2L);
        sellerTeam.setName("Seller FC");
        sellerTeam.setCountry("Turkey");
        sellerTeam.setBudget(5_000_000);
        sellerTeam.setPlayerCount(20);

        player = new Player();
        player.setId(1L);
        player.setFirstName("John");
        player.setLastName("Doe");
        player.setCountry("Turkey");
        player.setAge(25);
        player.setPosition("Forward");
        player.setValue(1_000_000);
        player.setTeam(sellerTeam);

        transferList = new TransferList();
        transferList.setId(1L);
        transferList.setPlayer(player);
        transferList.setPrice(2_000_000);
    }

    // ==================== addToTransferList Tests ====================

    @Test
    void addToTransferList_whenValidRequest_shouldAddPlayerSuccessfully() {
        // Given
        TransferListRequest.Add request = new TransferListRequest.Add();
        request.setPlayerId(1L);
        request.setPrice(2_000_000);

        TransferListAdditionResponse expectedResponse = new TransferListAdditionResponse(
                2_000_000, 1L, LocalDateTime.now()
        );

        // When
        when(teamService.getTeam()).thenReturn(sellerTeam);
        when(playerRepository.findByIdAndTeam_Id(1L, 2L)).thenReturn(Optional.of(player));
        when(transferListRepository.existsByPlayer(player)).thenReturn(false);
        when(transferListRepository.save(any(TransferList.class))).thenReturn(transferList);
        when(transferListMapper.toAdditionResponse(any(TransferList.class))).thenReturn(expectedResponse);

        TransferListAdditionResponse result = transferListService.addToTransferList(request);

        // Then
        assertNotNull(result);
        assertEquals(2_000_000, result.getPrice());
        assertEquals(1L, result.getPlayerId());
        verify(transferListRepository, times(1)).save(any(TransferList.class));
    }

    @Test
    void addToTransferList_whenPlayerNotFound_shouldThrowNoSuchPlayerException() {
        // Given
        TransferListRequest.Add request = new TransferListRequest.Add();
        request.setPlayerId(999L);
        request.setPrice(2_000_000);

        // When
        when(teamService.getTeam()).thenReturn(sellerTeam);
        when(playerRepository.findByIdAndTeam_Id(999L, 2L)).thenReturn(Optional.empty());

        // Then
        assertThrowsExactly(NoSuchPlayerException.class, () -> transferListService.addToTransferList(request));
        verify(transferListRepository, never()).save(any(TransferList.class));
    }

    @Test
    void addToTransferList_whenPlayerAlreadyInList_shouldThrowIllegalStateException() {
        // Given
        TransferListRequest.Add request = new TransferListRequest.Add();
        request.setPlayerId(1L);
        request.setPrice(2_000_000);

        // When
        when(teamService.getTeam()).thenReturn(sellerTeam);
        when(playerRepository.findByIdAndTeam_Id(1L, 2L)).thenReturn(Optional.of(player));
        when(transferListRepository.existsByPlayer(player)).thenReturn(true);

        // Then
        IllegalStateException exception = assertThrowsExactly(IllegalStateException.class,
                () -> transferListService.addToTransferList(request));
        assertEquals("This player is already on the transfer list!", exception.getMessage());
        verify(transferListRepository, never()).save(any(TransferList.class));
    }

    @Test
    void addToTransferList_whenPriceIsNegative_shouldThrowBadInputException() {
        // Given
        TransferListRequest.Add request = new TransferListRequest.Add();
        request.setPlayerId(1L);
        request.setPrice(-100);

        // When
        when(teamService.getTeam()).thenReturn(sellerTeam);
        when(playerRepository.findByIdAndTeam_Id(1L, 2L)).thenReturn(Optional.of(player));
        when(transferListRepository.existsByPlayer(player)).thenReturn(false);

        // Then
        BadInputException exception = assertThrowsExactly(BadInputException.class,
                () -> transferListService.addToTransferList(request));
        assertEquals("Price cannot be less than 0", exception.getMessage());
        verify(transferListRepository, never()).save(any(TransferList.class));
    }

    @Test
    void addToTransferList_whenPriceIsZero_shouldAddSuccessfully() {
        // Given
        TransferListRequest.Add request = new TransferListRequest.Add();
        request.setPlayerId(1L);
        request.setPrice(0);

        TransferList savedTransferList = new TransferList();
        savedTransferList.setId(1L);
        savedTransferList.setPlayer(player);
        savedTransferList.setPrice(0);

        TransferListAdditionResponse expectedResponse = new TransferListAdditionResponse(
                0, 1L, LocalDateTime.now()
        );

        // When
        when(teamService.getTeam()).thenReturn(sellerTeam);
        when(playerRepository.findByIdAndTeam_Id(1L, 2L)).thenReturn(Optional.of(player));
        when(transferListRepository.existsByPlayer(player)).thenReturn(false);
        when(transferListRepository.save(any(TransferList.class))).thenReturn(savedTransferList);
        when(transferListMapper.toAdditionResponse(any(TransferList.class))).thenReturn(expectedResponse);

        TransferListAdditionResponse result = transferListService.addToTransferList(request);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getPrice());
        verify(transferListRepository, times(1)).save(any(TransferList.class));
    }

    // ==================== getTransferList Tests ====================

    @Test
    void getTransferList_whenFilterProvided_shouldReturnFilteredList() {
        // Given
        TransferListFilter filter = TransferListFilter.builder()
                .playerName("John")
                .teamName("Seller FC")
                .country("Turkey")
                .minPrice(1_000_000)
                .maxPrice(5_000_000)
                .build();

        List<TransferList> transferLists = List.of(transferList);
        List<TransferListDto> dtoList = List.of(
                TransferListDto.builder().id(1L).price(2_000_000).build()
        );
        TransferListInfoResponse expectedResponse = TransferListInfoResponse.builder()
                .players(dtoList)
                .build();

        // When
        when(transferListRepository.findAll(any(Specification.class))).thenReturn(transferLists);
        when(transferListMapper.transferListToDtoList(transferLists)).thenReturn(dtoList);
        when(transferListMapper.toTransferListInfoResponse(dtoList)).thenReturn(expectedResponse);

        TransferListInfoResponse result = transferListService.getTransferList(filter);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getPlayers().size());
        verify(transferListRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void getTransferList_whenNoFilter_shouldReturnAllList() {
        // Given
        TransferListFilter filter = new TransferListFilter();

        List<TransferList> transferLists = List.of(transferList);
        List<TransferListDto> dtoList = List.of(
                TransferListDto.builder().id(1L).price(2_000_000).build()
        );
        TransferListInfoResponse expectedResponse = TransferListInfoResponse.builder()
                .players(dtoList)
                .build();

        // When
        when(transferListRepository.findAll(any(Specification.class))).thenReturn(transferLists);
        when(transferListMapper.transferListToDtoList(transferLists)).thenReturn(dtoList);
        when(transferListMapper.toTransferListInfoResponse(dtoList)).thenReturn(expectedResponse);

        TransferListInfoResponse result = transferListService.getTransferList(filter);

        // Then
        assertNotNull(result);
        verify(transferListRepository, times(1)).findAll(any(Specification.class));
    }

    @Test
    void getTransferList_whenEmptyResult_shouldReturnEmptyList() {
        // Given
        TransferListFilter filter = TransferListFilter.builder()
                .playerName("NonExistent")
                .build();

        List<TransferList> emptyList = new ArrayList<>();
        List<TransferListDto> emptyDtoList = new ArrayList<>();
        TransferListInfoResponse expectedResponse = TransferListInfoResponse.builder()
                .players(emptyDtoList)
                .build();

        // When
        when(transferListRepository.findAll(any(Specification.class))).thenReturn(emptyList);
        when(transferListMapper.transferListToDtoList(emptyList)).thenReturn(emptyDtoList);
        when(transferListMapper.toTransferListInfoResponse(emptyDtoList)).thenReturn(expectedResponse);

        TransferListInfoResponse result = transferListService.getTransferList(filter);

        // Then
        assertNotNull(result);
        assertTrue(result.getPlayers().isEmpty());
    }

    // ==================== updateTransferList Tests ====================

    @Test
    void updateTransferList_whenAuthorized_shouldUpdatePrice() {
        // Given
        Long playerId = 1L;
        TransferListRequest.UpdatePrice request = new TransferListRequest.UpdatePrice();
        request.setPrice(3_000_000);

        TransferList updatedTransferList = new TransferList();
        updatedTransferList.setId(1L);
        updatedTransferList.setPlayer(player);
        updatedTransferList.setPrice(3_000_000);

        TransferListDto dto = TransferListDto.builder()
                .id(1L)
                .price(3_000_000)
                .build();

        TransferListInfoResponse expectedResponse = TransferListInfoResponse.builder()
                .players(List.of(dto))
                .build();

        // When
        when(transferListSecurity.getListingIfAuthorized(playerId)).thenReturn(transferList);
        when(transferListRepository.save(any(TransferList.class))).thenReturn(updatedTransferList);
        when(transferListMapper.transferListToDto(any(TransferList.class))).thenReturn(dto);
        when(transferListMapper.toTransferListInfoResponse(anyList())).thenReturn(expectedResponse);

        TransferListInfoResponse result = transferListService.updateTransferList(playerId, request);

        // Then
        assertNotNull(result);
        assertEquals(3_000_000, result.getPlayers().get(0).getPrice());
        verify(transferListRepository, times(1)).save(any(TransferList.class));
    }

    @Test
    void updateTransferList_whenPriceChangedToZero_shouldUpdateSuccessfully() {
        // Given
        Long playerId = 1L;
        TransferListRequest.UpdatePrice request = new TransferListRequest.UpdatePrice();
        request.setPrice(0);

        TransferListDto dto = TransferListDto.builder().id(1L).price(0).build();
        TransferListInfoResponse expectedResponse = TransferListInfoResponse.builder()
                .players(List.of(dto))
                .build();

        // When
        when(transferListSecurity.getListingIfAuthorized(playerId)).thenReturn(transferList);
        when(transferListRepository.save(any(TransferList.class))).thenReturn(transferList);
        when(transferListMapper.transferListToDto(any(TransferList.class))).thenReturn(dto);
        when(transferListMapper.toTransferListInfoResponse(anyList())).thenReturn(expectedResponse);

        TransferListInfoResponse result = transferListService.updateTransferList(playerId, request);

        // Then
        assertNotNull(result);
        assertEquals(0, result.getPlayers().get(0).getPrice());
    }

    // ==================== deleteTransferList Tests ====================

    @Test
    void deleteTransferList_whenAuthorized_shouldDeleteSuccessfully() {
        // Given
        Long playerId = 1L;
        player.setTransferList(transferList);

        // When
        when(transferListSecurity.getListingIfAuthorized(playerId)).thenReturn(transferList);

        transferListService.deleteTransferList(playerId);

        // Then
        verify(transferListSecurity, times(1)).getListingIfAuthorized(playerId);
        verify(transferListRepository, times(1)).delete(transferList);
        assertNull(transferList.getPlayer());
    }

    // ==================== purchasePlayer Tests ====================

    @Test
    void purchasePlayer_whenValidPurchase_shouldCompleteSuccessfully() {
        // Given
        Long playerId = 1L;

        PurchaseResponse expectedResponse = PurchaseResponse.builder()
                .purchaseId(1L)
                .playerId(playerId)
                .sellerId(2L)
                .buyerId(1L)
                .price(2_000_000)
                .createdAt(LocalDateTime.now())
                .build();

        ArgumentCaptor<Purchase> purchaseCaptor = ArgumentCaptor.forClass(Purchase.class);

        // When
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(transferListRepository.findByPlayer_Id(playerId)).thenReturn(transferList);
        when(playerRepository.save(any(Player.class))).thenReturn(player);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseRepository.save(purchaseCaptor.capture())).thenAnswer(invocation -> {
            Purchase p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(transferListMapper.toPurchaseResponse(any(Purchase.class))).thenReturn(expectedResponse);

        PurchaseResponse result = transferListService.purchasePlayer(playerId);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getPurchaseId());
        assertEquals(2_000_000, result.getPrice());

        // Verify budget changes
        assertEquals(8_000_000, buyerTeam.getBudget()); // 10M - 2M
        assertEquals(7_000_000, sellerTeam.getBudget()); // 5M + 2M

        // Verify player count changes
        assertEquals(21, buyerTeam.getPlayerCount());
        assertEquals(19, sellerTeam.getPlayerCount());

        // Verify purchase was saved correctly
        Purchase savedPurchase = purchaseCaptor.getValue();
        assertEquals(playerId, savedPurchase.getPlayerId());
        assertEquals(2L, savedPurchase.getSellerId());
        assertEquals(1L, savedPurchase.getBuyerId());
        assertEquals(2_000_000, savedPurchase.getPrice());

        verify(transferListRepository, times(1)).delete(transferList);
    }

    @Test
    void purchasePlayer_whenPlayerNotInTransferList_shouldThrowPlayerNotInTransferListException() {
        // Given
        Long playerId = 999L;

        // When
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(transferListRepository.findByPlayer_Id(playerId)).thenReturn(null);

        // Then
        PlayerNotInTransferListException exception = assertThrowsExactly(
                PlayerNotInTransferListException.class,
                () -> transferListService.purchasePlayer(playerId)
        );
        assertEquals("Player not in transfer list!", exception.getMessage());
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void purchasePlayer_whenBuyingOwnPlayer_shouldThrowIllegalStateException() {
        // Given
        Long playerId = 1L;
        player.setTeam(buyerTeam); // Player belongs to buyer team

        // When
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(transferListRepository.findByPlayer_Id(playerId)).thenReturn(transferList);

        // Then
        IllegalStateException exception = assertThrowsExactly(IllegalStateException.class,
                () -> transferListService.purchasePlayer(playerId));
        assertEquals("You cannot buy your own player!", exception.getMessage());
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void purchasePlayer_whenInsufficientBudget_shouldThrowBadInputException() {
        // Given
        Long playerId = 1L;
        buyerTeam.setBudget(1_000_000); // Less than transfer price (2M)

        // When
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(transferListRepository.findByPlayer_Id(playerId)).thenReturn(transferList);

        // Then
        BadInputException exception = assertThrowsExactly(BadInputException.class,
                () -> transferListService.purchasePlayer(playerId));
        assertEquals("Your do not have enough budget to buy this player!", exception.getMessage());
        verify(purchaseRepository, never()).save(any(Purchase.class));
    }

    @Test
    void purchasePlayer_whenBudgetExactlyEqualToPrice_shouldCompleteSuccessfully() {
        // Given
        Long playerId = 1L;
        buyerTeam.setBudget(2_000_000); // Exactly equal to price

        PurchaseResponse expectedResponse = PurchaseResponse.builder()
                .purchaseId(1L)
                .playerId(playerId)
                .price(2_000_000)
                .build();

        // When
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(transferListRepository.findByPlayer_Id(playerId)).thenReturn(transferList);
        when(playerRepository.save(any(Player.class))).thenReturn(player);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(transferListMapper.toPurchaseResponse(any(Purchase.class))).thenReturn(expectedResponse);

        PurchaseResponse result = transferListService.purchasePlayer(playerId);

        // Then
        assertNotNull(result);
        assertEquals(0, buyerTeam.getBudget()); // 2M - 2M = 0
        verify(purchaseRepository, times(1)).save(any(Purchase.class));
    }

    @Test
    void purchasePlayer_shouldIncreasePlayerValue() {
        // Given
        Long playerId = 1L;
        int originalValue = player.getValue(); // 1_000_000

        PurchaseResponse expectedResponse = PurchaseResponse.builder()
                .purchaseId(1L)
                .playerId(playerId)
                .build();

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);

        // When
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(transferListRepository.findByPlayer_Id(playerId)).thenReturn(transferList);
        when(playerRepository.save(playerCaptor.capture())).thenReturn(player);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(transferListMapper.toPurchaseResponse(any(Purchase.class))).thenReturn(expectedResponse);

        transferListService.purchasePlayer(playerId);

        // Then
        Player savedPlayer = playerCaptor.getValue();
        // Value should increase between 10% and 100%: (110-200) * originalValue / 100
        assertTrue(savedPlayer.getValue() >= originalValue * 110 / 100);
        assertTrue(savedPlayer.getValue() <= originalValue * 200 / 100);
    }

    @Test
    void purchasePlayer_shouldTransferPlayerToNewTeam() {
        // Given
        Long playerId = 1L;

        PurchaseResponse expectedResponse = PurchaseResponse.builder()
                .purchaseId(1L)
                .playerId(playerId)
                .build();

        ArgumentCaptor<Player> playerCaptor = ArgumentCaptor.forClass(Player.class);

        // When
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(transferListRepository.findByPlayer_Id(playerId)).thenReturn(transferList);
        when(playerRepository.save(playerCaptor.capture())).thenReturn(player);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(transferListMapper.toPurchaseResponse(any(Purchase.class))).thenReturn(expectedResponse);

        transferListService.purchasePlayer(playerId);

        // Then
        Player savedPlayer = playerCaptor.getValue();
        assertEquals(buyerTeam, savedPlayer.getTeam());
        assertNull(savedPlayer.getTransferList());
    }

    @Test
    void purchasePlayer_shouldDeleteTransferListEntry() {
        // Given
        Long playerId = 1L;

        PurchaseResponse expectedResponse = PurchaseResponse.builder()
                .purchaseId(1L)
                .playerId(playerId)
                .build();

        // When
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(transferListRepository.findByPlayer_Id(playerId)).thenReturn(transferList);
        when(playerRepository.save(any(Player.class))).thenReturn(player);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(transferListMapper.toPurchaseResponse(any(Purchase.class))).thenReturn(expectedResponse);

        transferListService.purchasePlayer(playerId);

        // Then
        verify(transferListRepository, times(1)).delete(transferList);
        assertNull(transferList.getPlayer());
    }

    @Test
    void purchasePlayer_shouldSaveBothTeams() {
        // Given
        Long playerId = 1L;

        PurchaseResponse expectedResponse = PurchaseResponse.builder()
                .purchaseId(1L)
                .playerId(playerId)
                .build();

        // When
        when(teamService.getTeam()).thenReturn(buyerTeam);
        when(transferListRepository.findByPlayer_Id(playerId)).thenReturn(transferList);
        when(playerRepository.save(any(Player.class))).thenReturn(player);
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(invocation -> {
            Purchase p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(transferListMapper.toPurchaseResponse(any(Purchase.class))).thenReturn(expectedResponse);

        transferListService.purchasePlayer(playerId);

        // Then
        verify(teamRepository, times(2)).save(any(Team.class));
    }
}