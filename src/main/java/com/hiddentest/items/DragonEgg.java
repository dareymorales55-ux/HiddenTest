package com.hiddentest.items;

import com.hiddentest.HiddenTest;
import com.hiddentest.reveal.RevealManager;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class DragonEgg implements Listener {

    private final HiddenTest plugin;

    private static final double RADIUS = 8.0;
    private static final int REVEAL_DURATION = 5 * 20;
    private static final int COOLDOWN_SECONDS = 20;
    private static final int COOLDOWN_TICKS = COOLDOWN_SECONDS * 20;

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    public DragonEgg(HiddenTest plugin) {
        this.plugin = plugin;
        startPassiveTask();
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!event.hasItem()) return;

        ItemStack item = event.getItem();
        if (!isDragonEgg(item)) return;

        // ❌ Prevent placing the egg
        event.setCancelled(true);

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        long remaining = getCooldownRemaining(uuid);
        if (remaining > 0) {
            player.sendMessage(ChatColor.RED + "Dragon Egg on cooldown: " + remaining + "s");
            return;
        }

        cooldowns.put(uuid, System.currentTimeMillis() + COOLDOWN_SECONDS * 1000L);
        player.setCooldown(Material.DRAGON_EGG, COOLDOWN_TICKS);

        player.getWorld().playSound(
                player.getLocation(),
                Sound.ENTITY_ENDER_DRAGON_GROWL,
                1f,
                1f
        );

        new BukkitRunnable() {

            int count = 5;

            @Override
            public void run() {

                if (count <= 0) {

                    for (Player target : Bukkit.getOnlinePlayers()) {

                        if (!target.getWorld().equals(player.getWorld())) continue;
                        if (target.equals(player)) continue;

                        if (target.getLocation().distance(player.getLocation()) <= RADIUS) {
                            RevealManager.reveal(target, REVEAL_DURATION);
                        }
                    }

                    cancel();
                    return;
                }

                for (Player p : Bukkit.getOnlinePlayers()) {

                    if (!p.getWorld().equals(player.getWorld())) continue;

                    if (p.getLocation().distance(player.getLocation()) <= 15.0) {
                        p.sendTitle(
                                count == 1
                                        ? ChatColor.RED + "1"
                                        : ChatColor.LIGHT_PURPLE + String.valueOf(count),
                                "",
                                0, 20, 0
                        );
                    }
                }

                spawnRing(player);
                count--;
            }

        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void startPassiveTask() {

        new BukkitRunnable() {

            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {

                    // Replace normal eggs with custom egg
                    for (int i = 0; i < player.getInventory().getSize(); i++) {

                        ItemStack item = player.getInventory().getItem(i);
                        if (item == null) continue;

                        if (item.getType() == Material.DRAGON_EGG && !isDragonEgg(item)) {
                            player.getInventory().setItem(i, createDragonEgg());
                        }
                    }

                    boolean hasEgg = hasDragonEgg(player);

                    // 🔥 Locator Bar Fix
                    AttributeInstance transmit =
                            player.getAttribute(Attribute.WAYPOINT_TRANSMIT_RANGE);

                    AttributeInstance receive =
                            player.getAttribute(Attribute.WAYPOINT_RECEIVE_RANGE);

                    if (transmit != null) {
                        transmit.setBaseValue(6_000_000.0); // everyone transmits
                    }

                    if (receive != null) {
                        receive.setBaseValue(hasEgg ? 6_000_000.0 : 0.0); // only egg holders receive
                    }

                    if (hasEgg) {
                        player.getWorld().spawnParticle(
                                Particle.PORTAL,
                                player.getLocation().add(0, 1.1, 0),
                                35,
                                0.8, 1.0, 0.8,
                                0
                        );
                    }
                }
            }

        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void spawnRing(Player player) {

        int points = 240;
        double y = player.getLocation().getY() + 1.0;

        for (int i = 0; i < points; i++) {

            double angle = 2 * Math.PI * i / points;
            double x = player.getLocation().getX() + RADIUS * Math.cos(angle);
            double z = player.getLocation().getZ() + RADIUS * Math.sin(angle);

            Location loc = new Location(player.getWorld(), x, y, z);

            player.getWorld().spawnParticle(
                    Particle.PORTAL,
                    loc,
                    4,
                    0.05, 0.05, 0.05,
                    0
            );
        }
    }

    private boolean hasDragonEgg(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isDragonEgg(item)) return true;
        }
        return false;
    }

    private boolean isDragonEgg(ItemStack item) {

        if (item == null) return false;
        if (item.getType() != Material.DRAGON_EGG) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;

        return meta.getDisplayName().equals(
                ChatColor.LIGHT_PURPLE.toString() +
                        ChatColor.BOLD +
                        "Dragon Egg"
        );
    }

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

    public static ItemStack createDragonEgg() {

        ItemStack egg = new ItemStack(Material.DRAGON_EGG);
        ItemMeta meta = egg.getItemMeta();

        meta.setDisplayName(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Dragon Egg");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click to reveal players in an 8 block radius");
        lore.add(ChatColor.GRAY + "Reveal lasts 5 seconds");
        lore.add(ChatColor.GRAY + "Gain heart on kill");
        lore.add(ChatColor.RED + "20 second cooldown");

        meta.setLore(lore);

        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        egg.setItemMeta(meta);

        return egg;
    }
}
