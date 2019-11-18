package de.rfeoi.realmarket.purchase;

import org.bukkit.inventory.ItemStack;

public class ItemPurchase {
    private int amount;
    private long boughtTime;
    private Double boughtPrice;
    private PurchaseType type;

    public ItemPurchase(int amount, long boughtTime, Double boughtPrice, PurchaseType type) {
        this.amount = amount;
        this.boughtTime = boughtTime;
        this.boughtPrice = boughtPrice;
        this.type = type;
    }

    public int getAmount() {
        return amount;
    }

    public long getBoughtTime() {
        return boughtTime;
    }

    public Double getBoughtPrice() {
        return boughtPrice;
    }

    public PurchaseType getType() {
        return type;
    }
}
