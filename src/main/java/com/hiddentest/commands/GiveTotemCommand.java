package com.hiddentest.commands;

import com.hiddentest.items.UnknownTotem;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UnknownTotemCommand implements CommandExecutor {

    @Override
    public boolean onCommand(
            CommandSender sender,
            Command command,
            String label,
            String[] args
    ) {

        if (args.length != 1) {

            sender.sendMessage(
                    ChatColor.RED +
                    "Usage: /givetotem <player>"
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
                UnknownTotem.createUnknownTotem()
        );

        sender.sendMessage(
                ChatColor.GREEN +
                "Gave Unknown Totem to " +
                target.getName()
        );

        target.sendMessage(
                ChatColor.GOLD +
                "You received an Unknown Totem."
        );

        return true;
    }
}
