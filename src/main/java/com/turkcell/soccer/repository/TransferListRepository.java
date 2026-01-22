package com.turkcell.soccer.repository;

import com.turkcell.soccer.model.Player;
import com.turkcell.soccer.model.TransferList;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransferListRepository extends JpaRepository<TransferList, Long>,
        JpaSpecificationExecutor<TransferList> {
    // Add filters

    List<TransferList> findByPlayer_CountryIgnoreCase(String country);
    List<TransferList> findByPriceLessThanEqual(Integer price);
    List<TransferList> findByPriceGreaterThanEqual(Integer price);
    List<TransferList> findByPriceBetween(Integer priceStart, Integer priceEnd);

    List<TransferList> findByPlayer_Team_NameContainingIgnoreCase(String teamName);
    TransferList findByPlayer_IdAndPlayer_Team_Id(Long playerId, Long teamId);
    TransferList findByPlayer_Id(Long playerId);
    boolean existsByPlayer(Player player);
}
