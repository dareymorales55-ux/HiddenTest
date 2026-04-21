package com.hiddentest.items;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

public class BookOfNames implements CommandExecutor {

    public static ItemStack createBook() {

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();

        if (meta == null) return book;

        // ✅ Title + Author
        meta.setTitle(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Player Names");
        meta.setAuthor("Anonymous");

        List<BaseComponent[]> pages = new ArrayList<>();
        List<TextComponent> currentPage = new ArrayList<>();

        // Header
        TextComponent header = new TextComponent("Player Names\n\n");
        header.setColor(net.md_5.bungee.api.ChatColor.LIGHT_PURPLE);
        header.setBold(true);
        header.setUnderlined(true);

        TextComponent instruction = new TextComponent("Click a name to copy\n\n");
        instruction.setColor(net.md_5.bungee.api.ChatColor.GRAY);

        currentPage.add(header);
        currentPage.add(instruction);

        int lineCount = 3;

        // ✅ GET + SORT WHITELIST
        List<String> names = new ArrayList<>();

        for (OfflinePlayer p : Bukkit.getWhitelistedPlayers()) {
            if (p.getName() != null) {
                names.add(p.getName()); // keeps real capitalization
            }
        }

        // Alphabetical (case-insensitive, keeps original caps)
        names.sort(String.CASE_INSENSITIVE_ORDER);

        // ✅ BUILD BOOK
        for (String name : names) {

            TextComponent line = new TextComponent("- " + name + "\n");
            line.setColor(net.md_5.bungee.api.ChatColor.BLACK);

            // Click to copy
            line.setClickEvent(new ClickEvent(
                    ClickEvent.Action.COPY_TO_CLIPBOARD,
                    name
            ));

            // Hover
            line.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new BaseComponent[]{
                            new TextComponent("Click to copy player name")
                    }
            ));

            currentPage.add(line);
            lineCount++;

            if (lineCount >= 13) {
                pages.add(currentPage.toArray(new BaseComponent[0]));
                currentPage = new ArrayList<>();
                lineCount = 0;
            }
        }

        if (!currentPage.isEmpty()) {
            pages.add(currentPage.toArray(new BaseComponent[0]));
        }

        meta.spigot().setPages(pages);
        book.setItemMeta(meta);

        return book;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        player.getInventory().addItem(createBook());

        player.sendMessage(
                ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD +
                        "Player Names book given."
        );

        return true;
    }
}
