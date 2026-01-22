package com.turkcell.soccer.controller;

import com.turkcell.soccer.dto.TransferListFilter;
import com.turkcell.soccer.dto.request.TransferListRequest;
import com.turkcell.soccer.dto.response.PurchaseResponse;
import com.turkcell.soccer.dto.response.TransferListAdditionResponse;
import com.turkcell.soccer.dto.response.TransferListInfoResponse;
import com.turkcell.soccer.service.TransferListService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transfer-list")
public class TransferListController {



    TransferListService transferListService;

    @Autowired
    public TransferListController(TransferListService transferListService) {
        this.transferListService = transferListService;
    }

    @PostMapping
    public ResponseEntity<TransferListAdditionResponse> addToTransferList(
            @Valid @RequestBody TransferListRequest.Add request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(transferListService.addToTransferList(request));
    }

    @GetMapping
    public ResponseEntity<TransferListInfoResponse> getTransferListInfo(@Valid @ModelAttribute TransferListFilter filter) {
        return ResponseEntity.ok(transferListService.getTransferList(filter));
    }

    // To changePrice
    @PatchMapping ("/players/{playerId}")
    public ResponseEntity<TransferListInfoResponse>
    updateTransferList(@PathVariable Long playerId,
                       @Valid @RequestBody TransferListRequest.UpdatePrice request) {

        return ResponseEntity.ok(transferListService.updateTransferList(playerId, request));
    }

    @DeleteMapping("/players/{playerId}")
    public ResponseEntity<Void> deleteTransferList(@PathVariable Long playerId) {
        transferListService.deleteTransferList(playerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/players/{playerId}")
    public ResponseEntity<PurchaseResponse> purchasePlayer(@PathVariable Long playerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transferListService.purchasePlayer(playerId));
    }














}
