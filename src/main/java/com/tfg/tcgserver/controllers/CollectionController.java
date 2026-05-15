package com.tfg.tcgserver.controllers;

import com.tfg.tcgserver.models.player.UserOwnedCard;
import com.tfg.tcgserver.service.player.PlayerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users/{uid}/collection")
public class CollectionController extends AuthenticatedController {

    private final PlayerService playerService;

    public CollectionController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public CompletableFuture<Map<String, UserOwnedCard>> getCollection(
            @PathVariable("uid") String uid,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return playerService.getCollection(uid);
    }
}
