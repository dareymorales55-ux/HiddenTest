package com.hiddentest;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.hiddentest.items.UnknownTotem;
import com.hiddentest.reveal.RevealManager;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;

public class DeathListener implements Listener {

    // ✅ Regular Minecraft red
    private static final String CAUGHT_COLOR =
            ChatColor.RED.toString();

    // ✅ Orange for head name
    private static final String ORANGE =
            ChatColor.of("#FFA500").toString();

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player victim =
                event.getEntity();

        if (victim.hasMetadata("caught"))
            return;

        String realVictimName =
                ProfileManager.getRealName(victim);

        // =============================
        // DETERMINE PLAYER-RELATED DEATH
        // =============================

        Player killer =
                victim.getKiller();

        boolean playerRelated =
                killer != null;

        if (!playerRelated) {

            EntityDamageEvent lastDamage =
                    victim.getLastDamageCause();

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

        // =============================
        // NAME WEAPON CHECK
        // =============================

        boolean nameWeaponMatch = false;

        if (killer != null) {

            ItemStack weapon =
                    killer.getInventory()
                            .getItemInMainHand();

            if (weapon != null &&
                weapon.hasItemMeta()) {

                if (weapon.getItemMeta()
                        .hasDisplayName()) {

                    String weaponName =
                            ChatColor.stripColor(
                                    weapon.getItemMeta()
                                            .getDisplayName()
                            );

                    nameWeaponMatch =
                            weaponName.equalsIgnoreCase(
                                    realVictimName
                            );
                }
            }
        }

        boolean revealed =
                RevealManager.isRevealed(victim);

        if (!nameWeaponMatch &&
            !(revealed && playerRelated)) {

            return;
        }

        // =============================
        // UNKNOWN TOTEM SAVE
        // =============================

        if (UnknownTotem.hasUnknownTotem(victim)) {

            UnknownTotem.consumeTotem(victim);

            Bukkit.broadcastMessage(
                    ChatColor.RED +
                    victim.getName() +
                    " has been saved by the Unknown Totem."
            );

            return;
        }

        // =============================
        // MARK CAUGHT
        // =============================

        victim.setMetadata(
                "caught",
                new FixedMetadataValue(
                        HiddenTest.getInstance(),
                        true
                )
        );

        RevealManager.hide(victim);

        // =============================
        // CREATE HEAD
        // =============================

        ItemStack head =
                new ItemStack(Material.PLAYER_HEAD);

        SkullMeta meta =
                (SkullMeta) head.getItemMeta();

        if (meta != null) {

            // ✅ REAL PLAYER SKIN
            PlayerProfile profile =
                    ProfileManager.getRealProfile(victim);

            if (profile != null) {

                meta.setPlayerProfile(
                        profile.clone()
                );
            }

            // ✅ BOLD ORANGE NAME
            meta.setDisplayName(
                    ORANGE +
                    "" +
                    ChatColor.BOLD +
                    realVictimName +
                    "'s Head"
            );

            // =============================
            // LOCATION LORE
            // =============================

            Location loc =
                    victim.getLocation();

            String coords =
                    ChatColor.YELLOW +
                    "Died at " +
                    loc.getBlockX() + ", " +
                    loc.getBlockY() + ", " +
                    loc.getBlockZ();

            // =============================
            // DIMENSION LORE
            // =============================

            String dimensionLine;

            World.Environment env =
                    loc.getWorld()
                            .getEnvironment();

            switch (env) {

                case NORMAL:

                    dimensionLine =
                            ChatColor.GREEN +
                            "Overworld";

                    break;

                case NETHER:

                    dimensionLine =
                            ChatColor.RED +
                            "Nether";

                    break;

                case THE_END:

                    dimensionLine =
                            ChatColor.LIGHT_PURPLE +
                            "The End";

                    break;

                default:

                    dimensionLine =
                            ChatColor.GRAY +
                            "Unknown";

                    break;
            }

            meta.setLore(Arrays.asList(
                    coords,
                    dimensionLine
            ));

            head.setItemMeta(meta);
        }

        // =============================
        // DROP HEAD
        // =============================

        victim.getWorld().dropItemNaturally(
                victim.getLocation(),
                head
        );

        // =============================
        // BROADCAST
        // =============================

        Bukkit.broadcastMessage(
                ChatColor.YELLOW +
                realVictimName +
                ChatColor.YELLOW +
                " left the game"
        );

        Bukkit.broadcastMessage(
                CAUGHT_COLOR +
                realVictimName +
                " has been caught."
        );

        // =============================
        // GLOBAL SOUND
        // =============================

        for (Player online :
                Bukkit.getOnlinePlayers()) {

            online.playSound(
                    online.getLocation(),
                    Sound.ENTITY_WITHER_SPAWN,
                    1.5f,
                    1.0f
            );
        }

        // =============================
        // BAN PLAYER
        // =============================

        Bukkit.getBanList(BanList.Type.NAME)
                .addBan(
                        realVictimName,
                        CAUGHT_COLOR +
                        "You have been caught.",
                        null,
                        null
                );

        // =============================
        // KICK PLAYER
        // =============================

        victim.kickPlayer(
                CAUGHT_COLOR +
                "You have been caught."
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {

        Player player =
                event.getPlayer();

        if (player.hasMetadata("caught")) {

            event.setQuitMessage(null);

            player.removeMetadata(
                    "caught",
                    HiddenTest.getInstance()
            );
        }
    }
}
