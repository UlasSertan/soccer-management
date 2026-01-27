package com.turkcell.soccer.repository;

import com.turkcell.soccer.dto.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findById(Long matchId);
    List<Match> findByLeagueId(Long leagueId);
    List<Match> findByLeagueIdAndWeek(Long leagueId, Integer week);
}
