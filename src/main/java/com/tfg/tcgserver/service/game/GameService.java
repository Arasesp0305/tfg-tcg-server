package com.tfg.tcgserver.service.game;

import com.tfg.tcgserver.models.cards.Card;
import com.tfg.tcgserver.models.cards.CardEffect;
import com.tfg.tcgserver.models.game.CreatureSelection;
import com.tfg.tcgserver.models.game.FieldCard;
import com.tfg.tcgserver.models.game.FieldCardStatus;
import com.tfg.tcgserver.models.game.FieldCardVisibility;
import com.tfg.tcgserver.models.game.Game;
import com.tfg.tcgserver.models.game.GamePlayerState;
import com.tfg.tcgserver.models.game.GameStatus;
import com.tfg.tcgserver.models.game.Move;
import com.tfg.tcgserver.models.game.MoveType;
import com.tfg.tcgserver.models.game.PlayerTurnSelection;
import com.tfg.tcgserver.models.game.Turn;
import com.tfg.tcgserver.models.rules.GameRules;
import com.tfg.tcgserver.persistence.firebase.CardRepository;
import com.tfg.tcgserver.persistence.firebase.GameRepository;
import com.tfg.tcgserver.persistence.firebase.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class GameService {

    private final GameRepository gameRepository;
    private final ActiveGameStore activeGameStore;
    private final CardRepository cardRepository;
    private final UserProfileRepository userProfileRepository;

    public GameService(GameRepository gameRepository, ActiveGameStore activeGameStore,
                       CardRepository cardRepository, UserProfileRepository userProfileRepository) {
        this.gameRepository = gameRepository;
        this.activeGameStore = activeGameStore;
        this.cardRepository = cardRepository;
        this.userProfileRepository = userProfileRepository;
    }

    public CompletableFuture<Game> getGame(String gameId) {
        return activeGameStore.findById(gameId)
                .map(CompletableFuture::completedFuture)
                .orElseGet(() -> gameRepository.findById(gameId));
    }

    public CompletableFuture<Game> getFinishedGame(String gameId) {
        return gameRepository.findById(gameId);
    }

    public CompletableFuture<String> createGame(String player1Id, String player1DeckId, String player2Id, String player2DeckId) {
        String gameId = "game_" + UUID.randomUUID();
        String now = Instant.now().toString();

        return cardRepository.findAll().thenCompose(catalog -> {
            CompletableFuture<List<String>> deck1Future = userProfileRepository.getDeckActionCardIds(player1Id, player1DeckId, catalog);
            CompletableFuture<List<String>> deck2Future = userProfileRepository.getDeckActionCardIds(player2Id, player2DeckId, catalog);

            return deck1Future.thenCombine(deck2Future, (deck1, deck2) -> {
                GamePlayerState state1 = new GamePlayerState(player1DeckId, 0);
                Collections.shuffle(deck1);
                state1.setActionDeck(deck1);

                GamePlayerState state2 = new GamePlayerState(player2DeckId, 0);
                Collections.shuffle(deck2);
                state2.setActionDeck(deck2);

                Game game = new Game();
                game.setStatus(GameStatus.SELECTING_CREATURES);
                game.setCreatedAt(now);
                game.setStartedAt(now);
                game.setPlayer1Id(player1Id);
                game.setPlayer2Id(player2Id);
                game.getPlayers().put(player1Id, state1);
                game.getPlayers().put(player2Id, state2);

                activeGameStore.save(gameId, game);
                return gameId;
            });
        });
    }

    public CompletableFuture<Void> selectCreatures(String gameId, String playerId, List<String> activeCreatureCardIds, String hiddenCreatureCardId) {
        Game game = activeGameStore.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("La partida activa no existe"));

        if (game.getStatus() != GameStatus.SELECTING_CREATURES) {
            throw new IllegalStateException("La partida no esta en fase de seleccion de criaturas");
        }

        if (activeCreatureCardIds == null || activeCreatureCardIds.size() != GameRules.ACTIVE_CREATURES_PER_PLAYER) {
            throw new IllegalArgumentException("Debes seleccionar exactamente 2 criaturas activas");
        }

        CreatureSelection selection = new CreatureSelection();
        selection.setActiveCreatureCardIds(activeCreatureCardIds);
        selection.setHiddenCreatureCardId(hiddenCreatureCardId);
        selection.setSubmittedAt(Instant.now().toString());
        game.getCreatureSelections().put(playerId, selection);

        boolean bothSelected = game.getCreatureSelections().containsKey(game.getPlayer1Id())
                && game.getCreatureSelections().containsKey(game.getPlayer2Id());

        if (bothSelected) {
            return cardRepository.findAll().thenAccept(catalog -> {
                startFirstTurn(game, catalog);
                activeGameStore.save(gameId, game);
            });
        }

        activeGameStore.save(gameId, game);
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> submitTurnSelection(String gameId, String playerId, List<Move> actions) {
        Game game = activeGameStore.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("La partida activa no existe"));

        if (game.getStatus() != GameStatus.IN_PROGRESS) {
            throw new IllegalStateException("La partida no esta en curso");
        }

        Turn currentTurn = game.getTurns().get(game.getCurrentTurnId());

        if (currentTurn == null) {
            throw new IllegalStateException("La partida no tiene turno actual");
        }

        if (actions == null || actions.size() > GameRules.MAX_ACTIONS_PER_TURN) {
            throw new IllegalArgumentException("Solo puedes jugar hasta 2 acciones por turno");
        }

        GamePlayerState playerState = game.getPlayers().get(playerId);
        PlayerTurnSelection selection = new PlayerTurnSelection();
        selection.setSubmittedAt(Instant.now().toString());

        for (Move move : actions) {
            if (!playerState.getHand().contains(move.getCardId())) {
                throw new IllegalArgumentException("La carta " + move.getCardId() + " no esta en tu mano");
            }

            FieldCard source = currentTurn.getFieldCards().get(move.getSourceFieldCardId());
            if (source == null || !playerId.equals(source.getOwnerId()) || source.getStatus() != FieldCardStatus.ACTIVE) {
                throw new IllegalArgumentException("La criatura origen no es valida o no esta activa");
            }

            if (!currentTurn.getFieldCards().containsKey(move.getTargetFieldCardId())) {
                throw new IllegalArgumentException("La criatura objetivo no existe en el campo");
            }

            String actionId = "action_" + UUID.randomUUID();
            move.setPlayerId(playerId);
            move.setType(MoveType.PLAY_ACTION);
            move.setCreatedAt(selection.getSubmittedAt());
            move.setSpeed(source.getSpeed());
            selection.getActions().put(actionId, move);
        }

        currentTurn.getPlayerSelections().put(playerId, selection);

        boolean bothSubmitted = currentTurn.getPlayerSelections().containsKey(game.getPlayer1Id())
                && currentTurn.getPlayerSelections().containsKey(game.getPlayer2Id());

        if (bothSubmitted) {
            return cardRepository.findAll().thenCompose(catalog -> {
                boolean finished = resolveTurn(currentTurn, game, catalog);

                if (finished) {
                    activeGameStore.remove(gameId);
                    return gameRepository.save(gameId, game);
                }

                startNextTurn(game, catalog);
                activeGameStore.save(gameId, game);
                return CompletableFuture.completedFuture(null);
            });
        }

        activeGameStore.save(gameId, game);
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<Void> finishGame(String gameId, String winnerId) {
        Game game = activeGameStore.remove(gameId)
                .orElseThrow(() -> new IllegalArgumentException("La partida activa no existe"));

        game.setStatus(GameStatus.FINISHED);
        game.setWinnerId(winnerId);
        game.setFinishedAt(Instant.now().toString());

        return gameRepository.save(gameId, game);
    }

    // Returns true if the game ended (a winner was found)
    private boolean resolveTurn(Turn turn, Game game, Map<String, Card> catalog) {
        int order = 1;

        List<Move> moves = turn.getPlayerSelections().values().stream()
                .flatMap(sel -> sel.getActions().values().stream())
                .sorted(Comparator.comparingInt(Move::getSpeed).reversed())
                .toList();

        for (Move move : moves) {
            move.setResolvedOrder(order++);
            turn.getResolvedMoves().put("resolved_move_" + move.getResolvedOrder(), move);

            Card actionCard = catalog.get(move.getCardId());
            if (actionCard == null || actionCard.getEffect() == null) {
                continue;
            }

            CardEffect effect = actionCard.getEffect();
            FieldCard target = turn.getFieldCards().get(move.getTargetFieldCardId());

            if (target == null || target.getStatus() == FieldCardStatus.DEAD) {
                continue;
            }

            int newHealth = target.getCurrentHealth() - effect.getDamage();
            // Cap heals at max health
            target.setCurrentHealth(Math.min(newHealth, target.getMaxHealth()));

            if (target.getCurrentHealth() <= 0) {
                target.setCurrentHealth(0);
                target.setStatus(FieldCardStatus.DEAD);
                GamePlayerState ownerState = game.getPlayers().get(target.getOwnerId());
                ownerState.setDeadCreatures(ownerState.getDeadCreatures() + 1);
                promoteBenchCreature(turn, target.getOwnerId());
            }
        }

        turn.setEndedAt(Instant.now().toString());

        // Check win condition: the player with enough dead creatures loses
        for (Map.Entry<String, GamePlayerState> entry : game.getPlayers().entrySet()) {
            if (entry.getValue().getDeadCreatures() >= GameRules.DEAD_CREATURES_TO_LOSE) {
                String loser = entry.getKey();
                String winner = loser.equals(game.getPlayer1Id()) ? game.getPlayer2Id() : game.getPlayer1Id();
                game.setStatus(GameStatus.FINISHED);
                game.setWinnerId(winner);
                game.setFinishedAt(Instant.now().toString());
                return true;
            }
        }

        return false;
    }

    private void startFirstTurn(Game game, Map<String, Card> catalog) {
        String turnId = "turn_001";
        Instant now = Instant.now();

        Turn firstTurn = new Turn();
        firstTurn.setNumber(1);
        firstTurn.setStartedAt(now.toString());
        firstTurn.setSelectionDeadlineAt(now.plus(GameRules.TURN_SELECTION_SECONDS, ChronoUnit.SECONDS).toString());

        addInitialFieldCards(firstTurn, game.getPlayer1Id(), game.getCreatureSelections().get(game.getPlayer1Id()), 1, catalog);
        addInitialFieldCards(firstTurn, game.getPlayer2Id(), game.getCreatureSelections().get(game.getPlayer2Id()), 4, catalog);

        drawCards(game.getPlayers().get(game.getPlayer1Id()), GameRules.ACTION_CARDS_DRAWN_PER_TURN);
        drawCards(game.getPlayers().get(game.getPlayer2Id()), GameRules.ACTION_CARDS_DRAWN_PER_TURN);

        game.setStatus(GameStatus.IN_PROGRESS);
        game.setCurrentTurnId(turnId);
        game.getTurns().put(turnId, firstTurn);
    }

    private void startNextTurn(Game game, Map<String, Card> catalog) {
        Turn currentTurn = game.getTurns().get(game.getCurrentTurnId());

        discardTurnCards(game, currentTurn);

        drawCards(game.getPlayers().get(game.getPlayer1Id()), GameRules.ACTION_CARDS_DRAWN_PER_TURN);
        drawCards(game.getPlayers().get(game.getPlayer2Id()), GameRules.ACTION_CARDS_DRAWN_PER_TURN);

        int nextNumber = currentTurn.getNumber() + 1;
        String nextTurnId = String.format("turn_%03d", nextNumber);
        Instant now = Instant.now();

        Turn nextTurn = new Turn();
        nextTurn.setNumber(nextNumber);
        nextTurn.setStartedAt(now.toString());
        nextTurn.setSelectionDeadlineAt(now.plus(GameRules.TURN_SELECTION_SECONDS, ChronoUnit.SECONDS).toString());

        // Carry over surviving field cards preserving current health
        for (Map.Entry<String, FieldCard> entry : currentTurn.getFieldCards().entrySet()) {
            if (entry.getValue().getStatus() != FieldCardStatus.DEAD) {
                nextTurn.getFieldCards().put(entry.getKey(), entry.getValue());
            }
        }

        game.setCurrentTurnId(nextTurnId);
        game.getTurns().put(nextTurnId, nextTurn);
    }

    private void discardTurnCards(Game game, Turn turn) {
        for (String playerId : List.of(game.getPlayer1Id(), game.getPlayer2Id())) {
            GamePlayerState state = game.getPlayers().get(playerId);
            PlayerTurnSelection sel = turn.getPlayerSelections().get(playerId);

            // Played cards go to discard and are removed from hand
            if (sel != null) {
                for (Move move : sel.getActions().values()) {
                    state.getDiscardPile().add(move.getCardId());
                    state.getHand().remove(move.getCardId());
                }
            }

            // Remaining unplayed cards also go to discard
            state.getDiscardPile().addAll(state.getHand());
            state.getHand().clear();
        }
    }

    private void drawCards(GamePlayerState state, int count) {
        if (state.getActionDeck().size() < count) {
            List<String> reshuffled = new ArrayList<>(state.getDiscardPile());
            Collections.shuffle(reshuffled);
            state.getActionDeck().addAll(reshuffled);
            state.getDiscardPile().clear();
        }

        int toDraw = Math.min(count, state.getActionDeck().size());
        List<String> drawn = new ArrayList<>(state.getActionDeck().subList(0, toDraw));
        state.getActionDeck().subList(0, toDraw).clear();
        state.getHand().addAll(drawn);
    }

    private void addInitialFieldCards(Turn turn, String playerId, CreatureSelection selection, int startPosition, Map<String, Card> catalog) {
        for (int index = 0; index < selection.getActiveCreatureCardIds().size(); index++) {
            String cardId = selection.getActiveCreatureCardIds().get(index);
            Card card = catalog != null ? catalog.get(cardId) : null;

            FieldCard fieldCard = new FieldCard();
            fieldCard.setOwnerId(playerId);
            fieldCard.setCardId(cardId);
            fieldCard.setPosition(startPosition + index);
            fieldCard.setVisibility(FieldCardVisibility.PUBLIC);
            fieldCard.setStatus(FieldCardStatus.ACTIVE);
            applyCardStats(fieldCard, card);

            turn.getFieldCards().put("field_" + playerId + "_active_" + (index + 1), fieldCard);
        }

        String hiddenCardId = selection.getHiddenCreatureCardId();
        Card hiddenCard = catalog != null ? catalog.get(hiddenCardId) : null;

        FieldCard hiddenCreature = new FieldCard();
        hiddenCreature.setOwnerId(playerId);
        hiddenCreature.setCardId(hiddenCardId);
        hiddenCreature.setPosition(startPosition + GameRules.ACTIVE_CREATURES_PER_PLAYER);
        hiddenCreature.setVisibility(FieldCardVisibility.HIDDEN);
        hiddenCreature.setStatus(FieldCardStatus.BENCH);
        applyCardStats(hiddenCreature, hiddenCard);

        turn.getFieldCards().put("field_" + playerId + "_hidden", hiddenCreature);
    }

    private void applyCardStats(FieldCard fieldCard, Card card) {
        if (card != null && card.getStats() != null) {
            fieldCard.setCurrentHealth(card.getStats().getHealth());
            fieldCard.setMaxHealth(card.getStats().getHealth());
            fieldCard.setAttack(card.getStats().getAttack());
            fieldCard.setSpeed(card.getStats().getSpeed());
        }
    }

    private void promoteBenchCreature(Turn turn, String ownerId) {
        for (FieldCard fc : turn.getFieldCards().values()) {
            if (ownerId.equals(fc.getOwnerId()) && fc.getStatus() == FieldCardStatus.BENCH) {
                fc.setStatus(FieldCardStatus.ACTIVE);
                fc.setVisibility(FieldCardVisibility.PUBLIC);
                break;
            }
        }
    }
}
