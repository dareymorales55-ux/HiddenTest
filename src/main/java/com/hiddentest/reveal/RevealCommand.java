package com.hiddentest.reveal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RevealCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender,
                             Command command,
                             String label,
                             String[] args) {

        if (args.length < 1 || args.length > 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <player> [seconds]");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        // /reveal
        if (label.equalsIgnoreCase("reveal")) {

            if (args.length == 2) {
                try {
                    long seconds = Long.parseLong(args[1]);
                    long millis = seconds * 1000L;

                    RevealManager.reveal(target, millis);

                    sender.sendMessage(ChatColor.GREEN +
                            "Revealed " + target.getName() +
                            " for " + seconds + " seconds.");

                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid time.");
                }

            } else {
                // Infinite reveal
                RevealManager.reveal(target);
                sender.sendMessage(ChatColor.GREEN +
                        "Revealed " + target.getName() + " indefinitely.");
            }
        }

        // /hide
        if (label.equalsIgnoreCase("hide")) {

            RevealManager.hide(target);

            sender.sendMessage(ChatColor.YELLOW +
                    "Hidden " + target.getName() + ".");
        }

        return true;
    }
}
