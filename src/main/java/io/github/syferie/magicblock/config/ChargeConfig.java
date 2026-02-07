package io.github.syferie.magicblock.config;

import com.google.common.base.Function;
import io.github.syferie.magicblock.MagicBlockPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChargeConfig {
    private final MagicBlockPlugin plugin;
    private final List<MaterialChargeConfig> materialConfigs = new ArrayList<>();
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
            for (String sectionKey : materialsSection.getKeys(false)) {
                try {
                    MaterialChargeConfig config = loadMaterialChargeConfig(materialsSection.getConfigurationSection(sectionKey), false);
                    if (config != null) {
                        materialConfigs.add(config);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("加载自定义充能配置节点失败: " + sectionKey);
                    e.printStackTrace();
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

        // 加载材料判断器
        ConfigurationSection materialSection = section.getConfigurationSection("material");
        if (materialSection == null) {
            if (isDefault) { // 默认配置不检查材质
                config.materialCheck = m -> true;
            } else {
                config.materialCheck = m -> true;
            }
        } else {
            List<String> containsList = materialSection.getStringList("contains");
            List<String> prefixList = materialSection.getStringList("prefix");
            List<String> suffixList = materialSection.getStringList("suffix");
            Set<String> exactList = new HashSet<>(materialSection.getStringList("exact"));
            config.materialCheck = m -> {
                String name = m.name().toUpperCase();
                for (String contains : containsList) {
                    if (name.contains(contains)) {
                        return true;
                    }
                }
                for (String prefix : prefixList) {
                    if (name.startsWith(prefix)) {
                        return true;
                    }
                }
                for (String suffix : suffixList) {
                    if (name.endsWith(suffix)) {
                        return true;
                    }
                }
                return exactList.contains(name);
            };
        }

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
            config.nbtItems = loadNBTItems(nbtSection.getMapList("items"));
        } else {
            config.nbtEnabled = false;
            config.nbtItems = new ArrayList<>();
        }

        return config;
    }

    private List<NBTItemConfig> loadNBTItems(List<Map<?, ?>> sectionList) {
        List<NBTItemConfig> items = new ArrayList<>();
        if (sectionList == null || sectionList.isEmpty()) return items;

        for (Map<?, ?> section : sectionList) {
            try {
                NBTItemConfig item = new NBTItemConfig();
                item.material = Material.valueOf((String) section.get("material"));
                item.nbtKey = (String) section.get("nbt-key");
                item.description = (String) section.get("description");
                item.cost = (Integer) section.get("cost");
                item.uses = (Integer) section.get("uses");
                items.add(item);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的NBT物品材料: " + section.get("material"));
            }
        }

        return items;
    }

    private void loadDefaultConfig() {
        defaultConfig = new MaterialChargeConfig();
        guiConfig = new ChargeGUIConfig();
    }

    public MaterialChargeConfig getChargeConfig(@Nullable Material material) {
        if (material == null) {
            return defaultConfig;
        }
        for (MaterialChargeConfig config : materialConfigs) {
            if (config.materialCheck.apply(material)) {
                return config;
            }
        }
        return defaultConfig;
    }

    public ChargeGUIConfig getGuiConfig() {
        return guiConfig;
    }

    public void reload() {
        materialConfigs.clear();
        loadConfig();
    }

    public static class MaterialChargeConfig {
        public Function<@NotNull Material, Boolean> materialCheck;

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
        public String description;
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
                    "vp  x   c"
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
            buttons.put("v", new ButtonConfig('v', "GOLD_INGOT"));
            buttons.put("p", new ButtonConfig('p', "EMERALD"));
            buttons.put("c", new ButtonConfig('c', "NETHER_STAR"));
            buttons.put("x", new ButtonConfig('x', "BARRIER"));
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