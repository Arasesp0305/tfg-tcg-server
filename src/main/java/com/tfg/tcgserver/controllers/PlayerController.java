package com.tfg.tcgserver.controllers;

import com.tfg.tcgserver.api.dto.CreateUserProfileRequest;
import com.tfg.tcgserver.api.dto.GameSummaryResponse;
import com.tfg.tcgserver.api.dto.UpdateUserProfileRequest;
import com.tfg.tcgserver.config.AuthenticatedUser;
import com.tfg.tcgserver.models.player.UserProfile;
import com.tfg.tcgserver.service.game.GameService;
import com.tfg.tcgserver.service.player.PlayerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
public class PlayerController extends AuthenticatedController {

    private final PlayerService playerService;
    private final GameService gameService;

    public PlayerController(PlayerService playerService, GameService gameService) {
        this.playerService = playerService;
        this.gameService = gameService;
    }

    @GetMapping("/{uid}")
    public CompletableFuture<UserProfile> getProfile(
            @PathVariable("uid") String uid,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return playerService.getProfile(uid);
    }

    @PostMapping
    public CompletableFuture<UserProfile> createProfile(
            @Valid @RequestBody CreateUserProfileRequest request,
            HttpServletRequest servletRequest
    ) {
        AuthenticatedUser user = authenticatedUser(servletRequest);

        if (!user.uid().equals(request.uid())) {
            throw new SecurityException("No puedes crear un perfil para otro usuario");
        }

        return playerService.createProfile(user.uid(), request.username(), user.email());
    }

    @PutMapping("/{uid}/profile")
    public CompletableFuture<UserProfile> updateProfile(
            @PathVariable("uid") String uid,
            @Valid @RequestBody UpdateUserProfileRequest request,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return playerService.updateProfile(uid, request.username(), request.avatarId());
    }

    @GetMapping("/{uid}/games")
    public CompletableFuture<List<GameSummaryResponse>> getGames(
            @PathVariable("uid") String uid,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return gameService.getGamesForPlayer(uid);
    }
}
