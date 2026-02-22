package com.hiddentest.items;

import com.hiddentest.HiddenTest;
import com.hiddentest.reveal.RevealManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class BellOfTruth implements Listener {

    private final HiddenTest plugin;
    private final NamespacedKey bellKey;
    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static final double RADIUS = 15.0;

    // ✅ USE TICKS (15 minutes)
    private static final int REVEAL_DURATION = 15 * 60 * 20; // 18,000 ticks

    private static final int COOLDOWN_SECONDS = 300; // 5 minutes

    public BellOfTruth(HiddenTest plugin) {
        this.plugin = plugin;
        this.bellKey = new NamespacedKey(plugin, "bell_of_truth");
    }

    /* =========================
       PLACE
       ========================= */

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {

        ItemStack item = event.getItemInHand();
        if (item.getType() != Material.BELL) return;
        if (!item.hasItemMeta()) return;

        if (!item.getItemMeta().getPersistentDataContainer()
                .has(bellKey, PersistentDataType.BOOLEAN)) return;

        Block block = event.getBlockPlaced();

        NamespacedKey blockKey = new NamespacedKey(
                plugin,
                "bell_" + block.getX() + "_" + block.getY() + "_" + block.getZ()
        );

        block.getChunk().getPersistentDataContainer()
                .set(blockKey, PersistentDataType.BOOLEAN, true);
    }

    /* =========================
       BREAK
       ========================= */

    @EventHandler
    public void onBreak(BlockBreakEvent event) {

        Block block = event.getBlock();
        if (block.getType() != Material.BELL) return;

        NamespacedKey blockKey = new NamespacedKey(
                plugin,
                "bell_" + block.getX() + "_" + block.getY() + "_" + block.getZ()
        );

        if (!block.getChunk().getPersistentDataContainer()
                .has(blockKey, PersistentDataType.BOOLEAN)) return;

        event.setDropItems(false);
        block.getWorld().dropItemNaturally(
                block.getLocation(),
                createBell()
        );

        block.getChunk().getPersistentDataContainer().remove(blockKey);
    }

    /* =========================
       RING
       ========================= */

    @EventHandler
    public void onRing(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.BELL) return;

        NamespacedKey blockKey = new NamespacedKey(
                plugin,
                "bell_" + block.getX() + "_" + block.getY() + "_" + block.getZ()
        );

        if (!block.getChunk().getPersistentDataContainer()
                .has(blockKey, PersistentDataType.BOOLEAN)) return;

        Player player = event.getPlayer();

        long remaining = getCooldownRemaining(player.getUniqueId());
        if (remaining > 0) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED +
                    "Bell on cooldown: " + formatTime((int) remaining));
            return;
        }

        cooldowns.put(player.getUniqueId(),
                System.currentTimeMillis() + COOLDOWN_SECONDS * 1000L);

        Location center = block.getLocation().add(0.5, 0.5, 0.5);

        spawnParticles(center);
        revealNearby(center);
    }

    /* =========================
       REVEAL
       ========================= */

    private void revealNearby(Location center) {

        for (Player target : Bukkit.getOnlinePlayers()) {

            if (!target.getWorld().equals(center.getWorld())) continue;
            if (target.getLocation().distance(center) > RADIUS) continue;

            // ✅ NOW CORRECT (ticks)
            RevealManager.reveal(target, REVEAL_DURATION);
        }
    }

    /* =========================
       PARTICLES
       ========================= */

    private void spawnParticles(Location center) {

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                if (ticks >= 40) {
                    cancel();
                    return;
                }

                int points = 40;

                for (int i = 0; i < points; i++) {

                    double angle = 2 * Math.PI * i / points;

                    double x = center.getX() + RADIUS * Math.cos(angle);
                    double z = center.getZ() + RADIUS * Math.sin(angle);

                    Location loc = new Location(
                            center.getWorld(),
                            x,
                            center.getY(),
                            z
                    );

                    center.getWorld().spawnParticle(
                            Particle.DUST,
                            loc,
                            1,
                            new Particle.DustOptions(Color.RED, 1.5f)
                    );
                }

                ticks++;
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    /* ========================= */

    private long getCooldownRemaining(UUID uuid) {

        Long expiry = cooldowns.get(uuid);
        if (expiry == null) return 0;

        long remaining = (expiry - System.currentTimeMillis()) / 1000L;

        if (remaining <= 0) {
            cooldowns.remove(uuid);
            return 0;
        }

        return remaining;
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /* =========================
       CREATE ITEM
       ========================= */

    public ItemStack createBell() {

        ItemStack bell = new ItemStack(Material.BELL);
        ItemMeta meta = bell.getItemMeta();

        meta.setDisplayName(ChatColor.RED.toString() + ChatColor.BOLD + "Bell of Truth");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Ring to reveal players");
        lore.add(ChatColor.GRAY + "Reveals players in a 15 block radius");
        lore.add(ChatColor.RED + "Reveals the ringer");

        meta.setLore(lore);

        meta.addEnchant(Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        meta.getPersistentDataContainer()
                .set(bellKey, PersistentDataType.BOOLEAN, true);

        bell.setItemMeta(meta);
        return bell;
    }
}
