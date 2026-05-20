package com.hiddentest.commands;

import com.hiddentest.items.BloodCompass;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveBloodCompassCommand implements CommandExecutor {

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (!sender.hasPermission("hiddentest.givebloodcompass")) {

            sender.sendMessage(
                    ChatColor.RED +
                    "You do not have permission."
            );

            return true;
        }

        if (args.length != 1) {

            sender.sendMessage(
                    ChatColor.RED +
                    "Usage: /givebloodcompass <player>"
            );

            return true;
        }

        Player target =
                Bukkit.getPlayer(args[0]);

        if (target == null) {

            sender.sendMessage(
                    ChatColor.RED +
                    "Player not found."
            );

            return true;
        }

        target.getInventory().addItem(
                BloodCompass.createBloodCompass()
        );

        sender.sendMessage(
                ChatColor.GREEN +
                "Gave Blood Compass to " +
                target.getName()
        );

        target.sendMessage(
                ChatColor.DARK_RED +
                "You received a Blood Compass."
        );

        return true;
    }
}
