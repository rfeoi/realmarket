package de.rfeoi.realmarket.managers;

import de.rfeoi.realmarket.purchase.ItemPurchase;
import de.rfeoi.realmarket.purchase.PurchaseType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Runs a scheduler (every 10 Minutes & when an item is purchaseData) to automatically adjust prices, by percentage.
 * Holds prices
 */
public class ItemManager implements IManger{

    private HashMap<ItemStack, Double> prices;
    private HashMap<ItemStack, ArrayList<ItemPurchase>> purchaseData;
    private final Runnable runnable;

    public ItemManager(final JavaPlugin plugin, FileManager file) {
        prices = new HashMap<ItemStack, Double>();
        Object savedPrices = file.getSavedData(this, "prices", prices.getClass());
        if (savedPrices != null) {
            prices = (HashMap<ItemStack, Double>) savedPrices;
        }

        purchaseData = new HashMap<ItemStack, ArrayList<ItemPurchase>>();
        Object savedPurchases = file.getSavedData(this, "purchaseData", purchaseData.getClass());
        if (savedPurchases != null) {
            purchaseData = (HashMap<ItemStack, ArrayList<ItemPurchase>>) savedPurchases;
        }

        runnable = new Runnable() {
            public void run() {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable, 20*60*10);

                for(ItemStack stack : prices.keySet()) {
                    calculatePrice(stack);
                }
            }
        };

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable, 20*60*10);
    }

    public Double getPrice(ItemStack item) {
        return prices.get(item);
    }

    public Integer getBought(ItemStack item) {
        deleteOldEntries(item);

        int buyCount = 0;

        for (ItemPurchase purchase : purchaseData.get(item)) {
            if(purchase.getType() == PurchaseType.BUY) {
                buyCount += purchase.getAmount();
            }
        }
        return buyCount;
    }

    public void registerItem(ItemStack item, double startPrice) {
        prices.put(item, startPrice);
        purchaseData.put(item, new ArrayList<ItemPurchase>());
    }

    public void deleteItem(ItemStack item) {
        prices.remove(item);
        purchaseData.remove(item);
    }

    /**
     * Deletes purchases older than a week
     * @param item The item from which the purchases will be deleted
     */
    private void deleteOldEntries(ItemStack item) {
        Date date = new Date();
        long time = date.getTime();
        long duration = 7*24*60*60*1000;

        for(int i = 0; i< purchaseData.size(); i++) {
            ItemPurchase purchase = purchaseData.get(item).get(i);
            if(purchase.getBoughtTime() < (time-duration)) {
                purchaseData.get(item).remove(i);
            }
        }
    }

    /**
     * Changes the price of an item depending on how many items were purchaseData
     * @param item The item from which the price will be adjusted
     * @return The new price of the item
     */
    public double calculatePrice(ItemStack item) {
        double currentPrice = getPrice(item);
        double boughtCount = getBought(item);

        int x = 1000;
        double y = 0.8;
        double z = 1.1;

        if(boughtCount>x) {
            return currentPrice*y;
        }
        return currentPrice*z;
    }

    @Override
    public String getManagerName() {
        return "Item";
    }
}
