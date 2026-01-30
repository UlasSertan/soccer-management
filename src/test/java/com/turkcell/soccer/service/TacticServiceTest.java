package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.TacticDto;
import com.turkcell.soccer.dto.request.TacticCreationRequest;
import com.turkcell.soccer.exception.BadInputException;
import com.turkcell.soccer.exception.DuplicateTacticException;
import com.turkcell.soccer.mapper.TacticMapper;
import com.turkcell.soccer.model.Tactic;
import com.turkcell.soccer.repository.TacticRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TacticServiceTest {

    @Mock
    private TacticRepository tacticRepository;
    @Mock
    private TacticMapper tacticMapper;
    @InjectMocks
    private TacticService tacticService;

    private TacticCreationRequest validRequest;
    private Tactic sampleTactic;

    @BeforeEach
    void setUp() {
        // Givens
        validRequest = new TacticCreationRequest();
        validRequest.setDefenders(4);
        validRequest.setMidfielders(4);
        validRequest.setForwards(2);
        validRequest.setStyle(Tactic.TacticStyle.BALANCED);

        sampleTactic = new Tactic();
        sampleTactic.setDefenders(4);
        sampleTactic.setMidfielders(4);
        sampleTactic.setForwards(2);
        sampleTactic.setStyle(Tactic.TacticStyle.BALANCED);
    }

    @Test
    void createTactic_whenTacticExists_shouldThrowDuplicateTacticException() {
        // When
        when(tacticRepository.findByDefendersAndMidfieldersAndForwardsAndStyle(
                4, 4, 2, Tactic.TacticStyle.BALANCED))
                .thenReturn(Optional.of(sampleTactic));

        // Then
        assertThrows(DuplicateTacticException.class, () -> {
            tacticService.createTactic(validRequest);
        });

        verify(tacticRepository, times(1))
                .findByDefendersAndMidfieldersAndForwardsAndStyle(4, 4, 2, Tactic.TacticStyle.BALANCED);
        verify(tacticRepository, never()).save(any());
    }

    @Test
    void createTactic_whenIncorrectFormation_shouldThrowBadInputException() {
        // Given
        TacticCreationRequest invalidRequest = new TacticCreationRequest();
        invalidRequest.setDefenders(4);
        invalidRequest.setMidfielders(4);
        invalidRequest.setForwards(3);
        invalidRequest.setStyle(Tactic.TacticStyle.BALANCED);

        when(tacticRepository.findByDefendersAndMidfieldersAndForwardsAndStyle(
                4, 4, 3, Tactic.TacticStyle.BALANCED))
                .thenReturn(Optional.empty());

        // When & Then
        assertThrows(BadInputException.class, () -> {
            tacticService.createTactic(invalidRequest);
        });

        verify(tacticRepository, never()).save(any());
    }

    @Test
    void createTactic_whenTacticNotExists_shouldReturnTactic() {
        when(tacticRepository.findByDefendersAndMidfieldersAndForwardsAndStyle(4, 4, 2, Tactic.TacticStyle.BALANCED))
                .thenReturn(Optional.empty());
        when(tacticRepository.save(any(Tactic.class))).thenReturn(sampleTactic);

        Tactic result = tacticService.createTactic(validRequest);

        assertNotNull(result);
        assertEquals(sampleTactic, result);
        verify(tacticRepository, times(1)).save(any(Tactic.class));
        verify(tacticMapper, never()).toDto(any(Tactic.class));
    }

    @Test
    void findExistingTactic_whenTacticExists_shouldReturnOptionalWithTactic() {
        // Given
        when(tacticRepository.findByDefendersAndMidfieldersAndForwardsAndStyle(
                4, 4, 2, Tactic.TacticStyle.BALANCED))
                .thenReturn(Optional.of(sampleTactic));

        // When
        Optional<Tactic> result = tacticService.findExistingTactic(validRequest);

        // Then
        assertTrue(result.isPresent());
        assertEquals(sampleTactic, result.get());
    }

    @Test
    void findExistingTactic_whenTacticNotExists_shouldReturnEmptyOptional() {
        // Given
        when(tacticRepository.findByDefendersAndMidfieldersAndForwardsAndStyle(
                4, 4, 2, Tactic.TacticStyle.BALANCED))
                .thenReturn(Optional.empty());

        // When
        Optional<Tactic> result = tacticService.findExistingTactic(validRequest);

        // Then
        assertTrue(result.isEmpty());
    }
}