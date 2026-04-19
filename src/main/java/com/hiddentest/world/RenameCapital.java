package com.hiddentest.world;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Set;

public class RenameCapital implements Listener {

    @EventHandler
    public void onAnvilRename(PrepareAnvilEvent event) {

        ItemStack result = event.getResult();
        if (result == null) return;

        ItemMeta meta = result.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String inputName = ChatColor.stripColor(meta.getDisplayName());

        // ✅ Check WHITELIST instead of tablist
        Set<OfflinePlayer> whitelist = Bukkit.getWhitelistedPlayers();

        for (OfflinePlayer player : whitelist) {

            String realName = player.getName();
            if (realName == null) continue;

            if (realName.equalsIgnoreCase(inputName)) {

                // Fix capitalization
                meta.setDisplayName(realName);
                result.setItemMeta(meta);

                event.setResult(result);
                return;
            }
        }
    }
}
