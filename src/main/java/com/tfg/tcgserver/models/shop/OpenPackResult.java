package com.tfg.tcgserver.models.shop;

import com.tfg.tcgserver.models.player.PurchaseTransaction;

import java.util.HashMap;
import java.util.Map;

public class OpenPackResult {

    private PackProduct pack;
    private PurchaseTransaction purchase;
    private Map<String, Integer> rewards = new HashMap<>();

    public OpenPackResult() {
    }

    public OpenPackResult(PackProduct pack, PurchaseTransaction purchase, Map<String, Integer> rewards) {
        this.pack = pack;
        this.purchase = purchase;
        this.rewards = rewards;
    }

    public PackProduct getPack() {
        return pack;
    }

    public void setPack(PackProduct pack) {
        this.pack = pack;
    }

    public PurchaseTransaction getPurchase() {
        return purchase;
    }

    public void setPurchase(PurchaseTransaction purchase) {
        this.purchase = purchase;
    }

    public Map<String, Integer> getRewards() {
        return rewards;
    }

    public void setRewards(Map<String, Integer> rewards) {
        this.rewards = rewards;
    }
}
