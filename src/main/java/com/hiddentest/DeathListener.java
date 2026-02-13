package com.hiddentest;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Date;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon == null || !weapon.hasItemMeta()) return;

        ItemMeta meta = weapon.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;

        String weaponName = ChatColor.stripColor(meta.getDisplayName());

        // Check if weapon name matches killer's EXACT username
        if (!weaponName.equals(killer.getName())) return;

        // Get victim real name from stored profile
        String realName = victim.getPlayerProfile().getName();

        // Send messages in order
        Bukkit.broadcastMessage(ChatColor.RED + realName + " has been caught.");
        Bukkit.broadcastMessage(ChatColor.YELLOW + realName + " left the game");

        // Ban victim permanently
        Bukkit.getBanList(BanList.Type.NAME).addBan(
                victim.getName(),
                ChatColor.DARK_RED + "Your cover was blown.",
                null,
                null
        );

        // Kick immediately so it looks clean
        victim.kickPlayer(ChatColor.DARK_RED + "Your cover was blown.");
    }
}
