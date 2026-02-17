package com.hiddentest.items;

import com.hiddentest.HiddenTest;
import com.hiddentest.reveal.RevealManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DragonEgg implements Listener {

    private final HiddenTest plugin;
    private static final double RADIUS = 8.0; // 8 block reveal radius
    private static final long REVEAL_DURATION = 5 * 1000L; // 5 seconds
    private static final int COOLDOWN_SECONDS = 20; // 20 second cooldown
    private final java.util.Map<UUID, Long> cooldowns = new java.util.HashMap<>();

    public DragonEgg(HiddenTest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (!event.hasItem()) return;
        ItemStack item = event.getItem();
        if (item == null || item.getType() != Material.DRAGON_EGG) return;
        if (!item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return;
        if (!meta.getDisplayName().equals(ChatColor.LIGHT_PURPLE.toString() + ChatColor.BOLD + "Dragon Egg")) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        long remaining = getCooldownRemaining(uuid);
        if (remaining > 0) {
            player.sendMessage(ChatColor.RED + "Dragon Egg on cooldown: " + remaining + "s");
            event.setCancelled(true);
            return;
        }

        cooldowns.put(uuid, System.currentTimeMillis() + COOLDOWN_SECONDS * 1000L);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, 1f);

        // Countdown display and particles
        new BukkitRunnable() {
            int count = 5;

            @Override
            public void run() {
                if (count <= 0) {
                    // Reveal nearby players for 5 seconds
                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (!target.getWorld().equals(player.getWorld())) continue;
                        if (target.getLocation().distance(player.getLocation()) <= RADIUS) {
                            RevealManager.reveal(target, REVEAL_DURATION);
                        }
                    }
                    cancel();
                    return;
                }

                // Display timer in center of nearby players (15 block radius)
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (!p.getWorld().equals(player.getWorld())) continue;
                    if (p.getLocation().distance(player.getLocation()) <= 15.0) {
                        p.sendTitle(
                                count == 1 ? ChatColor.RED + "1" : ChatColor.LIGHT_PURPLE + String.valueOf(count),
                                "",
                                0, 20, 0
                        );
                    }
                }

                // Spawn moving 8-block radius purple particle ring around the ringer
                int points = 40;
                double y = player.getLocation().getY() + 1.0;
                for (int i = 0; i < points; i++) {
                    double angle = 2 * Math.PI * i / points;
                    double x = player.getLocation().getX() + RADIUS * Math.cos(angle);
                    double z = player.getLocation().getZ() + RADIUS * Math.sin(angle);
                    Location loc = new Location(player.getWorld(), x, y, z);
                    player.getWorld().spawnParticle(Particle.PORTAL, loc, 1, 0, 0, 0, 0);
                }

                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // 20L = 1 second ticks
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
        lore.add(ChatColor.RED + "20 second cooldown");
        meta.setLore(lore);

        meta.addEnchant(org.bukkit.enchantments.Enchantment.UNBREAKING, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        egg.setItemMeta(meta);

        return egg;
    }
}
