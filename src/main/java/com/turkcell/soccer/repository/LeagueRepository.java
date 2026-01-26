package com.turkcell.soccer.repository;

import com.turkcell.soccer.model.League;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeagueRepository extends JpaRepository<League, Long> {

    Optional<League> findByLeagueId(Long id);
}
