package io.github.syferie.magicblock.charge;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.block.BlockManager;
import io.github.syferie.magicblock.config.ChargeConfig;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;

public class ChargeManager {
    private final MagicBlockPlugin plugin;
    private final BlockManager blockManager;
    private final ChargeConfig chargeConfig;

    public ChargeManager(MagicBlockPlugin plugin, BlockManager blockManager) {
        this.plugin = plugin;
        this.blockManager = blockManager;
        this.chargeConfig = new ChargeConfig(plugin);
    }

//    public ChargeResult processVaultCharge(Player player, ItemStack magicBlock, int chargeCount) {
//        Material blockType = magicBlock.getType();
//        ChargeConfig.MaterialChargeConfig config = chargeConfig.getChargeConfig(blockType);
//
//        if (!config.vaultEnabled) {
//            return new ChargeResult(false, "messages.charge-vault-disabled", null);
//        }
//
//        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
//            return new ChargeResult(false, "messages.charge-vault-not-found", null);
//        }
//
//        double totalCost = config.vaultCost * chargeCount;
//        double balance = getPlayerBalance(player);
//
//        if (balance < totalCost) {
//            return new ChargeResult(false, "messages.charge-no-money",
//                new Object[]{String.format("%.2f", totalCost), String.format("%.2f", balance)});
//        }
//
//        if (withdrawMoney(player, totalCost)) {
//            int addedUses = config.vaultUses * chargeCount;
//            blockManager.addUseTimes(magicBlock, addedUses);
//            return new ChargeResult(true, "messages.charge-success-vault",
//                new Object[]{String.format("%.2f", totalCost), String.valueOf(addedUses)});
//        }
//
//        return new ChargeResult(false, "messages.charge-failed", null);
//    }
//
//    public ChargeResult processPointsCharge(Player player, ItemStack magicBlock, int chargeCount) {
//        Material blockType = magicBlock.getType();
//        ChargeConfig.MaterialChargeConfig config = chargeConfig.getChargeConfig(blockType);
//
//        if (!config.pointsEnabled) {
//            return new ChargeResult(false, "messages.charge-points-disabled", null);
//        }
//
//        if (plugin.getServer().getPluginManager().getPlugin("PlayerPoints") == null) {
//            return new ChargeResult(false, "messages.charge-playerpoints-not-found", null);
//        }
//
//        int totalCost = config.pointsCost * chargeCount;
//        int points = getPlayerPoints(player);
//
//        if (points < totalCost) {
//            return new ChargeResult(false, "messages.charge-no-points",
//                new Object[]{String.valueOf(totalCost), String.valueOf(points)});
//        }
//
//        if (withdrawPoints(player, totalCost)) {
//            int addedUses = config.pointsUses * chargeCount;
//            blockManager.addUseTimes(magicBlock, addedUses);
//            return new ChargeResult(true, "messages.charge-success-points",
//                new Object[]{String.valueOf(totalCost), String.valueOf(addedUses)});
//        }
//
//        return new ChargeResult(false, "messages.charge-failed", null);
//    }
//
//    public ChargeResult processItemCharge(Player player, ItemStack magicBlock, ItemStack chargeItem) {
//        Material blockType = magicBlock.getType();
//        ChargeConfig.MaterialChargeConfig config = chargeConfig.getChargeConfig(blockType);
//
//        if (!config.nbtEnabled || config.nbtItems.isEmpty()) {
//            return new ChargeResult(false, "messages.charge-item-disabled", null);
//        }
//
//        ChargeConfig.NBTItemConfig itemConfig = getValidItemConfig(chargeItem, config.nbtItems);
//        if (itemConfig == null) {
//            return new ChargeResult(false, "messages.charge-invalid-item", null);
//        }
//
//        int requiredAmount = itemConfig.cost;
//        int availableAmount = chargeItem.getAmount();
//
//        if (availableAmount < requiredAmount) {
//            return new ChargeResult(false, "messages.charge-insufficient-items",
//                new Object[]{String.valueOf(requiredAmount), String.valueOf(availableAmount)});
//        }
//
//        // 检查玩家背包中是否有足够的充能物品
//        int playerHasAmount = countValidItems(player, itemConfig);
//        if (playerHasAmount < requiredAmount) {
//            return new ChargeResult(false, "messages.charge-no-items",
//                new Object[]{String.valueOf(requiredAmount), String.valueOf(playerHasAmount)});
//        }
//
//        // 扣除物品
//        if (!removeChargeItems(player, itemConfig, requiredAmount)) {
//            return new ChargeResult(false, "messages.charge-failed", null);
//        }
//
//        int addedUses = itemConfig.uses;
//        blockManager.addUseTimes(magicBlock, addedUses);
//
//        return new ChargeResult(true, "messages.charge-success-item",
//            new Object[]{String.valueOf(requiredAmount), chargeItem.getType().name(), String.valueOf(addedUses)});
//    }

    /**
     * 是否支持充能
     * */
    public boolean canCharge(Player player, ItemStack magicBlock) {
        if (!blockManager.isMagicBlock(magicBlock)) {
            return false;
        }

        Material blockType = magicBlock.getType();
        ChargeConfig.MaterialChargeConfig config = chargeConfig.getChargeConfig(blockType);

        // 检查是否至少有一种充能方式可用
        if (config.vaultEnabled && plugin.getServer().getPluginManager().getPlugin("Vault") != null) {
            return true;
        }
        if (config.pointsEnabled && plugin.getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            return true;
        }
        if (config.nbtEnabled && !config.nbtItems.isEmpty() && hasValidChargeItems(player, config.nbtItems)) {
            return true;
        }

        return false;
    }

    public ChargeConfig.MaterialChargeConfig getChargeConfig(Material material) {
        return chargeConfig.getChargeConfig(material);
    }

    private ChargeConfig.NBTItemConfig getValidItemConfig(ItemStack item, List<ChargeConfig.NBTItemConfig> nbtItems) {
        if (item == null || !item.hasItemMeta()) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || meta.getPersistentDataContainer() == null) {
            return null;
        }

        for (ChargeConfig.NBTItemConfig config : nbtItems) {
            if (item.getType() == config.material) {
                if (hasNBTTag(meta, config.nbtKey, config.nbtValue)) {
                    return config;
                }
            }
        }
        return null;
    }

    private boolean hasNBTTag(ItemMeta meta, String key, String value) {
        try {
            NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
            String storedValue = meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
            return value.equals(storedValue);
        } catch (Exception e) {
            plugin.debug("检查NBT标签失败: " + e.getMessage());
            return false;
        }
    }

    private boolean hasValidChargeItems(Player player, List<ChargeConfig.NBTItemConfig> nbtItems) {
        for (ChargeConfig.NBTItemConfig config : nbtItems) {
            if (countValidItems(player, config) >= config.cost) {
                return true;
            }
        }
        return false;
    }

    private int countValidItems(Player player, ChargeConfig.NBTItemConfig config) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == config.material) {
                if (getValidItemConfig(item, java.util.Collections.singletonList(config)) != null) {
                    count += item.getAmount();
                }
            }
        }
        return count;
    }

//    private boolean removeChargeItems(Player player, ChargeConfig.NBTItemConfig config, int amount) {
//        int toRemove = amount;
//        ItemStack[] contents = player.getInventory().getContents();
//
//        for (int i = 0; i < contents.length && toRemove > 0; i++) {
//            ItemStack item = contents[i];
//            if (item != null && item.getType() == config.material) {
//                if (getValidItemConfig(item, java.util.Collections.singletonList(config)) != null) {
//                    int itemAmount = item.getAmount();
//                    if (itemAmount <= toRemove) {
//                        player.getInventory().setItem(i, null);
//                        toRemove -= itemAmount;
//                    } else {
//                        item.setAmount(itemAmount - toRemove);
//                        toRemove = 0;
//                    }
//                }
//            }
//        }
//
//        return toRemove == 0;
//    }

    public void reload() {
        chargeConfig.reload();
    }

//    public static class ChargeResult {
//        private final boolean success;
//        private final String messageKey;
//        private final Object[] messageArgs;
//
//        public ChargeResult(boolean success, String messageKey, Object[] messageArgs) {
//            this.success = success;
//            this.messageKey = messageKey;
//            this.messageArgs = messageArgs;
//        }
//
//        public boolean isSuccess() {
//            return success;
//        }
//
//        public String getMessageKey() {
//            return messageKey;
//        }
//
//        public Object[] getMessageArgs() {
//            return messageArgs;
//        }
//    }
}