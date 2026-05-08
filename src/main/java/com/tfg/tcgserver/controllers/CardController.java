package com.tfg.tcgserver.controllers;

import com.tfg.tcgserver.api.dto.SaveCardRequest;
import com.tfg.tcgserver.models.cards.Card;
import com.tfg.tcgserver.service.cards.CardService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @GetMapping
    public CompletableFuture<Map<String, Card>> getCards() {
        return cardService.getCards();
    }

    @GetMapping("/{cardId}")
    public CompletableFuture<Card> getCard(@PathVariable("cardId") String cardId) {
        return cardService.getCard(cardId);
    }

    @PutMapping("/{cardId}")
    public CompletableFuture<Card> saveCard(
            @PathVariable("cardId") String cardId,
            @Valid @RequestBody SaveCardRequest request
    ) {
        Card card = new Card();
        card.setName(request.name());
        card.setType(request.type());
        card.setImage(request.image());
        card.setText(request.text());
        card.setStats(request.stats());
        card.setEffect(request.effect());

        return cardService.saveCard(cardId, card);
    }

    @DeleteMapping("/{cardId}")
    public CompletableFuture<Void> deleteCard(@PathVariable("cardId") String cardId) {
        return cardService.deleteCard(cardId);
    }
}
