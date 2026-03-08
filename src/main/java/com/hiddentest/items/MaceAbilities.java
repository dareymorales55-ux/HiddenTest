package com.hiddentest.items;

import com.hiddentest.reveal.RevealManager;

import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class MaceAbilities implements Listener {

    private final JavaPlugin plugin;

    public MaceAbilities(JavaPlugin plugin) {
        this.plugin = plugin;
        startPassiveTask();
    }

    // =========================
    // AUTO UNBREAKING III
    // =========================
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() != Material.MACE) return;

        if (!item.containsEnchantment(Enchantment.UNBREAKING)) {
            item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        checkInventory(event.getPlayer());
    }

    private void checkInventory(Player player) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.MACE) {
                if (!item.containsEnchantment(Enchantment.UNBREAKING)) {
                    item.addUnsafeEnchantment(Enchantment.UNBREAKING, 3);
                }
            }
        }
    }

    // =========================
    // PASSIVE EFFECTS
    // =========================
    private void startPassiveTask() {

        new BukkitRunnable() {
            @Override
            public void run() {

                for (Player player : Bukkit.getOnlinePlayers()) {

                    boolean hasMace = false;

                    for (ItemStack item : player.getInventory().getContents()) {
                        if (item != null && item.getType() == Material.MACE) {
                            hasMace = true;
                            break;
                        }
                    }

                    if (hasMace) {

                        // Speed I (longer so it never flickers)
                        player.addPotionEffect(
                                new PotionEffect(PotionEffectType.SPEED, 60, 0, true, false)
                        );

                        // Fire Resistance
                        player.addPotionEffect(
                                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 60, 0, true, false)
                        );

                        // Smooth constant particles (like Dragon Egg)
                        Location loc = player.getLocation().add(0, 1.0, 0);

                        player.getWorld().spawnParticle(
                                Particle.DUST_COLOR_TRANSITION,
                                loc,
                                3,
                                0.35, 0.5, 0.35,
                                new Particle.DustTransition(
                                        Color.AQUA,
                                        Color.fromRGB(173, 216, 230),
                                        1.1f
                                )
                        );
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 1L); // run every tick for smooth particles
    }

    // =========================
    // SMASH ATTACK ABILITY
    // =========================
    @EventHandler
    public void onMaceHit(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        ItemStack item = attacker.getInventory().getItemInMainHand();
        if (item == null || item.getType() != Material.MACE) return;

        // Only trigger on real smash attacks
        if (attacker.getFallDistance() < 1.5) return;

        // Reveal victim for 3 seconds
        RevealManager.reveal(victim, 60);

        // Slow Falling for 5 seconds
        victim.addPotionEffect(
                new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0)
        );
    }
}
