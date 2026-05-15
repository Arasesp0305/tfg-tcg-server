package com.tfg.tcgserver.models.shop;

import java.util.ArrayList;
import java.util.List;

public class PackProduct {

    private String id;
    private String name;
    private String description;
    private int priceCoins;
    private int cardsPerPack;
    private List<String> possibleCardIds = new ArrayList<>();

    public PackProduct() {
    }

    public PackProduct(String id, String name, String description, int priceCoins, int cardsPerPack, List<String> possibleCardIds) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.priceCoins = priceCoins;
        this.cardsPerPack = cardsPerPack;
        this.possibleCardIds = possibleCardIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getPriceCoins() {
        return priceCoins;
    }

    public void setPriceCoins(int priceCoins) {
        this.priceCoins = priceCoins;
    }

    public int getCardsPerPack() {
        return cardsPerPack;
    }

    public void setCardsPerPack(int cardsPerPack) {
        this.cardsPerPack = cardsPerPack;
    }

    public List<String> getPossibleCardIds() {
        return possibleCardIds;
    }

    public void setPossibleCardIds(List<String> possibleCardIds) {
        this.possibleCardIds = possibleCardIds;
    }
}
