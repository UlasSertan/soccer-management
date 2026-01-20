package com.turkcell.soccer.repository;

import com.turkcell.soccer.model.Account;
import com.turkcell.soccer.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    
    Optional<Account> findByUsername(String username);
    
    Optional<Account> findByEmail(String email);

    Optional<Account> findByTeam(Team team);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
}
