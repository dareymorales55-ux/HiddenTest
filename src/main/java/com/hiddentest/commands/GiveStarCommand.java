package com.hiddentest.commands;

import com.hiddentest.items.ShadowStar;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveStarCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        player.getInventory().addItem(ShadowStar.createShadowStar());
        player.sendMessage(ChatColor.GRAY + "You received a Shadow Star.");

        return true;
    }
}
