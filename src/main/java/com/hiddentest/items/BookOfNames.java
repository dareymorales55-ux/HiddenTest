package com.hiddentest.items;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;

public class BookOfNames implements CommandExecutor {

    public static ItemStack createBook() {

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        meta.setTitle(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Book of Names");
        meta.setAuthor("anonymous");

        String page1 =
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + ChatColor.UNDERLINE +
                        "Book of Names\n\n" +
                ChatColor.BLACK +
                        "- Amanjamin\n" +
                        "- vlgu\n" +
                        "- YTOnyxx\n" +
                        "- BTLXMAS\n" +
                        "- CNK_Frags\n" +
                        "- Astrosss_\n" +
                        "- Darkisbadwew\n" +
                        "- _DeejayAlt_\n" +
                        "- CoolFighter_\n" +
                        "- XfadezX\n" +
                        "- Jaampss\n" +
                        "- kieranifm\n" +
                        "- NorSpear\n" +
                        "- Bladescape\n" +
                        "- Reddogs_MC\n" +
                        "- Shadowbanning\n" +
                        "- Vashblade_\n" +
                        "- r0gue8";

        meta.setPages(Arrays.asList(page1));

        book.setItemMeta(meta);
        return book;
    }

    // =========================
    // COMMAND
    // =========================

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        player.getInventory().addItem(createBook());

        player.sendMessage(
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD +
                        "Book of Names given."
        );

        return true;
    }
}
