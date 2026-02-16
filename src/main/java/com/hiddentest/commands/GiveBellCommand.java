package com.hiddentest.commands;

import com.hiddentest.BellOfTruth;
import com.hiddentest.HiddenTest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveBellCommand implements CommandExecutor {

    private final HiddenTest plugin;

    public GiveBellCommand(HiddenTest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("hiddentest.givebell")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /givebell <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        BellOfTruth bell = new BellOfTruth(plugin);
        target.getInventory().addItem(bell.createBell());

        sender.sendMessage(ChatColor.GREEN + "Gave Bell of Truth to " + target.getName());
        target.sendMessage(ChatColor.GOLD + "You received a Bell of Truth.");

        return true;
    }
}
