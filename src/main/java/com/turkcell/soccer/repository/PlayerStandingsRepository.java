package com.turkcell.soccer.repository;

import com.turkcell.soccer.model.PlayerStandings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerStandingsRepository extends JpaRepository<PlayerStandings, Long> {

    @Query(
            "SELECT g FROM PlayerStandings g " +
            "WHERE g.league.leagueId = :leagueId " +
            "ORDER BY g.goals DESC"
    )
    List<PlayerStandings> findGoalTable(@Param("leagueId") Long leagueId);
    @Query(
            "SELECT g FROM PlayerStandings g " +
            "WHERE g.league.leagueId = :leagueId " +
            "ORDER BY g.assists DESC"
    )
    List<PlayerStandings> findAssistTable(@Param("leagueId") Long leagueId);

    Optional<PlayerStandings> findById(Long id);
}
