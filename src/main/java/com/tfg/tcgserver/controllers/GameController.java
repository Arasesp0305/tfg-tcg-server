package com.tfg.tcgserver.controllers;

import com.tfg.tcgserver.api.dto.FinishGameRequest;
import com.tfg.tcgserver.api.dto.SelectCreaturesRequest;
import com.tfg.tcgserver.api.dto.SelectedActionRequest;
import com.tfg.tcgserver.api.dto.SubmitTurnSelectionRequest;
import com.tfg.tcgserver.api.dto.SubmitMoveRequest;
import com.tfg.tcgserver.models.game.Game;
import com.tfg.tcgserver.models.game.Move;
import com.tfg.tcgserver.service.game.GameService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/games")
public class GameController extends AuthenticatedController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{gameId}")
    public CompletableFuture<Game> getGame(
            @PathVariable("gameId") String gameId,
            @RequestParam(value = "since", required = false) Long since
    ) {
        return gameService.getGame(gameId, since);
    }

    @GetMapping("/history/{gameId}")
    public CompletableFuture<Game> getFinishedGame(@PathVariable("gameId") String gameId) {
        return gameService.getFinishedGame(gameId);
    }

    @PostMapping("/{gameId}/moves")
    public CompletableFuture<ResponseEntity<Map<String, String>>> submitMove(
            @PathVariable("gameId") String gameId,
            @Valid @RequestBody SubmitMoveRequest request,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(request.playerId(), servletRequest);

        Move move = new Move();
        move.setPlayerId(request.playerId());
        move.setType(request.type());
        move.setCardId(request.cardId());
        move.setSourceFieldCardId(request.sourceFieldCardId());
        move.setTargetFieldCardId(request.targetFieldCardId());

        return gameService.submitTurnSelection(gameId, request.playerId(), java.util.List.of(move))
                .thenApply(ignored -> ResponseEntity.ok(Map.of("status", "MOVE_ACCEPTED")));
    }

    @PostMapping("/{gameId}/creatures")
    public CompletableFuture<ResponseEntity<Map<String, String>>> selectCreatures(
            @PathVariable("gameId") String gameId,
            @Valid @RequestBody SelectCreaturesRequest request,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(request.playerId(), servletRequest);

        return gameService.selectCreatures(
                        gameId,
                        request.playerId(),
                        request.activeCreatureCardIds(),
                        request.hiddenCreatureCardId()
                )
                .thenApply(ignored -> ResponseEntity.ok(Map.of("status", "CREATURES_SELECTED")));
    }

    @PostMapping("/{gameId}/turn-selection")
    public CompletableFuture<ResponseEntity<Map<String, String>>> submitTurnSelection(
            @PathVariable("gameId") String gameId,
            @Valid @RequestBody SubmitTurnSelectionRequest request,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(request.playerId(), servletRequest);

        java.util.List<Move> moves = java.util.Optional.ofNullable(request.actions())
                .orElse(java.util.List.of())
                .stream()
                .map(this::toMove)
                .toList();

        return gameService.submitTurnSelection(gameId, request.playerId(), moves)
                .thenApply(ignored -> ResponseEntity.ok(Map.of("status", "TURN_SELECTION_ACCEPTED")));
    }

    @PostMapping("/{gameId}/finish")
    public CompletableFuture<ResponseEntity<Map<String, String>>> finishGame(
            @PathVariable("gameId") String gameId,
            @RequestBody FinishGameRequest request
    ) {
        return gameService.finishGame(gameId, request.winnerId())
                .thenApply(ignored -> ResponseEntity.ok(Map.of("status", "GAME_SAVED_IN_HISTORY")));
    }

    private Move toMove(SelectedActionRequest action) {
        Move move = new Move();
        move.setCardId(action.actionCardId());
        move.setSourceFieldCardId(action.sourceFieldCardId());
        move.setTargetFieldCardId(action.targetFieldCardId());
        return move;
    }
}
