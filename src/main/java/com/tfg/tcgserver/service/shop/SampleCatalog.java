package com.tfg.tcgserver.service.shop;

import com.tfg.tcgserver.models.cards.Card;
import com.tfg.tcgserver.models.cards.CardEffect;
import com.tfg.tcgserver.models.cards.CardStats;
import com.tfg.tcgserver.models.cards.CardType;
import com.tfg.tcgserver.models.cards.EffectTarget;
import com.tfg.tcgserver.models.shop.PackProduct;

import java.util.List;
import java.util.Map;

public final class SampleCatalog {

    private SampleCatalog() {
    }

    public static Map<String, Card> cards() {
        return Map.of(
                "card_fire_dragon", creature("Dragon de Fuego", "Criatura ofensiva con alto ataque.", 5, 4, 3, 7),
                "card_water_guardian", creature("Guardian de Agua", "Criatura defensiva resistente.", 2, 7, 3, 3),
                "card_stone_titan", creature("Titan de Piedra", "Criatura lenta con mucha vida.", 4, 8, 4, 2),
                "card_shadow_wolf", creature("Lobo Sombrio", "Criatura rapida para rematar objetivos.", 3, 3, 2, 9),
                "card_leaf_beast", creature("Bestia Hoja", "Criatura equilibrada de apoyo.", 3, 5, 2, 5),
                "card_fireball", action("Bola de Fuego", "Inflige 3 puntos de dano a una criatura enemiga.", EffectTarget.ENEMY_CREATURE, 3, 2),
                "card_heal", action("Curacion", "Restaura vida a una criatura aliada.", EffectTarget.OWN_CREATURE, -3, 2),
                "card_quick_strike", action("Golpe Rapido", "Ataque ligero pensado para criaturas veloces.", EffectTarget.ENEMY_CREATURE, 2, 1),
                "card_stone_skin", action("Piel de Piedra", "Accion defensiva para proteger una criatura.", EffectTarget.OWN_CREATURE, 0, 1),
                "card_lightning", action("Rayo", "Inflige 4 puntos de dano a cualquier criatura.", EffectTarget.ANY_CREATURE, 4, 3)
        );
    }

    public static List<PackProduct> packs() {
        return List.of(
                new PackProduct(
                        "pack_basic_001",
                        "Sobre basico",
                        "Incluye 5 cartas de ejemplo para empezar la coleccion.",
                        100,
                        5,
                        List.copyOf(cards().keySet())
                )
        );
    }

    private static Card creature(String name, String text, int attack, int health, int cost, int speed) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setImage("");
        card.setText(text);
        card.setStats(new CardStats(attack, health, cost, speed));
        return card;
    }

    private static Card action(String name, String text, EffectTarget target, int damage, int cost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ACTION);
        card.setImage("");
        card.setText(text);
        card.setEffect(new CardEffect(target, damage, cost));
        return card;
    }
}
