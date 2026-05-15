package com.tfg.tcgserver.controllers;

import com.tfg.tcgserver.api.dto.CreatePurchaseTransactionRequest;
import com.tfg.tcgserver.models.player.PurchaseTransaction;
import com.tfg.tcgserver.service.player.PlayerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users/{uid}/purchases")
public class PurchaseController extends AuthenticatedController {

    private final PlayerService playerService;

    public PurchaseController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public CompletableFuture<Map<String, PurchaseTransaction>> getPurchaseHistory(
            @PathVariable("uid") String uid,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return playerService.getPurchaseHistory(uid);
    }

    @PostMapping
    public CompletableFuture<PurchaseTransaction> registerPurchase(
            @PathVariable("uid") String uid,
            @Valid @RequestBody CreatePurchaseTransactionRequest request,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return playerService.registerPurchase(
                uid,
                request.itemId(),
                request.itemName(),
                request.coinAmount(),
                request.description()
        );
    }
}
