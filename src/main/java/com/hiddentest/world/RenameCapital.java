package com.hiddentest.world;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RenameCapital implements Listener {

    @EventHandler
    public void onAnvilRename(PrepareAnvilEvent event) {

        ItemStack result = event.getResult();
        if (result == null) return;

        ItemMeta meta = result.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String inputName = ChatColor.stripColor(meta.getDisplayName());

        for (OfflinePlayer player : Bukkit.getWhitelistedPlayers()) {

            String realName = player.getName();
            if (realName == null) continue;

            if (realName.equalsIgnoreCase(inputName)) {

                // ✅ Clone result to prevent overwrite issues
                ItemStack newResult = result.clone();
                ItemMeta newMeta = newResult.getItemMeta();

                if (newMeta != null) {
                    newMeta.setDisplayName(realName);
                    newResult.setItemMeta(newMeta);
                }

                event.setResult(newResult);
                return;
            }
        }
    }
}
