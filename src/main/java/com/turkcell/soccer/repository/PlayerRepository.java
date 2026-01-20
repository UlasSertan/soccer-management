package com.turkcell.soccer.repository;

import com.turkcell.soccer.model.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByFirstName(String firstName);
    boolean existsByFirstName(String firstName);
}
