package com.tfg.tcgserver.controllers;

import com.tfg.tcgserver.api.dto.SaveDeckRequest;
import com.tfg.tcgserver.models.player.Deck;
import com.tfg.tcgserver.service.player.DeckService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users/{uid}/decks")
public class DeckController extends AuthenticatedController {

    private final DeckService deckService;

    public DeckController(DeckService deckService) {
        this.deckService = deckService;
    }

    @GetMapping
    public CompletableFuture<Map<String, Deck>> getDecks(
            @PathVariable("uid") String uid,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return deckService.getDecks(uid);
    }

    @GetMapping("/{deckId}")
    public CompletableFuture<Deck> getDeck(
            @PathVariable("uid") String uid,
            @PathVariable("deckId") String deckId,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return deckService.getDeck(uid, deckId);
    }

    @PostMapping
    public CompletableFuture<Map<String, String>> createDeck(
            @PathVariable("uid") String uid,
            @Valid @RequestBody SaveDeckRequest request,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return deckService.createDeck(uid, toDeck(request))
                .thenApply(deckId -> Map.of("deckId", deckId));
    }

    @PutMapping("/{deckId}")
    public CompletableFuture<Deck> saveDeck(
            @PathVariable("uid") String uid,
            @PathVariable("deckId") String deckId,
            @Valid @RequestBody SaveDeckRequest request,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return deckService.saveDeck(uid, deckId, toDeck(request));
    }

    @DeleteMapping("/{deckId}")
    public CompletableFuture<Void> deleteDeck(
            @PathVariable("uid") String uid,
            @PathVariable("deckId") String deckId,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return deckService.deleteDeck(uid, deckId);
    }

    @PostMapping("/{deckId}/select")
    public CompletableFuture<Void> selectDeck(
            @PathVariable("uid") String uid,
            @PathVariable("deckId") String deckId,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(uid, servletRequest);
        return deckService.selectDeck(uid, deckId);
    }

    private Deck toDeck(SaveDeckRequest request) {
        Deck deck = new Deck();
        deck.setName(request.name());
        deck.setCards(request.cards());
        return deck;
    }
}
