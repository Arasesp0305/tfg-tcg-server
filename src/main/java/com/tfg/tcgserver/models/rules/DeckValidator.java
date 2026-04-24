package com.tfg.tcgserver.models.rules;

import com.tfg.tcgserver.models.cards.Card;
import com.tfg.tcgserver.models.cards.DeckCard;
import com.tfg.tcgserver.models.player.Deck;
import com.tfg.tcgserver.models.player.DeckCardCount;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DeckValidator {

    public DeckCardCount calculateCardCount(Deck deck, Map<String, Card> cardCatalog) {
        int actionCards = 0;
        int creatureCards = 0;

        if (deck.getCards() != null && cardCatalog != null) {
            for (DeckCard deckCard : deck.getCards().values()) {
                Card card = cardCatalog.get(deckCard.getCardId());

                if (card == null || card.getType() == null) {
                    continue;
                }

                switch (card.getType()) {
                    case ACTION -> actionCards += deckCard.getQuantity();
                    case CREATURE -> creatureCards += deckCard.getQuantity();
                }
            }
        }

        return new DeckCardCount(actionCards, creatureCards, actionCards + creatureCards);
    }

    public boolean isValid(Deck deck, Map<String, Card> cardCatalog) {
        DeckCardCount count = calculateCardCount(deck, cardCatalog);

        return count.getAction() == DeckRules.REQUIRED_ACTION_CARDS
                && count.getCreature() == DeckRules.REQUIRED_CREATURE_CARDS
                && count.getTotal() == DeckRules.REQUIRED_TOTAL_CARDS;
    }
}
