package com.ashkiano.customizablesilktouch.command;

import com.ashkiano.customizablesilktouch.CustomizableSilkTouch;
import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

@RequiredArgsConstructor
public class CSTCommand implements CommandExecutor {

    private final CustomizableSilkTouch plugin;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customizablesilktouch.admin")) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("Messages.No-Permission")));
            return true;
        }

        plugin.reload();
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("Messages.Successfully-Reloaded")));
        return true;
    }

}
