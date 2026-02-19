package com.hiddentest;

import com.hiddentest.reveal.RevealManager;
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
import org.bukkit.metadata.FixedMetadataValue;

public class DeathListener implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player victim = event.getEntity();
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

        // 🔥 Mark as caught so we suppress quit message
        victim.setMetadata("caught",
                new FixedMetadataValue(HiddenTest.getInstance(), true));

        // Optional: clear reveal state for safety
        RevealManager.hide(victim);

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

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        // Only suppress quit message if they were caught
        if (player.hasMetadata("caught")) {
            event.setQuitMessage(null);
            player.removeMetadata("caught", HiddenTest.getInstance());
        }
    }
}
