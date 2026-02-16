package com.hiddentest.items;

import com.hiddentest.HiddenTest;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DetectivesCompass implements Listener {

    private final HiddenTest plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, UUID> tracking = new HashMap<>();

    // 15 minute cooldown
    private static final int COOLDOWN_SECONDS = 15 * 60;

    // 5 minute tracking duration
    private static final int TRACK_DURATION_SECONDS = 5 * 60;

    private static final double MIN_TRACK_DISTANCE = 8.0;

    public DetectivesCompass(HiddenTest plugin) {
        this.plugin = plugin;
    }

    // =========================
    // ITEM CREATION
    // =========================

    public static ItemStack createDetectivesCompass() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Detective’s Compass");

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Right-click to hunt a random player");
            lore.add(ChatColor.GRAY + "Tracks target for 5 minutes");
            lore.add(ChatColor.GRAY + "Does not track players within an 8 block radius");
            lore.add(ChatColor.RED + "Overworld only");

            meta.setLore(lore);

            // Enchant glint
            meta.addEnchant(Enchantment.DURABILITY, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

            item.setItemMeta(meta);
        }

        return item;
    }

    private boolean isDetectivesCompass(ItemStack item) {
        if (item == null || item.getType() != Material.COMPASS) return false;
        if (!item.hasItemMeta()) return false;
        if (!item.getItemMeta().hasDisplayName()) return false;

        return item.getItemMeta().getDisplayName()
                .equals(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Detective’s Compass");
    }

    // =========================
    // ITEM USE
    // =========================

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player hunter = event.getPlayer();
        ItemStack item = event.getItem();

        if (!isDetectivesCompass(item)) return;

        event.setCancelled(true);

        if (hunter.getWorld().getEnvironment() != World.Environment.NORMAL) {
            hunter.sendMessage(ChatColor.RED + "Compass only works in the overworld.");
            return;
        }

        // Cooldown check
        if (cooldowns.containsKey(hunter.getUniqueId())) {
            long timeLeft = (cooldowns.get(hunter.getUniqueId()) - System.currentTimeMillis()) / 1000;
            if (timeLeft > 0) {
                hunter.sendMessage(ChatColor.RED + "Cooldown: " + formatTime((int) timeLeft));
                return;
            }
        }

        List<Player> possibleTargets = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {

            if (p.equals(hunter)) continue;

            if (p.getWorld().getEnvironment() != World.Environment.NORMAL)
                continue;

            // Skip players within 8 block radius
            if (p.getWorld().equals(hunter.getWorld())) {
                if (p.getLocation().distance(hunter.getLocation()) <= MIN_TRACK_DISTANCE)
                    continue;
            }

            possibleTargets.add(p);
        }

        if (possibleTargets.isEmpty()) {
            hunter.sendMessage(ChatColor.GRAY + "No players to track.");
            return;
        }

        Player target = possibleTargets.get(new Random().nextInt(possibleTargets.size()));

        hunter.playSound(hunter.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);
        target.playSound(target.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);

        hunter.sendMessage(ChatColor.RED + "Hunting " + target.getName());
        target.sendMessage(ChatColor.DARK_RED + "You are being hunted.");

        cooldowns.put(hunter.getUniqueId(),
                System.currentTimeMillis() + COOLDOWN_SECONDS * 1000L);

        hunter.setCooldown(Material.COMPASS, COOLDOWN_SECONDS * 20);

        tracking.put(hunter.getUniqueId(), target.getUniqueId());

        startTracking(hunter, target);
    }

    // =========================
    // TRACKING LOGIC
    // =========================

    private void startTracking(Player hunter, Player target) {

        new BukkitRunnable() {

            int timeLeft = TRACK_DURATION_SECONDS;

            @Override
            public void run() {

                if (!hunter.isOnline() || !target.isOnline()) {
                    end();
                    cancel();
                    return;
                }

                if (!hunter.getWorld().equals(target.getWorld())) {
                    hunter.sendActionBar(ChatColor.GRAY + "Target in another dimension");
                    end();
                    cancel();
                    return;
                }

                double distance = hunter.getLocation().distance(target.getLocation());
                String arrow = getDirectionArrow(hunter, target);

                hunter.sendActionBar(ChatColor.RED + "" + ChatColor.BOLD +
                        formatTime(timeLeft) + ChatColor.GRAY +
                        " | " + (int) distance + "m " + arrow);

                target.sendActionBar(ChatColor.RED + formatTime(timeLeft));

                if (--timeLeft <= 0) {
                    hunter.sendMessage(ChatColor.GRAY + "Hunt ended.");
                    target.sendMessage(ChatColor.GRAY + "Hunt ended.");
                    end();
                    cancel();
                }
            }

            private void end() {
                tracking.remove(hunter.getUniqueId());

                hunter.playSound(hunter.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1f, 1f);
                if (target.isOnline()) {
                    target.playSound(target.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 1f, 1f);
                }
            }

        }.runTaskTimer(plugin, 0L, 20L);
    }

    // =========================
    // UTIL
    // =========================

    private String getDirectionArrow(Player hunter, Player target) {

        double dx = target.getLocation().getX() - hunter.getLocation().getX();
        double dz = target.getLocation().getZ() - hunter.getLocation().getZ();

        double angle = Math.toDegrees(Math.atan2(dz, dx)) - 90;
        double yaw = hunter.getLocation().getYaw();
        double relative = (angle - yaw + 360) % 360;

        if (relative < 22.5 || relative >= 337.5) return "⬆";
        if (relative < 67.5) return "⬈";
        if (relative < 112.5) return "➡";
        if (relative < 157.5) return "⬊";
        if (relative < 202.5) return "⬇";
        if (relative < 247.5) return "⬋";
        if (relative < 292.5) return "⬅";
        return "⬉";
    }

    private String formatTime(int seconds) {
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }
}
