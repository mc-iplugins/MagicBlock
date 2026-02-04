package io.github.syferie.magicblock.config;

import io.github.syferie.magicblock.MagicBlockPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChargeConfig {
    private final MagicBlockPlugin plugin;
    private final Map<Material, MaterialChargeConfig> materialConfigs = new HashMap<>();
    private MaterialChargeConfig defaultConfig;
    private ChargeGUIConfig guiConfig;

    public ChargeConfig(MagicBlockPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        ConfigurationSection chargeSection = plugin.getConfig().getConfigurationSection("charge-settings");
        if (chargeSection == null) {
            plugin.getLogger().warning("充能配置未找到，使用默认值");
            loadDefaultConfig();
            return;
        }

        // 加载默认配置
        defaultConfig = loadMaterialChargeConfig(chargeSection.getConfigurationSection("default"), true);

        // 加载特定材料配置
        ConfigurationSection materialsSection = chargeSection.getConfigurationSection("materials");
        if (materialsSection != null) {
            for (String materialName : materialsSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(materialName);
                    MaterialChargeConfig config = loadMaterialChargeConfig(materialsSection.getConfigurationSection(materialName), false);
                    if (config != null) {
                        materialConfigs.put(material, config);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的材料名称: " + materialName);
                }
            }
        }

        // 加载GUI配置
        ConfigurationSection guiSection = plugin.getConfig().getConfigurationSection("charge-gui");
        if (guiSection != null) {
            guiConfig = new ChargeGUIConfig(guiSection);
        } else {
            guiConfig = new ChargeGUIConfig();
        }

        plugin.getLogger().info("已加载 " + materialConfigs.size() + " 个材料的充能配置");
    }

    private MaterialChargeConfig loadMaterialChargeConfig(ConfigurationSection section, boolean isDefault) {
        if (section == null) {
            return isDefault ? new MaterialChargeConfig() : null;
        }

        MaterialChargeConfig config = new MaterialChargeConfig();

        // 加载Vault配置
        ConfigurationSection vaultSection = section.getConfigurationSection("vault");
        if (vaultSection != null) {
            config.vaultEnabled = vaultSection.getBoolean("enabled", isDefault);
            config.vaultCost = vaultSection.getDouble("cost", 1);
            config.vaultUses = vaultSection.getInt("uses", 1);
        } else {
            config.vaultEnabled = isDefault;
            config.vaultCost = 100.0;
            config.vaultUses = 10;
        }

        // 加载PlayerPoints配置
        ConfigurationSection pointsSection = section.getConfigurationSection("playerpoints");
        if (pointsSection != null) {
            config.pointsEnabled = pointsSection.getBoolean("enabled", isDefault);
            config.pointsCost = pointsSection.getInt("cost", 1);
            config.pointsUses = pointsSection.getInt("uses", 1);
        } else {
            config.pointsEnabled = isDefault;
            config.pointsCost = 50;
            config.pointsUses = 10;
        }

        // 加载NBT物品配置
        ConfigurationSection nbtSection = section.getConfigurationSection("nbt-items");
        if (nbtSection != null) {
            config.nbtEnabled = nbtSection.getBoolean("enabled", false);
            config.nbtItems = loadNBTItems(nbtSection.getConfigurationSection("items"));
        } else {
            config.nbtEnabled = false;
            config.nbtItems = new ArrayList<>();
        }

        return config;
    }

    private List<NBTItemConfig> loadNBTItems(ConfigurationSection section) {
        List<NBTItemConfig> items = new ArrayList<>();
        if (section == null) return items;

        for (String key : section.getKeys(false)) {
            ConfigurationSection itemSection = section.getConfigurationSection(key);
            if (itemSection != null) {
                try {
                    NBTItemConfig item = new NBTItemConfig();
                    item.material = Material.valueOf(itemSection.getString("material"));
                    item.nbtKey = itemSection.getString("nbt-key");
                    item.nbtValue = itemSection.getString("nbt-value");
                    item.cost = itemSection.getInt("cost", 1);
                    item.uses = itemSection.getInt("uses", 1);
                    items.add(item);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的NBT物品材料: " + itemSection.getString("material"));
                }
            }
        }

        return items;
    }

    private void loadDefaultConfig() {
        defaultConfig = new MaterialChargeConfig();
        guiConfig = new ChargeGUIConfig();
    }

    public MaterialChargeConfig getChargeConfig(Material material) {
        return materialConfigs.getOrDefault(material, defaultConfig);
    }

    public ChargeGUIConfig getGuiConfig() {
        return guiConfig;
    }

    public void reload() {
        materialConfigs.clear();
        loadConfig();
    }

    public static class MaterialChargeConfig {
        public boolean vaultEnabled;
        public double vaultCost;
        public int vaultUses;

        public boolean pointsEnabled;
        public int pointsCost;
        public int pointsUses;

        public boolean nbtEnabled;
        public List<NBTItemConfig> nbtItems;

        public MaterialChargeConfig() {
            this.vaultEnabled = true;
            this.vaultCost = 100.0;
            this.vaultUses = 10;
            this.pointsEnabled = true;
            this.pointsCost = 50;
            this.pointsUses = 10;
            this.nbtEnabled = false;
            this.nbtItems = new ArrayList<>();
        }
    }

    public static class NBTItemConfig {
        public Material material;
        public String nbtKey;
        public String nbtValue;
        public int cost;
        public int uses;
    }

    public static class ChargeGUIConfig {
        public String title;
        public String[] rows;
        public Map<String, ButtonConfig> buttons;

        public ChargeGUIConfig() {
            this.title = "&8⚡ &b方块充能";
            this.rows = new String[]{
                    "         ",
                    "         ",
                    "         "
            };
            this.buttons = new HashMap<>();
            loadDefaultButtons();
        }

        public ChargeGUIConfig(ConfigurationSection section) {
            this.title = section.getString("title", "&8⚡ &b方块充能");
            this.rows = section.getStringList("rows").toArray(new String[0]);
            this.buttons = new HashMap<>();
            loadButtons(section.getConfigurationSection("buttons"));
        }

        private void loadDefaultButtons() {
            // 默认按钮配置
            buttons.put("vault-charge", new ButtonConfig('v', "GOLD_INGOT"));
            buttons.put("points-charge", new ButtonConfig('p', "EMERALD"));
            buttons.put("item-charge", new ButtonConfig('c', "NETHER_STAR"));
            buttons.put("close", new ButtonConfig('x', "BARRIER"));
            buttons.put("item-slot", new ButtonConfig('s', "AIR"));
        }

        private void loadButtons(ConfigurationSection section) {
            if (section == null) {
                loadDefaultButtons();
                return;
            }

            for (String buttonKey : section.getKeys(false)) {
                ConfigurationSection buttonSection = section.getConfigurationSection(buttonKey);
                if (buttonSection != null) {
                    ButtonConfig config = new ButtonConfig();
                    config.slot = buttonSection.getString("slot", " ").charAt(0);
                    config.material = buttonSection.getString("material", "STONE");
                    config.name = buttonSection.getString("name", "");
                    config.lore = buttonSection.getStringList("lore");
                    buttons.put(buttonKey, config);

                    // 加载禁用状态配置
                    ConfigurationSection disabledSection = buttonSection.getConfigurationSection("disabled");
                    if (disabledSection != null) {
                        config.disabledMaterial = disabledSection.getString("material", "GRAY_DYE");
                        config.disabledName = disabledSection.getString("name", "&8禁用");
                        config.disabledLore = disabledSection.getStringList("lore");
                    }

                    // 加载无配置状态配置
                    ConfigurationSection noConfigSection = buttonSection.getConfigurationSection("no-config");
                    if (noConfigSection != null) {
                        config.noConfigMaterial = noConfigSection.getString("material", "BARRIER");
                        config.noConfigName = noConfigSection.getString("name", "&c未配置");
                        config.noConfigLore = noConfigSection.getStringList("lore");
                    }
                }
            }
        }

        public static class ButtonConfig {
            public char slot;
            public String material;
            public String name;
            public List<String> lore;
            public String disabledMaterial;
            public String disabledName;
            public List<String> disabledLore;
            public String noConfigMaterial;
            public String noConfigName;
            public List<String> noConfigLore;

            public ButtonConfig() {}

            public ButtonConfig(char slot, String material) {
                this.slot = slot;
                this.material = material;
                this.name = "";
                this.lore = new ArrayList<>();
            }
        }
    }
}