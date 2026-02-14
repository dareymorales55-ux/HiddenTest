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

        String realKillerName = ProfileManager.getRealName(killer);
        String realVictimName = ProfileManager.getRealName(victim);

        if (!weaponName.equals(realKillerName)) return;

        Bukkit.broadcastMessage(ChatColor.RED + realVictimName + " has been caught.");
        Bukkit.broadcastMessage(ChatColor.YELLOW + realVictimName + " left the game");

        Bukkit.getBanList(BanList.Type.NAME).addBan(
                realVictimName,
                ChatColor.DARK_RED + "Your cover was blown.",
                null,
                null
        );

        victim.kickPlayer(ChatColor.DARK_RED + "Your cover was blown.");
    }
}
