package io.github.syferie.magicblock.block;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.api.IMagicBlock;
import io.github.syferie.magicblock.core.AbstractMagicItem;
import io.github.syferie.magicblock.util.LoreUtil;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 方块管理器
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class BlockManager extends AbstractMagicItem implements IMagicBlock {

    /**
     * 构造函数
     *
     * @param plugin 插件实例
     */
    public BlockManager(MagicBlockPlugin plugin) {
        super(plugin, "magicblock"); // keyPrefix: magicblock_times, magicblock_maxtimes
    }

    // ==================== 方块识别 ====================

    /**
     * 创建魔法方块
     *
     * @param material 方块材质
     * @param useTimes 使用次数 (-1 表示无限)
     * @return 创建的魔法方块
     */
    public ItemStack createMagicBlock(Material material, int useTimes) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // 1. 设置显示名称
        String blockName = plugin.getMinecraftLangManager().getItemStackName(item);
        String nameFormat = plugin.getConfig().getString("display.block-name-format", "&b✦ %s &b✦");
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&',
            String.format(nameFormat, blockName)));

        // 2. 初始化 Lore 列表，添加魔法标识
        ArrayList<String> lore = new ArrayList<>();
        lore.add(plugin.getMagicLore());

        // 3. 添加装饰性 lore（如果启用）
        if (plugin.getConfig().getBoolean("display.decorative-lore.enabled", true)) {
            List<String> decorativeLore = plugin.getConfig().getStringList("display.decorative-lore.lines");
            for (String line : decorativeLore) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
        }

        meta.setLore(lore);

        // 4. 添加附魔效果（隐藏）
        meta.addEnchant(Enchantment.DURABILITY, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // 5. 生成唯一ID
        plugin.ensureBlockHasId(meta);

        item.setItemMeta(meta);

        // 6. 设置使用次数（这会更新 PDC 数据）
        setUseTimes(item, useTimes);

        // 7. 更新完整的 Lore（包含使用次数显示）
        updateLore(item, useTimes == -1 ? Integer.MAX_VALUE - 100 : useTimes);

        return item;
    }

    /**
     * 检查是否是魔法方块
     *
     * @param item 物品
     * @return 如果是魔法方块返回true
     */
    public boolean isMagicBlock(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }
        return plugin.hasMagicLore(item.getItemMeta());
    }

    @Override
    public boolean isMagicItem(ItemStack item) {
        return isMagicBlock(item);
    }

    @Override
    public String getMagicItemType() {
        return "BLOCK";
    }

    // ==================== 方块绑定系统 ====================

    /**
     * 检查方块是否已绑定
     *
     * @param item 物品
     * @return 如果已绑定返回true
     */
    public boolean isBlockBound(ItemStack item) {
        return plugin.getBlockBindManager().isBlockBound(item);
    }

    @Override
    protected UUID getBoundPlayer(ItemStack item) {
        return plugin.getBlockBindManager().getBoundPlayer(item);
    }

    // ==================== 方块特有功能 ====================

    /**
     * 设置无限使用 (方块特有方法)
     *
     * @param item 物品
     */
    public void setInfiniteUse(ItemStack item) {
        setUseTimes(item, -1);
    }

    // ==================== 模板方法实现 ====================

    @Override
    protected String getMagicLoreIdentifier() {
        return plugin.getMagicLore();
    }

    @Override
    protected List<String> getDecorativeLore(ItemStack item, Player owner) {
        List<String> configLore = plugin.getConfig()
            .getStringList("display.decorative-lore.lines");
        return LoreUtil.processDecorativeLoreList(configLore, owner);
    }

    @Override
    protected String getUsageLorePrefix() {
        return plugin.getUsageLorePrefix();
    }

    @Override
    protected boolean shouldShowBinding() {
        return true; // 方块需要显示绑定信息
    }

    // ==================== 使用次数减少的特殊处理 ====================

    /**
     * 重写 decrementUseTimes 以添加方块特有的绑定数据更新逻辑
     *
     * 优化说明:
     * - 移除异步绑定数据更新（绑定数据在bindBlock时已保存）
     * - 减少不必要的方法调用
     */
    @Override
    public int decrementUseTimes(ItemStack item) {
        // 调用基类方法处理通用逻辑（包含updateLore）
        return super.decrementUseTimes(item);
    }
}
