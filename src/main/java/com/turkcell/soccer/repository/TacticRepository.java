package com.turkcell.soccer.repository;

import com.turkcell.soccer.model.Tactic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TacticRepository extends JpaRepository<Tactic, Long> {

    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Team t WHERE t.tactic.id = :tacticId ")
    boolean isTacticInUse(Long tacticId);
    @Query("SELECT COUNT(t) FROM Team t WHERE t.tactic.id = :tacticId ")
    long countTeamsUsingTactic(Long tacticId);

    boolean existsByDefendersAndMidfieldersAndForwardsAndStyle(
            int defenders, int midfielders, int forwards, Tactic.TacticStyle style
    );

    Optional<Tactic> findByDefendersAndMidfieldersAndForwardsAndStyle(
            int defenders, int midfielders, int forwards, Tactic.TacticStyle style
    );
}
