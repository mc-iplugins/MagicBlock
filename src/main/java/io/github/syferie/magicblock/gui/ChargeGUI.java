package io.github.syferie.magicblock.gui;

import com.google.gson.Gson;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.block.BlockManager;
import io.github.syferie.magicblock.config.ChargeConfig;
import io.github.syferie.magicblock.core.AbstractMagicItem;
import io.github.syferie.magicblock.hook.PlayerPointsHook;
import io.github.syferie.magicblock.hook.VaultHook;
import io.github.syferie.magicblock.util.LoreUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 充能 GUI
 * */
public class ChargeGUI {
    private final MagicBlockPlugin plugin;
    private final BlockManager blockManager;
    private final ChargeConfig chargeConfig;

    public ChargeGUI(MagicBlockPlugin plugin, BlockManager blockManager) {
        this.plugin = plugin;
        this.blockManager = blockManager;
        this.chargeConfig = new ChargeConfig(plugin);
    }

    public void openGUI(Player player) {
        ChargeConfig.ChargeGUIConfig guiConfig = chargeConfig.getGuiConfig();
        InventoryGui gui = new InventoryGui(plugin, guiConfig.title, guiConfig.rows);

        updateGUI(gui, player, null, null);
    }

    private void updateGUI(InventoryGui gui, Player player, @Nullable ItemStack magicItem, @Nullable ItemStack neededItem) {
        updateGUI(gui, player, magicItem, neededItem, true);
    }

    private void updateGUI(InventoryGui gui, Player player, @Nullable ItemStack magicItem, @Nullable ItemStack neededItem, boolean show) {
        ChargeConfig.ChargeGUIConfig guiConfig = chargeConfig.getGuiConfig();
        ChargeConfig.MaterialChargeConfig defaultButtonConfig = chargeConfig.getChargeConfig(
                magicItem != null ? magicItem.getType() : null);

        addVaultChargeButton(gui, player, defaultButtonConfig, guiConfig);
        addPointsChargeButton(gui, player, defaultButtonConfig, guiConfig);
        addItemChargeButton(gui, player, defaultButtonConfig, guiConfig);
        addCloseButton(gui, player, guiConfig);
        addStainedButton(gui, guiConfig);

        Inventory inv = Bukkit.createInventory(null, InventoryType.CHEST);
        if (magicItem != null) {
            inv.setItem(0, magicItem);
        }
        if (neededItem != null) {
            inv.setItem(1, neededItem);
        }
        GuiStorageElement storageElement = new GuiStorageElement(' ', inv);
        storageElement.setAction(action -> {
            if (action.getSlot() == 4 || action.getSlot() == 15) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    refreshGUIWithItems(gui, action.getRawEvent().getInventory(), player);
                }, 5L);
            }
            return false; // 不取消事件
        });
        gui.addElement(storageElement);

        gui.setCloseAction(action -> {
            // 归还玩家物品
            returnItem2Player(action.getEvent().getInventory(), player);
            return false;
        });
        if (show) {
            gui.show(player);
        } else {
            gui.draw(player);
        }
    }

    private void addVaultChargeButton(InventoryGui gui, Player player, 
                                   ChargeConfig.MaterialChargeConfig materialConfig, 
                                   ChargeConfig.ChargeGUIConfig guiConfig) {
        
        ChargeConfig.ChargeGUIConfig.ButtonConfig buttonConfig = guiConfig.buttons.get("v");
        if (buttonConfig == null) return;

        StaticGuiElement button;
        if (materialConfig.vaultEnabled) {
            double balance = VaultHook.look(player);
            button = new StaticGuiElement(buttonConfig.slot,
                createButtonItem(buttonConfig.material, buttonConfig.name, buttonConfig.lore,
                        new HashMap<>() {{
                            put("{cost}", String.valueOf(materialConfig.vaultCost));
                            put("{uses}", String.valueOf(materialConfig.vaultUses));
                            put("{balance}", String.format("%.2f", balance));
                        }}),
                click -> {
                    handleVaultCharge(click, player, materialConfig);
                    refreshGUIWithItems(gui, click.getRawEvent().getInventory(), player);
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
        
        ChargeConfig.ChargeGUIConfig.ButtonConfig buttonConfig = guiConfig.buttons.get("p");
        if (buttonConfig == null) return;

        StaticGuiElement button;
        if (materialConfig.pointsEnabled && plugin.getServer().getPluginManager().getPlugin("PlayerPoints") != null) {
            int points = PlayerPointsHook.look(player);
            button = new StaticGuiElement(buttonConfig.slot,
                createButtonItem(buttonConfig.material, buttonConfig.name, buttonConfig.lore,
                        new HashMap<>() {{
                            put("{cost}", String.valueOf(materialConfig.pointsCost));
                            put("{uses}", String.valueOf(materialConfig.pointsUses));
                            put("{points}", String.valueOf(points));
                        }}),
                click -> {
                    handlePointsCharge(click, player, materialConfig);
                    refreshGUIWithItems(gui, click.getRawEvent().getInventory(), player);
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
        
        ChargeConfig.ChargeGUIConfig.ButtonConfig buttonConfig = guiConfig.buttons.get("c");
        if (buttonConfig == null) return;

        StaticGuiElement button;
        if (materialConfig.nbtEnabled && !materialConfig.nbtItems.isEmpty()) {
            Map<String, String> loreReplacements = new HashMap<>();

            List<String> descriptionList = new ArrayList<>();
            List<String> costList = new ArrayList<>();
            List<String> usesList = new ArrayList<>();
            for (ChargeConfig.NBTItemConfig nbtItem : materialConfig.nbtItems) {
                descriptionList.add(nbtItem.description);
                costList.add(String.valueOf(nbtItem.cost));
                usesList.add(String.valueOf(nbtItem.uses));
            }
            loreReplacements.put("{item}", String.join("/", descriptionList));
            loreReplacements.put("{cost}", String.join("/", costList));
            loreReplacements.put("{uses}", String.join("/", usesList));

            button = new StaticGuiElement(buttonConfig.slot,
                createButtonItem(buttonConfig.material, buttonConfig.name, buttonConfig.lore, loreReplacements),
                click -> {
                    handleItemCharge(click, player, materialConfig);
                    refreshGUIWithItems(gui, click.getRawEvent().getInventory(), player);
                    return true;
                });
        } else { // 未配置 == 禁用
            String materialName = buttonConfig.disabledMaterial;
            String name = buttonConfig.disabledName;
            List<String> lore = buttonConfig.disabledLore;

            button = new StaticGuiElement(buttonConfig.slot,
                createButtonItem(materialName, name, lore, new HashMap<>()),
                click -> true);
        }

        gui.addElement(button);
    }

    private void addCloseButton(InventoryGui gui, Player player, ChargeConfig.ChargeGUIConfig guiConfig) {
        ChargeConfig.ChargeGUIConfig.ButtonConfig buttonConfig = guiConfig.buttons.get("x");
        if (buttonConfig == null) return;

        StaticGuiElement button = new StaticGuiElement(buttonConfig.slot,
            createButtonItem(buttonConfig.material, buttonConfig.name, buttonConfig.lore, new HashMap<>()),
            click -> {
                returnItem2Player(click.getRawEvent().getInventory(), player);
                gui.close(player);
                return true;
            });

        gui.addElement(button);
    }

    private void addStainedButton(InventoryGui gui, ChargeConfig.ChargeGUIConfig guiConfig) {
        for (String key : Arrays.asList(">", "<", "^")) {
            ChargeConfig.ChargeGUIConfig.ButtonConfig buttonConfig = guiConfig.buttons.get(key);
            if (buttonConfig != null) {
                StaticGuiElement button = new StaticGuiElement(buttonConfig.slot,
                        createButtonItem(buttonConfig.material, buttonConfig.name, buttonConfig.lore, new HashMap<>()),
                        click -> true);

                gui.addElement(button);
            }
        }
    }

    // 点击特定的几个charge按钮才会触发，此时Inventory必定为菜单
    private boolean checkMagicBlock(GuiElement.Click click, Player player) {
        ItemStack magicBlock = click.getRawEvent().getInventory().getContents()[4];
        if (!blockManager.isMagicBlock(magicBlock)) {
            plugin.sendMessage(player, "messages.charge-invalid-item");
            return false;
        }
        if (blockManager.getMaxUseTimes(magicBlock) == AbstractMagicItem.INFINITE_USES) {
            plugin.sendMessage(player, "messages.charge-infinite-uses");
            return false;
        }
        return true;
    }

    private void addUses(GuiElement.Click click, int addedUses) {
        ItemStack magicBlock = click.getRawEvent().getInventory().getContents()[4];
        blockManager.addUseTimes(magicBlock, addedUses);
    }

    private void handleVaultCharge(GuiElement.Click click, Player player, ChargeConfig.MaterialChargeConfig materialConfig) {
        if (!checkMagicBlock(click, player)) {
            return;
        }

        double balance = VaultHook.look(player);
        if (balance < materialConfig.vaultCost) {
            plugin.sendMessage(player, "messages.charge-no-money",
                String.format("%.2f", materialConfig.vaultCost),
                String.format("%.2f", balance));
            return;
        }

        if (VaultHook.take(materialConfig.vaultCost, player)) {
            int addedUses = materialConfig.vaultUses;
            addUses(click, addedUses);
            plugin.sendMessage(player, "messages.charge-success-vault",
                String.format("%.2f", materialConfig.vaultCost),
                String.valueOf(addedUses));
        }
    }

    private void handlePointsCharge(GuiElement.Click click, Player player, ChargeConfig.MaterialChargeConfig materialConfig) {
        if (!checkMagicBlock(click, player)) {
            return;
        }

        int points = PlayerPointsHook.look(player);
        if (points < materialConfig.pointsCost) {
            plugin.sendMessage(player, "messages.charge-no-points", 
                String.valueOf(materialConfig.pointsCost), 
                String.valueOf(points));
            return;
        }

        if (PlayerPointsHook.take(materialConfig.pointsCost, player)) {
            int addedUses = materialConfig.pointsUses;
            addUses(click, addedUses);
            plugin.sendMessage(player, "messages.charge-success-points", 
                String.valueOf(materialConfig.pointsCost), 
                String.valueOf(addedUses));
        }
    }

    private void handleItemCharge(GuiElement.Click click, Player player, ChargeConfig.MaterialChargeConfig materialConfig) {
        if (!checkMagicBlock(click, player)) {
            return;
        }

        // 待充能物品
        ItemStack chargeItem = click.getRawEvent().getInventory().getContents()[4];
        // 用于充能的物品
        ItemStack chargeNeedItem = click.getRawEvent().getInventory().getContents()[15];

        if (chargeItem == null || chargeItem.getType() == Material.AIR || !blockManager.isMagicBlock(chargeItem)) {
            plugin.sendMessage(player, "messages.charge-please-place-item");
            return;
        }

        ChargeConfig.NBTItemConfig validConfig = getValidItemConfig(chargeNeedItem, materialConfig.nbtItems);
        if (validConfig == null) {
            plugin.sendMessage(player, "messages.charge-need-invalid-item");
            return;
        }

        int requiredAmount = validConfig.cost;
        int availableAmount = chargeNeedItem.getAmount();
        if (availableAmount < requiredAmount) {
            plugin.sendMessage(player, "messages.charge-insufficient-items", 
                String.valueOf(requiredAmount), 
                String.valueOf(availableAmount));
            return;
        }

        // 扣除物品
        chargeNeedItem.setAmount(availableAmount - requiredAmount);

        int addedUses = validConfig.uses;
        addUses(click, addedUses);
        plugin.sendMessage(player, "messages.charge-success-item", 
            String.valueOf(requiredAmount), 
            chargeItem.getType().name(), 
            String.valueOf(addedUses));
    }

    private ChargeConfig.NBTItemConfig getValidItemConfig(ItemStack item, List<ChargeConfig.NBTItemConfig> nbtItems) {
        if (item == null) return null;
        
        for (ChargeConfig.NBTItemConfig config : nbtItems) {
            if (item.getType() == config.material) {
                ItemMeta meta = item.getItemMeta();
                // 检测 NBTKEY
                if (config.nbtKey == null || config.nbtKey.isBlank()) {
                    return config;
                } else {
                    if (meta == null) {
                        continue;
                    }
                    // 存在 key 即可
                    if (meta.getPersistentDataContainer()
                            .has(new NamespacedKey(plugin, config.nbtKey), PersistentDataType.STRING)) {
                        return config;
                    }
                }
            }
        }
        return null;
    }

    public void reload() {
        chargeConfig.reload();
    }

    private ItemStack createButtonItem(String materialName, String name, List<String> lore, Map<String, String> replacements) {
        Material material;
        try {
            material = Material.valueOf(materialName);
        } catch (IllegalArgumentException e) {
            material = Material.BARRIER;
            e.printStackTrace();
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));

            if (lore != null && !lore.isEmpty()) {
                List<String> processedLore = new ArrayList<>();
                for (String line : lore) {
                    processedLore.add(ChatColor.translateAlternateColorCodes('&', LoreUtil.replaceVariables(line, replacements)));
                }
                meta.setLore(processedLore);
            }

            item.setItemMeta(meta);
        }
        return item;
    }

    private void returnItem2Player(Inventory inv, Player player) {
        ItemStack magicBlock = inv.getContents()[4];
        ItemStack needMaterial = inv.getContents()[15];

        for (ItemStack item : Arrays.asList(magicBlock, needMaterial)) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            HashMap<Integer, ItemStack> remainMap = player.getInventory().addItem(item);
            if (!remainMap.isEmpty()) {
                for (ItemStack remainItem : remainMap.values()) {
                    player.getWorld().dropItemNaturally(player.getLocation(), remainItem);
                }
            }
            inv.remove(item);
        }
    }

    private void refreshGUIWithItems(InventoryGui gui, Inventory inv, Player player) {
        ItemStack magicBlock = inv.getContents()[4];
        ItemStack needMaterial = inv.getContents()[15];
        updateGUI(gui, player, magicBlock, needMaterial, false);
    }

}