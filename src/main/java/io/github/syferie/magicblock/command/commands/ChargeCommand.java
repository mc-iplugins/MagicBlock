package io.github.syferie.magicblock.command.commands;

import io.github.syferie.magicblock.MagicBlockPlugin;
import io.github.syferie.magicblock.command.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChargeCommand implements ICommand {

    private final MagicBlockPlugin plugin;

    public ChargeCommand(MagicBlockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.sendMessage(sender, "commands.console-only-error");
            return;
        }

        plugin.getChargeGUI().openGUI(player);
    }

    @Override
    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(getPermissionNode());
    }

    @Override
    public String getPermissionNode() {
        return "magicblock.use";
    }

    @Override
    public String getUsage() {
        return "/mb charge";
    }

    @Override
    public String getDescription() {
        return "打开充能菜单";
    }

}
