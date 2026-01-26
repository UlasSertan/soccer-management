package com.turkcell.soccer.controller;

import com.turkcell.soccer.annotation.RateLimit;
import com.turkcell.soccer.dto.request.OfferCreationRequest;
import com.turkcell.soccer.dto.request.OfferUpdateRequest;
import com.turkcell.soccer.dto.response.OfferCreationResponse;
import com.turkcell.soccer.dto.response.OfferUpdateResponse;
import com.turkcell.soccer.model.Offer;
import com.turkcell.soccer.service.OfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/offers")
public class OfferController {

    private final OfferService offerService;

    @Autowired
    public OfferController(OfferService offerService) {
        this.offerService = offerService;
    }


    @PostMapping
    @RateLimit(capacity = 10, timeInSeconds = 60)
    public ResponseEntity<OfferCreationResponse> createOffer(@RequestBody OfferCreationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(offerService.createOffer(request));
    }

    @GetMapping("/{offerId}")
    @RateLimit(capacity = 10, timeInSeconds = 60)
    public ResponseEntity<OfferCreationResponse> getOffer(@PathVariable Long offerId) {
        return ResponseEntity.status(HttpStatus.OK).body(offerService.getOffer(offerId));
    }

    @GetMapping("/player/{playerId}")
    @RateLimit(capacity = 10, timeInSeconds = 60)
    public ResponseEntity<List<OfferCreationResponse>> getOffersByPlayer(@PathVariable Long playerId) {
        return ResponseEntity.ok(offerService.getAllOffers(playerId));
    }

    // URL: GET /api/offers/team/5
    @GetMapping("/team/incoming/{teamId}")
    @RateLimit(capacity = 10, timeInSeconds = 60)
    public ResponseEntity<List<OfferCreationResponse>> getIncomingOffers(@PathVariable Long teamId) {
        return ResponseEntity.ok(offerService.getAllOffersByTeam(teamId));
    }

    @GetMapping("/team/outgoing/{teamId}")
    @RateLimit(capacity = 10, timeInSeconds = 60)
    public ResponseEntity<List<OfferCreationResponse>> getOutgoingOffers(@PathVariable Long teamId) {
        return ResponseEntity.ok(offerService.getOutgoingOffers(teamId));
    }



    @PatchMapping
    @RateLimit(capacity = 10, timeInSeconds = 60)
    public ResponseEntity<OfferUpdateResponse> updateOffer(@RequestBody OfferUpdateRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(offerService.updateOffer(request));
    }

    @PostMapping("/{offerId}/accept")
    @RateLimit(capacity = 10, timeInSeconds = 60)
    public ResponseEntity<Void> acceptOffer(@PathVariable Long offerId) {
        offerService.acceptOffer(offerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{offerId}/reject")
    @RateLimit(capacity = 10, timeInSeconds = 60)
    public ResponseEntity<Void> rejectOffer(@PathVariable Long offerId) {
        offerService.rejectOffer(offerId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{offerId}/cancel")
    @RateLimit(capacity = 10, timeInSeconds = 60)
    public ResponseEntity<Void> cancelOffer(@PathVariable Long offerId) {
        offerService.cancelOffer(offerId);
        return ResponseEntity.ok().build();
    }


}
