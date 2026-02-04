package io.github.syferie.magicblock.gui;

import de.themoep.inventorygui.GuiStateElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.block.BlockManager;
import io.github.syferie.magicblock.config.ChargeConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 充能 GUI
 * */
public class ChargeGUI {
    private final MagicBlockPlugin plugin;
    private final BlockManager blockManager;
    private final ChargeConfig chargeConfig;
    private final Map<UUID, InventoryGui> openGuis = new ConcurrentHashMap<>();
    private final Map<UUID, ItemStack> originalItems = new ConcurrentHashMap<>();
    private final Map<UUID, ItemStack> placedChargeItems = new ConcurrentHashMap<>();

    public ChargeGUI(MagicBlockPlugin plugin, BlockManager blockManager) {
        this.plugin = plugin;
        this.blockManager = blockManager;
        this.chargeConfig = new ChargeConfig(plugin);
    }

    public void openGUI(Player player, ItemStack magicBlock) {
        if (!blockManager.isMagicBlock(magicBlock)) {
            plugin.sendMessage(player, "messages.must-hold-magic-block");
            return;
        }

        int currentUses = blockManager.getUseTimes(magicBlock);
        if (currentUses == -1) {
            plugin.sendMessage(player, "messages.charge-infinite-uses");
            return;
        }

        UUID playerId = player.getUniqueId();
        originalItems.put(playerId, magicBlock.clone());

        InventoryGui gui = createGUI(player, magicBlock);
        openGuis.put(playerId, gui);

        gui.show(player);
    }

    private InventoryGui createGUI(Player player, ItemStack magicBlock) {
        ChargeConfig.ChargeGUIConfig guiConfig = chargeConfig.getGuiConfig();
        Material blockType = magicBlock.getType();
        ChargeConfig.MaterialChargeConfig materialConfig = chargeConfig.getChargeConfig(blockType);

        InventoryGui gui = new InventoryGui(plugin, guiConfig.title, guiConfig.rows);

        addVaultChargeButton(gui, player, materialConfig, guiConfig);
        addPointsChargeButton(gui, player, materialConfig, guiConfig);
        addItemChargeButton(gui, player, materialConfig, guiConfig);
        addCloseButton(gui, player, guiConfig);

        gui.setCloseAction(action -> {
            closeGUI(player);
            return false;
        });

        return gui;
    }

    private void addVaultChargeButton(InventoryGui gui, Player player, 
                                   ChargeConfig.MaterialChargeConfig materialConfig, 
                                   ChargeConfig.ChargeGUIConfig guiConfig) {
        
        ChargeConfig.ChargeGUIConfig.ButtonConfig buttonConfig = guiConfig.buttons.get("vault-charge");
        if (buttonConfig == null) return;

        StaticGuiElement button;
        if (materialConfig.vaultEnabled) {
            double balance = getPlayerBalance(player);
            String buttonText = replaceVariables(buttonConfig.name, 
                new HashMap<String, String>() {{
                    put("{cost}", String.valueOf(materialConfig.vaultCost));
                    put("{uses}", String.valueOf(materialConfig.vaultUses));
                    put("{balance}", String.format("%.2f", balance));
                }});

            button = new StaticGuiElement(buttonConfig.slot,
                createButtonItem(buttonConfig.material, buttonText, buttonConfig.lore, 
                    new HashMap<String, String>() {{
                        put("{cost}", String.valueOf(materialConfig.vaultCost));
                        put("{uses}", String.valueOf(materialConfig.vaultUses));
                        put("{balance}", String.format("%.2f", balance));
                    }}),
                click -> {
                    handleVaultCharge(player, materialConfig);
                    return true;
                });
        } else {
            button = new StaticGuiElement(buttonConfig.slot,
                createButtonItem(buttonConfig.disabledMaterial, buttonConfig.disabledName, 
                    buttonConfig.disabledLore, new HashMap<>()),
                click -> true);
        }

        gui.addElement(button);
    }

    private void addPointsChargeButton(InventoryGui gui, Player player, 
                                    ChargeConfig.MaterialChargeConfig materialConfig, 
                                    ChargeConfig.ChargeGUIConfig guiConfig) {
        
        ChargeConfig.ChargeGUIConfig.ButtonConfig buttonConfig = guiConfig.buttons.get("points-charge");
        if (buttonConfig == null) return;

        StaticGuiElement button;
        if (materialConfig.pointsEnabled && plugin.getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            int points = getPlayerPoints(player);
            String buttonText = replaceVariables(buttonConfig.name, 
                new HashMap<String, String>() {{
                    put("{cost}", String.valueOf(materialConfig.pointsCost));
                    put("{uses}", String.valueOf(materialConfig.pointsUses));
                    put("{points}", String.valueOf(points));
                }});

            button = new StaticGuiElement(buttonConfig.slot,
                createButtonItem(buttonConfig.material, buttonText, buttonConfig.lore, 
                    new HashMap<String, String>() {{
                        put("{cost}", String.valueOf(materialConfig.pointsCost));
                        put("{uses}", String.valueOf(materialConfig.pointsUses));
                        put("{points}", String.valueOf(points));
                    }}),
                click -> {
                    handlePointsCharge(player, materialConfig);
                    return true;
                });
        } else {
            button = new StaticGuiElement(buttonConfig.slot,
                createButtonItem(buttonConfig.disabledMaterial, buttonConfig.disabledName, 
                    buttonConfig.disabledLore, new HashMap<>()),
                click -> true);
        }

        gui.addElement(button);
    }

    private void addItemChargeButton(InventoryGui gui, Player player, 
                                  ChargeConfig.MaterialChargeConfig materialConfig, 
                                  ChargeConfig.ChargeGUIConfig guiConfig) {
        
        ChargeConfig.ChargeGUIConfig.ButtonConfig buttonConfig = guiConfig.buttons.get("item-charge");
        if (buttonConfig == null) return;

        StaticGuiElement button;
        if (materialConfig.nbtEnabled && !materialConfig.nbtItems.isEmpty()) {
            ChargeConfig.NBTItemConfig validItem = findValidChargeItem(player, materialConfig.nbtItems);
            
            String buttonText;
            Map<String, String> loreReplacements = new HashMap<>();
            
            if (validItem != null) {
                buttonText = replaceVariables(buttonConfig.name, new HashMap<String, String>() {{
                    put("{item}", validItem.material.name());
                    put("{cost}", String.valueOf(validItem.cost));
                    put("{uses}", String.valueOf(validItem.uses));
                }});
                
                loreReplacements.put("{item}", validItem.material.name());
                loreReplacements.put("{cost}", String.valueOf(validItem.cost));
                loreReplacements.put("{uses}", String.valueOf(validItem.uses));
            } else {
                buttonText = replaceVariables(buttonConfig.name, new HashMap<>());
                loreReplacements.put("{item}", "无");
                loreReplacements.put("{cost}", "0");
                loreReplacements.put("{uses}", "0");
            }

            button = new StaticGuiElement(buttonConfig.slot,
                createButtonItem(buttonConfig.material, buttonText, buttonConfig.lore, loreReplacements),
                click -> {
                    handleItemCharge(player, materialConfig);
                    return true;
                });
        } else {
            String materialName = materialConfig.nbtEnabled ?
                buttonConfig.noConfigMaterial : buttonConfig.disabledMaterial;
            String name = materialConfig.nbtEnabled ?
                buttonConfig.noConfigName : buttonConfig.disabledName;
            List<String> lore = materialConfig.nbtEnabled ?
                buttonConfig.noConfigLore : buttonConfig.disabledLore;

            button = new StaticGuiElement(buttonConfig.slot,
                createButtonItem(materialName, name, lore, new HashMap<>()),
                click -> true);
        }

        gui.addElement(button);
    }

    private void addCloseButton(InventoryGui gui, Player player, ChargeConfig.ChargeGUIConfig guiConfig) {
        ChargeConfig.ChargeGUIConfig.ButtonConfig buttonConfig = guiConfig.buttons.get("close");
        if (buttonConfig == null) return;

        StaticGuiElement button = new StaticGuiElement(buttonConfig.slot,
            createButtonItem(buttonConfig.material, buttonConfig.name, buttonConfig.lore, new HashMap<>()),
            click -> {
                gui.close(player);
                return true;
            });

        gui.addElement(button);
    }

    private void handleVaultCharge(Player player, ChargeConfig.MaterialChargeConfig materialConfig) {
        double balance = getPlayerBalance(player);
        if (balance < materialConfig.vaultCost) {
            plugin.sendMessage(player, "messages.charge-no-money", 
                String.format("%.2f", materialConfig.vaultCost), 
                String.format("%.2f", balance));
            return;
        }

        if (withdrawMoney(player, materialConfig.vaultCost)) {
            int addedUses = materialConfig.vaultUses;
            blockManager.addUseTimes(originalItems.get(player.getUniqueId()), addedUses);
            plugin.sendMessage(player, "messages.charge-success-vault", 
                String.format("%.2f", materialConfig.vaultCost), 
                String.valueOf(addedUses));
        }
    }

    private void handlePointsCharge(Player player, ChargeConfig.MaterialChargeConfig materialConfig) {
        int points = getPlayerPoints(player);
        if (points < materialConfig.pointsCost) {
            plugin.sendMessage(player, "messages.charge-no-points", 
                String.valueOf(materialConfig.pointsCost), 
                String.valueOf(points));
            return;
        }

        if (withdrawPoints(player, materialConfig.pointsCost)) {
            int addedUses = materialConfig.pointsUses;
            blockManager.addUseTimes(originalItems.get(player.getUniqueId()), addedUses);
            plugin.sendMessage(player, "messages.charge-success-points", 
                String.valueOf(materialConfig.pointsCost), 
                String.valueOf(addedUses));
        }
    }

    private void handleItemCharge(Player player, ChargeConfig.MaterialChargeConfig materialConfig) {
        UUID playerId = player.getUniqueId();
        ItemStack chargeItem = placedChargeItems.get(playerId);
        
        if (chargeItem == null) {
            plugin.sendMessage(player, "messages.charge-please-place-item");
            return;
        }

        ChargeConfig.NBTItemConfig validConfig = getValidItemConfig(chargeItem, materialConfig.nbtItems);
        if (validConfig == null) {
            plugin.sendMessage(player, "messages.charge-invalid-item");
            return;
        }

        int requiredAmount = validConfig.cost;
        int availableAmount = chargeItem.getAmount();
        if (availableAmount < requiredAmount) {
            plugin.sendMessage(player, "messages.charge-insufficient-items", 
                String.valueOf(requiredAmount), 
                String.valueOf(availableAmount));
            return;
        }

        if (availableAmount == requiredAmount) {
            placedChargeItems.remove(playerId);
        } else {
            chargeItem.setAmount(availableAmount - requiredAmount);
            placedChargeItems.put(playerId, chargeItem);
        }

        int addedUses = validConfig.uses;
        blockManager.addUseTimes(originalItems.get(playerId), addedUses);
        plugin.sendMessage(player, "messages.charge-success-item", 
            String.valueOf(requiredAmount), 
            chargeItem.getType().name(), 
            String.valueOf(addedUses));
    }

    private double getPlayerBalance(Player player) {
        try {
            net.milkbowl.vault.economy.Economy economy =
                    Bukkit.getServer().getServicesManager()
                        .getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
            return economy.getBalance(player);
        } catch (Exception e) {
            plugin.debug("获取玩家余额失败: " + e.getMessage());
            return 0.0;
        }
    }

    private int getPlayerPoints(Player player) {
        try {
            org.black_ixx.playerpoints.PlayerPoints api = 
                (org.black_ixx.playerpoints.PlayerPoints) plugin.getServer()
                    .getPluginManager().getPlugin("PlayerPoints");
            return api.getAPI().look(player.getUniqueId());
        } catch (Exception e) {
            plugin.debug("获取玩家点券失败: " + e.getMessage());
            return 0;
        }
    }

    private boolean withdrawMoney(Player player, double amount) {
        try {
            net.milkbowl.vault.economy.Economy economy =
                    Bukkit.getServer().getServicesManager()
                        .getRegistration(net.milkbowl.vault.economy.Economy.class).getProvider();
            return economy.withdrawPlayer(player, amount).transactionSuccess();
        } catch (Exception e) {
            plugin.debug("扣除金币失败: " + e.getMessage());
            return false;
        }
    }

    private boolean withdrawPoints(Player player, int amount) {
        try {
            org.black_ixx.playerpoints.PlayerPoints api = 
                (org.black_ixx.playerpoints.PlayerPoints) plugin.getServer()
                    .getPluginManager().getPlugin("PlayerPoints");
            return api.getAPI().take(player.getUniqueId(), amount);
        } catch (Exception e) {
            plugin.debug("扣除点券失败: " + e.getMessage());
            return false;
        }
    }

    private ChargeConfig.NBTItemConfig findValidChargeItem(Player player, List<ChargeConfig.NBTItemConfig> nbtItems) {
        for (ChargeConfig.NBTItemConfig config : nbtItems) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == config.material) {
                    if (isValidChargeItem(item, nbtItems)) {
                        return config;
                    }
                }
            }
        }
        return null;
    }

    private boolean isValidChargeItem(ItemStack item, List<ChargeConfig.NBTItemConfig> nbtItems) {
        if (item == null || !item.hasItemMeta()) return false;
        
        for (ChargeConfig.NBTItemConfig config : nbtItems) {
            if (item.getType() == config.material) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer() != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private ChargeConfig.NBTItemConfig getValidItemConfig(ItemStack item, List<ChargeConfig.NBTItemConfig> nbtItems) {
        if (item == null) return null;
        
        for (ChargeConfig.NBTItemConfig config : nbtItems) {
            if (item.getType() == config.material) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.getPersistentDataContainer() != null) {
                    return config;
                }
            }
        }
        return null;
    }

    private ItemStack createButtonItem(String materialName, String name, List<String> lore, 
                                  Map<String, String> replacements) {
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.STONE;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', replaceVariables(name, replacements)));
            
            if (lore != null && !lore.isEmpty()) {
                List<String> processedLore = new ArrayList<>();
                for (String line : lore) {
                    processedLore.add(ChatColor.translateAlternateColorCodes('&', replaceVariables(line, replacements)));
                }
                meta.setLore(processedLore);
            }
            
            item.setItemMeta(meta);
        }
        
        return item;
    }

    private String replaceVariables(String text, Map<String, String> replacements) {
        String result = text;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            if (entry.getValue() != null) {
                result = result.replace(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public void closeGUI(Player player) {
        UUID playerId = player.getUniqueId();
        InventoryGui gui = openGuis.remove(playerId);
        if (gui != null) {
            gui.close();
        }
        
        ItemStack chargeItem = placedChargeItems.remove(playerId);
        if (chargeItem != null) {
            player.getInventory().addItem(chargeItem);
        }
        
        originalItems.remove(playerId);
    }

    public void reload() {
        chargeConfig.reload();
    }
}