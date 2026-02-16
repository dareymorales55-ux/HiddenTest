package com.hiddentest;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
        String realVictimName = ProfileManager.getRealName(victim);

        // 🔥 Weapon must match VICTIM'S real name
        if (!weaponName.equals(realVictimName)) return;

        // ✅ Remove vanilla death message
        event.setDeathMessage(null);

        // ✅ Switch order: Leave first, then caught
        Bukkit.broadcastMessage(ChatColor.YELLOW + realVictimName + " left the game");
        Bukkit.broadcastMessage(ChatColor.RED + realVictimName + " has been caught.");

        Bukkit.getBanList(BanList.Type.NAME).addBan(
                realVictimName,
                ChatColor.DARK_RED + "Your cover was blown.",
                null,
                null
        );

        victim.kickPlayer(ChatColor.DARK_RED + "Your cover was blown.");
    }

    // ✅ Suppress automatic quit message
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }
}
