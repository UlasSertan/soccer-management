package com.turkcell.soccer.repository;

import com.turkcell.soccer.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByFirstName(String firstName);
    Optional<Player> findByIdAndTeam_Id(Long id, Long teamId);
    boolean existsByFirstName(String firstName);
}
