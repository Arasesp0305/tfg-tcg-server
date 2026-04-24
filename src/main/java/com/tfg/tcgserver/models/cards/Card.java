package com.tfg.tcgserver.models.cards;

public class Card {

    private String name;
    private CardType type;
    private String image;
    private String text;
    private CardStats stats;
    private CardEffect effect;

    public Card() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CardType getType() {
        return type;
    }

    public void setType(CardType type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public CardStats getStats() {
        return stats;
    }

    public void setStats(CardStats stats) {
        this.stats = stats;
    }

    public CardEffect getEffect() {
        return effect;
    }

    public void setEffect(CardEffect effect) {
        this.effect = effect;
    }
}
