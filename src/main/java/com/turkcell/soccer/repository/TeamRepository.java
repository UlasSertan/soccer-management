package com.turkcell.soccer.repository;

import com.turkcell.soccer.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByName(String name);
    boolean existsByName(String name);
    @Query(
            "SELECT t FROM Team t LEFT JOIN FETCH t.players " +
            "WHERE t.id = :teamId "

    )
    Team findByIdQuery(@Param("teamId") Long teamId);

    @Query(
            "SELECT DISTINCT t FROM Team t " +
            "LEFT JOIN FETCH t.players "
    )
    List<Team> findAllQuery();
}
