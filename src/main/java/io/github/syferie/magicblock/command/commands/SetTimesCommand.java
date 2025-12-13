package io.github.syferie.magicblock.command.commands;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.command.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

/**
 * SetTimes 命令 - 设置手持魔法方块的使用次数
 *
 * 用法: /mb settimes <times>
 * 权限: magicblock.settimes
 *
 * @author MagicBlock Team
 * @version 2.0
 */
public class SetTimesCommand implements ICommand {

    private final MagicBlockPlugin plugin;

    public SetTimesCommand(MagicBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "commands.console-only-error");
            return;
        }

        if (args.length < 1) {
            sender.sendMessage(getUsage());
            return;
        }

        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();

        // 检查是否是魔法方块
        if (!plugin.getBlockManager().isMagicBlock(item)) {
            plugin.sendMessage(player, "commands.settimes.must-hold");
            return;
        }

        // 解析使用次数
        int times;
        try {
            times = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            plugin.sendMessage(player, "commands.settimes.invalid-number");
            return;
        }

        // 设置使用次数
        plugin.getBlockManager().setUseTimes(item, times);
        // 更新 Lore 显示
        plugin.getBlockManager().updateLore(item, times);

        // 根据是否无限次数选择消息
        if (times == -1 || times >= Integer.MAX_VALUE - 100) {
            plugin.sendMessage(player, "commands.settimes.success-infinite");
        } else {
            plugin.sendMessage(player, "commands.settimes.success", times);
        }
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermissionNode());
    }

    @Override
    public String getPermissionNode() {
        return "magicblock.settimes";
    }

    @Override
    public String getUsage() {
        return "/mb settimes <times>";
    }

    @Override
    public String getDescription() {
        return plugin.getMessage("commands.settimes-description");
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return List.of("10", "50", "100", "500", "1000", "-1");
        }
        return Collections.emptyList();
    }
}
