package com.turkcell.soccer.repository;

import com.turkcell.soccer.model.LeagueStandings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeagueStandingsRepository extends JpaRepository<LeagueStandings, Long> {
    @Query(
            "SELECT s FROM LeagueStandings s " +
            "WHERE s.league.leagueId = :leagueId " +
            "ORDER BY " +
            "s.points DESC, " +
            "(s.goalsScored - s.goalsConceded) DESC, " +
            "s.goalsScored DESC"
    )
    List<LeagueStandings> findFinalTable(@Param("leagueId") Long leagueId);
}
