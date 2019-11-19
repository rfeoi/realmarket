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
public class ItemManager implements IManger {

    private HashMap<ItemStack, Double> prices;
    private HashMap<ItemStack, ArrayList<ItemPurchase>> purchaseData;
    private final Runnable runnable;
    private final JavaPlugin plugin;

    private double increaseFactor = 1.01;
    private double decreaseFactor = 0.0001;
    private double amount = 10;

    /**
     * Initiates the ItemManager and starts the update loop
     *
     * @param plugin
     * @param file
     */
    public ItemManager(final JavaPlugin plugin, FileManager file) {
        this.plugin = plugin;

        initPrices(file);
        initPurchaseData(file);

        runnable = new Runnable() {
            public void run() {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable, calculateTime());

                for (ItemStack stack : prices.keySet()) {
                    prices.put(stack, calculatePrice(stack));
                }
            }
        };

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, runnable, calculateTime());
    }

    /**
     * Initiates the prices
     *
     * @param file The prices will be read from here if possible
     */
    private void initPrices(FileManager file) {
        prices = new HashMap<>();
        Object savedPrices = file.getSavedData(this, "prices", prices.getClass());
        if (savedPrices != null) {
            prices = (HashMap<ItemStack, Double>) savedPrices;
        }
    }

    /**
     * Initiates the purchaseData
     *
     * @param file The purchaseData will be read from here if possible
     */
    private void initPurchaseData(FileManager file) {
        purchaseData = new HashMap<>();
        Object savedPurchases = file.getSavedData(this, "purchaseData", purchaseData.getClass());
        if (savedPurchases != null) {
            purchaseData = (HashMap<ItemStack, ArrayList<ItemPurchase>>) savedPurchases;
        }
    }

    /**
     * Gets the price for an ItemStack
     *
     * @param item The stack from which the price should be returned
     * @return The price of the ItemStack
     */
    public Double getPrice(ItemStack item) {
        return prices.get(item);
    }

    /**
     * Gets how many items have been bought and factors in the wighting of this item
     *
     * @param item The item which will be analyzed
     * @return How many items were bought
     */
    public double getBought(ItemStack item) {
        deleteOldEntries(item);

        double buyCount = 0;

        for (ItemPurchase purchase : purchaseData.get(item)) {
            if (purchase.getType() == PurchaseType.BUY) {
                buyCount += purchase.getAmount() * purchase.getWeighting();
            }
            purchase.updateWeighting();
        }
        return buyCount;
    }

    /**
     * Adds an item to the update process
     *
     * @param item The item which will be updated
     * @param startPrice The initial price of this item
     */
    public void registerItem(ItemStack item, double startPrice) {
        prices.put(item, startPrice);
        purchaseData.put(item, new ArrayList<ItemPurchase>());
    }

    /**
     * Removes an item from being updated
     *
     * @param item The item which will be removed
     */
    public void deleteItem(ItemStack item) {
        prices.remove(item);
        purchaseData.remove(item);
    }

    /**
     * Calculates the time on which the plugin should update
     *
     * @return The time
     */
    private int calculateTime() {
        int players = plugin.getServer().getOnlinePlayers().size();
        return 20 * 60 * 50 / (players + 1);
    }

    /**
     * Deletes purchases older than a week
     *
     * @param item The item from which the purchases will be deleted
     */
    private void deleteOldEntries(ItemStack item) {
        Date date = new Date();
        long time = date.getTime();
        long duration = 7 * 24 * 60 * 60 * 1000;

        for (int i = purchaseData.size() - 1; i >= 0; i--) {
            ItemPurchase purchase = purchaseData.get(item).get(i);
            if (purchase.getWeighting() <= 0) {
                purchaseData.get(item).remove(i);
            }
            if (purchase.getBoughtTime() < (time - duration)) {
                purchaseData.get(item).remove(i);
            }
        }
    }

    /**
     * Changes the price of an item depending on how many items were purchaseData
     *
     * @param item The item from which the price will be adjusted
     * @return The new price of the item
     */
    public double calculatePrice(ItemStack item) {
        double currentPrice = getPrice(item);
        double boughtCount = getBought(item);

        if (boughtCount > amount) {
            return currentPrice * (1 - (boughtCount * decreaseFactor));
        }
        return currentPrice * increaseFactor;
    }

    @Override
    public String getManagerName() {
        return "Item";
    }
}
