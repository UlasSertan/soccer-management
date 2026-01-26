package com.turkcell.soccer.repository;

import com.turkcell.soccer.model.Offer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    Optional<Offer> findById(long id);
    List<Offer> findAllByPlayer_IdAndStatus(long id, Offer.OfferStatus status);
    List<Offer> findAllByPlayer_Id(Long playerId);
    List<Offer> findAllBySellerTeam_Id(Long teamId);
    List<Offer> findAllByBuyerTeam_Id(Long teamId);
}
