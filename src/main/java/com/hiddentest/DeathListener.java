package com.hiddentest;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.hiddentest.reveal.RevealManager;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class DeathListener implements Listener {

    private static final String CAUGHT_COLOR = ChatColor.of("#B41926").toString();

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player victim = event.getEntity();
        if (victim.hasMetadata("caught")) return;

        String realVictimName = ProfileManager.getRealName(victim);

        Player killer = victim.getKiller();
        boolean playerRelated = killer != null;

        if (!playerRelated) {
            EntityDamageEvent lastDamage = victim.getLastDamageCause();
            if (lastDamage != null) {
                switch (lastDamage.getCause()) {
                    case FIRE:
                    case FIRE_TICK:
                    case LAVA:
                    case FALL:
                    case ENTITY_EXPLOSION:
                    case BLOCK_EXPLOSION:
                    case PROJECTILE:
                        playerRelated = true;
                        break;
                }
            }
        }

        boolean nameWeaponMatch = false;

        if (killer != null) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (weapon != null && weapon.hasItemMeta()) {
                ItemMeta meta = weapon.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String weaponName = ChatColor.stripColor(meta.getDisplayName());
                    nameWeaponMatch = weaponName.equalsIgnoreCase(realVictimName);
                }
            }
        }

        boolean revealed = RevealManager.isRevealed(victim);
        if (!nameWeaponMatch && !(revealed && playerRelated)) return;

        victim.setMetadata("caught",
                new FixedMetadataValue(HiddenTest.getInstance(), true));

        RevealManager.hide(victim);

        // 🔥 FIXED HEAD (REAL SKIN)
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();

        if (meta != null) {
            PlayerProfile profile = ProfileManager.getRealProfile(victim);

            if (profile != null) {
                meta.setPlayerProfile(profile.clone()); // ✅ THIS is the fix
            }

            meta.setDisplayName(ChatColor.RED + realVictimName + "'s Head");
            head.setItemMeta(meta);
        }

        victim.getWorld().dropItemNaturally(victim.getLocation(), head);

        Bukkit.broadcastMessage(ChatColor.YELLOW + realVictimName + " left the game");
        Bukkit.broadcastMessage(CAUGHT_COLOR + realVictimName + " has been caught.");

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.playSound(
                    online.getLocation(),
                    Sound.ENTITY_WITHER_SPAWN,
                    1.5f,
                    1.0f
            );
        }

        Bukkit.getBanList(BanList.Type.NAME).addBan(
                realVictimName,
                CAUGHT_COLOR + "You have been caught.",
                null,
                null
        );

        victim.kickPlayer(CAUGHT_COLOR + "You have been caught.");
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
