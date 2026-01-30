package com.turkcell.soccer.service;

import com.turkcell.soccer.dto.TacticDto;
import com.turkcell.soccer.dto.request.TacticCreationRequest;
import com.turkcell.soccer.exception.BadInputException;
import com.turkcell.soccer.exception.DuplicateTacticException;
import com.turkcell.soccer.mapper.TacticMapper;
import com.turkcell.soccer.model.Tactic;
import com.turkcell.soccer.repository.TacticRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class TacticService {


    private final TacticRepository tacticRepository;
    private final TacticMapper tacticMapper;
    @Autowired
    public TacticService(TacticRepository tacticRepository,  TacticMapper tacticMapper) {
        this.tacticRepository = tacticRepository;
        this.tacticMapper = tacticMapper;
    }


    @Transactional
    public Tactic createTactic(TacticCreationRequest request) {
        if (findExistingTactic(request).isPresent()) {
            log.warn("Duplicate tactic creation request with formation {}-{}-{} and style: {}",
                    request.getDefenders(), request.getMidfielders(), request.getForwards(), request.getStyle());
            throw new DuplicateTacticException("Duplicate tactic creation request");
        }
        if (request.getDefenders() + request.getMidfielders() + request.getForwards() != 10) {
            log.warn("Total player count should be equal to 10");
            throw new BadInputException("Total player count should be equal to 10");
        }

        Tactic tactic = new Tactic();
        tactic.setDefenders(request.getDefenders());
        tactic.setMidfielders(request.getMidfielders());
        tactic.setForwards(request.getForwards());
        tactic.setStyle(request.getStyle());
        Tactic savedTactic = tacticRepository.save(tactic);
        log.debug("Creating tactic with ID: {}, Formation: {}-{}-{}, Style: {}",
                savedTactic.getId(), savedTactic.getDefenders(), savedTactic.getMidfielders(),
                savedTactic.getForwards(), savedTactic.getStyle());
        return savedTactic;
    }

    @Transactional(readOnly = true)
    public Optional<Tactic> findExistingTactic(TacticCreationRequest request) {
        return tacticRepository.findByDefendersAndMidfieldersAndForwardsAndStyle(
                request.getDefenders(),
                request.getMidfielders(),
                request.getForwards(),
                request.getStyle());
    }

    @Transactional(readOnly = true)
    public TacticDto toTacticDto(Tactic tactic) {
        return tacticMapper.toDto(tactic);
    }

}
