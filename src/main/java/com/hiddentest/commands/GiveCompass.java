package com.hiddentest.commands;

import com.hiddentest.items.DetectivesCompass;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GiveCompassCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("hiddentest.givecompass")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /givecompass <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        ItemStack compass = DetectivesCompass.createDetectivesCompass();
        target.getInventory().addItem(compass);

        sender.sendMessage(ChatColor.GREEN + "Gave Detective’s Compass to " + target.getName());
        target.sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "You received a Detective’s Compass!");

        return true;
    }
}
