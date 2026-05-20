package com.hiddentest.items;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;

import com.hiddentest.HiddenTest;
import com.hiddentest.ProfileManager;
import com.hiddentest.reveal.RevealManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BloodCompass implements Listener {

    private final HiddenTest plugin;

    private final Map<UUID, UUID> tracking = new HashMap<>();

    // ✅ 3 minute tracking
    private static final int TRACK_DURATION_SECONDS = 3 * 60;

    private static final double MIN_TRACK_DISTANCE = 5.0;

    // ✅ COLORS
    private static final String COLOR =
            ChatColor.DARK_RED.toString();

    public BloodCompass(HiddenTest plugin) {

        this.plugin = plugin;

        registerRecipe();
    }

    public static ItemStack createBloodCompass() {

        ItemStack item =
                new ItemStack(Material.RECOVERY_COMPASS);

        ItemMeta meta =
                item.getItemMeta();

        if (meta != null) {

            meta.setDisplayName(
                    COLOR +
                    "" +
                    ChatColor.BOLD +
                    "Blood Compass"
            );

            List<String> lore =
                    new ArrayList<>();

            lore.add(ChatColor.GRAY +
                    "Right-click to hunt a revealed player");

            lore.add(ChatColor.GRAY +
                    "Tracks target for 3 minutes");

            lore.add(ChatColor.GRAY +
                    "Does not track players within 5 blocks");

            lore.add(ChatColor.RED +
                    "One time use");

            lore.add(ChatColor.RED +
                    "Overworld only");

            meta.setLore(lore);

            Enchantment unbreaking =
                    Registry.ENCHANTMENT.get(
                            NamespacedKey.minecraft("unbreaking")
                    );

            if (unbreaking != null) {

                meta.addEnchant(
                        unbreaking,
                        1,
                        true
                );
            }

            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            item.setItemMeta(meta);
        }

        return item;
    }

    private boolean isBloodCompass(ItemStack item) {

        if (item == null
                || item.getType() != Material.RECOVERY_COMPASS)
            return false;

        if (!item.hasItemMeta()) return false;

        if (!item.getItemMeta().hasDisplayName())
            return false;

        return item.getItemMeta().getDisplayName().equals(
                COLOR +
                "" +
                ChatColor.BOLD +
                "Blood Compass"
        );
    }

    // =========================
    // RECIPE
    // =========================

    private void registerRecipe() {

        NamespacedKey key =
                new NamespacedKey(plugin, "blood_compass");

        ShapedRecipe recipe =
                new ShapedRecipe(key, createBloodCompass());

        recipe.shape(
                "RER",
                "GCG",
                "RGR"
        );

        // R = Redstone Block
        recipe.setIngredient(
                'R',
                Material.REDSTONE_BLOCK
        );

        // E = Enchanted Golden Apple
        recipe.setIngredient(
                'E',
                Material.ENCHANTED_GOLDEN_APPLE
        );

        // G = Golden Apple
        recipe.setIngredient(
                'G',
                Material.GOLDEN_APPLE
        );

        // C = Recovery Compass
        recipe.setIngredient(
                'C',
                Material.RECOVERY_COMPASS
        );

        Bukkit.addRecipe(recipe);
    }

    // =========================
    // USE
    // =========================

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player hunter =
                event.getPlayer();

        ItemStack item =
                event.getItem();

        if (!isBloodCompass(item)) return;

        event.setCancelled(true);

        if (hunter.getWorld().getEnvironment()
                != World.Environment.NORMAL) {

            hunter.sendMessage(
                    ChatColor.RED +
                    "Compass only works in the overworld."
            );

            return;
        }

        List<Player> possibleTargets =
                new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {

            if (p.equals(hunter)) continue;

            // ✅ ONLY REVEALED PLAYERS
            if (!RevealManager.isRevealed(p)) continue;

            if (p.getWorld().getEnvironment()
                    != World.Environment.NORMAL)
                continue;

            if (p.getWorld().equals(hunter.getWorld())) {

                if (p.getLocation()
                        .distance(hunter.getLocation())
                        <= MIN_TRACK_DISTANCE)
                    continue;
            }

            possibleTargets.add(p);
        }

        if (possibleTargets.isEmpty()) {

            hunter.sendMessage(
                    ChatColor.GRAY +
                    "No revealed players to track."
            );

            return;
        }

        possibleTargets.sort(
                Comparator.comparingDouble(p ->
                        p.getLocation()
                                .distanceSquared(
                                        hunter.getLocation()
                                )
                )
        );

        Player target =
                possibleTargets.get(0);

        hunter.playSound(
                hunter.getLocation(),
                Sound.BLOCK_RESPAWN_ANCHOR_CHARGE,
                1f,
                1f
        );

        target.playSound(
                target.getLocation(),
                Sound.BLOCK_RESPAWN_ANCHOR_CHARGE,
                1f,
                1f
        );

        String realName =
                ProfileManager.getRealName(target);

        hunter.sendMessage(
                ChatColor.RED +
                "Hunting: " +
                ChatColor.WHITE +
                realName
        );

        target.sendMessage(
                ChatColor.DARK_RED +
                "" +
                ChatColor.BOLD +
                "You are being hunted."
        );

        tracking.put(
                hunter.getUniqueId(),
                target.getUniqueId()
        );

        startTracking(hunter, target);
    }

    // =========================
    // TRACKING
    // =========================

    private void startTracking(
            Player hunter,
            Player target
    ) {

        new BukkitRunnable() {

            int secondsLeft =
                    TRACK_DURATION_SECONDS;

            int tickCounter = 0;

            boolean targetLoggedOut = false;

            @Override
            public void run() {

                if (!hunter.isOnline()) {

                    cancel();
                    return;
                }

                // ✅ TARGET LOGGED OUT
                if (!target.isOnline()) {

                    targetLoggedOut = true;

                    hunter.sendMessage(
                            ChatColor.GRAY +
                            "Target left the game."
                    );

                    end(false);

                    cancel();

                    return;
                }

                if (!hunter.getWorld()
                        .equals(target.getWorld())) {

                    hunter.sendActionBar(
                            ChatColor.GRAY +
                            "Target in another dimension"
                    );

                    end(true);

                    cancel();

                    return;
                }

                double distance =
                        hunter.getLocation()
                                .distance(
                                        target.getLocation()
                                );

                String arrow =
                        getDirectionArrow(hunter, target);

                ChatColor distColor;

                if (distance <= 15) {

                    distColor = ChatColor.GREEN;

                } else if (distance <= 24) {

                    distColor = ChatColor.YELLOW;

                } else {

                    distColor = ChatColor.RED;
                }

                hunter.sendActionBar(
                        ChatColor.RED +
                        "" +
                        ChatColor.BOLD +
                        formatTime(secondsLeft) +

                        ChatColor.GRAY +
                        " | " +

                        distColor +
                        (int) distance +
                        "m " +
                        arrow
                );

                target.sendActionBar(
                        ChatColor.WHITE +
                        formatTime(secondsLeft)
                );

                tickCounter += 2;

                if (tickCounter >= 20) {

                    tickCounter = 0;

                    secondsLeft--;

                    if (secondsLeft <= 0) {

                        hunter.sendMessage(
                                ChatColor.GRAY +
                                "Hunt ended."
                        );

                        target.sendMessage(
                                ChatColor.GRAY +
                                "Hunt ended."
                        );

                        end(true);

                        cancel();
                    }
                }
            }

            private void end(boolean consumeItem) {

                tracking.remove(
                        hunter.getUniqueId()
                );

                hunter.playSound(
                        hunter.getLocation(),
                        Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,
                        1f,
                        1f
                );

                if (target.isOnline()) {

                    target.playSound(
                            target.getLocation(),
                            Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,
                            1f,
                            1f
                    );
                }

                // ✅ DESTROY AFTER USE
                // ❌ DO NOT destroy if target logged out
                if (consumeItem && !targetLoggedOut) {

                    ItemStack mainHand =
                            hunter.getInventory()
                                    .getItemInMainHand();

                    if (isBloodCompass(mainHand)) {

                        hunter.getInventory()
                                .setItemInMainHand(null);

                    } else {

                        ItemStack offHand =
                                hunter.getInventory()
                                        .getItemInOffHand();

                        if (isBloodCompass(offHand)) {

                            hunter.getInventory()
                                    .setItemInOffHand(null);
                        }
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 2L);
    }

    // =========================
    // DIRECTION
    // =========================

    private String getDirectionArrow(
            Player hunter,
            Player target
    ) {

        double dx =
                target.getLocation().getX()
                        - hunter.getLocation().getX();

        double dz =
                target.getLocation().getZ()
                        - hunter.getLocation().getZ();

        double angle =
                Math.toDegrees(Math.atan2(dz, dx))
                        - 90;

        double yaw =
                hunter.getLocation().getYaw();

        double relative =
                (angle - yaw + 360) % 360;

        if (relative < 22.5 || relative >= 337.5)
            return "⬆";

        if (relative < 67.5)
            return "⬈";

        if (relative < 112.5)
            return "➡";

        if (relative < 157.5)
            return "⬊";

        if (relative < 202.5)
            return "⬇";

        if (relative < 247.5)
            return "⬋";

        if (relative < 292.5)
            return "⬅";

        return "⬉";
    }

    // =========================
    // TIME FORMAT
    // =========================

    private String formatTime(int seconds) {

        return String.format(
                "%02d:%02d",
                seconds / 60,
                seconds % 60
        );
    }
}
