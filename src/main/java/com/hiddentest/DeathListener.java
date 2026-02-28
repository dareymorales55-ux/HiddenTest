package com.hiddentest;

import com.hiddentest.reveal.RevealManager;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player victim = event.getEntity();

        // HARD STOP if already processed
        if (victim.hasMetadata("caught")) return;

        Player killer = victim.getKiller();
        if (killer == null) return;

        String realVictimName = ProfileManager.getRealName(victim);

        boolean nameWeaponMatch = false;

        ItemStack weapon = killer.getInventory().getItemInMainHand();
        if (weapon != null && weapon.hasItemMeta()) {
            ItemMeta meta = weapon.getItemMeta();
            if (meta != null && meta.hasDisplayName()) {
                String weaponName = ChatColor.stripColor(meta.getDisplayName());
                nameWeaponMatch = weaponName.equals(realVictimName);
            }
        }

        boolean victimIsRevealed = RevealManager.isRevealed(victim);

        if (!nameWeaponMatch && !victimIsRevealed) return;

        // =============================
        // MARK AS CAUGHT (prevents double fire)
        // =============================
        victim.setMetadata("caught",
                new FixedMetadataValue(HiddenTest.getInstance(), true));

        // Clear reveal state
        RevealManager.hide(victim);

        // =============================
        // DROP REAL SKIN HEAD
        // =============================
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();

        if (skullMeta != null) {
            skullMeta.setOwningPlayer(victim); // real skin
            skullMeta.setDisplayName(ChatColor.RED + realVictimName + "'s Head");
            head.setItemMeta(skullMeta);
        }

        victim.getWorld().dropItemNaturally(victim.getLocation(), head);

        // =============================
        // BROADCAST ONCE
        // =============================
        Bukkit.broadcastMessage(ChatColor.YELLOW + realVictimName + " left the game");
        Bukkit.broadcastMessage(ChatColor.RED + realVictimName + " has been caught.");

        // =============================
        // BAN + KICK
        // =============================
        Bukkit.getBanList(BanList.Type.NAME).addBan(
                realVictimName,
                ChatColor.DARK_RED + "Your cover was blown.",
                null,
                null
        );

        victim.kickPlayer(ChatColor.DARK_RED + "Your cover was blown.");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        if (player.hasMetadata("caught")) {
            event.setQuitMessage(null);
            player.removeMetadata("caught", HiddenTest.getInstance());
        }
    }
}
