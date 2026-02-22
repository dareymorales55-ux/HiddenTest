package com.hiddentest.mobs;

import com.hiddentest.items.DetectivesCompass;
import com.hiddentest.items.BellOfTruth;
import com.hiddentest.reveal.RevealManager;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.potion.*;

import org.bukkit.NamespacedKey;

import java.util.Random;

public class UnknownChicken implements Listener, CommandExecutor {

    private final JavaPlugin plugin;
    private final NamespacedKey key;
    private final Random random = new Random();

    public UnknownChicken(JavaPlugin plugin) {
        this.plugin = plugin;
        this.key = new NamespacedKey(plugin, "unknown_chicken");
    }

    // =========================
    // COMMAND
    // =========================

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        Location loc = player.getLocation();
        spawn(loc);

        World world = loc.getWorld();

        world.playSound(loc, Sound.ENTITY_ENDER_DRAGON_GROWL, 3f, 1f);

        Bukkit.broadcastMessage(
                ChatColor.GOLD + "" + ChatColor.BOLD +
                        "The Unknown Chicken has been summoned at (" +
                        loc.getBlockX() + ", " +
                        loc.getBlockY() + ", " +
                        loc.getBlockZ() + ")."
        );

        return true;
    }

    // =========================
    // SPAWN
    // =========================

    public Chicken spawn(Location location) {

        Chicken chicken = (Chicken) location.getWorld().spawnEntity(location, EntityType.CHICKEN);

        chicken.setCustomName(ChatColor.YELLOW + "" + ChatColor.BOLD + "Unknown Chicken");
        chicken.setCustomNameVisible(false);
        chicken.setRemoveWhenFarAway(false);
        chicken.setAdult();

        chicken.getAttribute(Attribute.MAX_HEALTH).setBaseValue(300.0);
        chicken.setHealth(300.0);

        chicken.getAttribute(Attribute.SCALE).setBaseValue(10.0);

        chicken.setGlowing(true);

        chicken.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, 1);

        createBossBar(chicken);
        startAbilities(chicken);

        return chicken;
    }

    // =========================
    // BOSS BAR
    // =========================

    private void createBossBar(Chicken chicken) {

        BossBar bar = Bukkit.createBossBar(
                ChatColor.YELLOW + "" + ChatColor.BOLD + "Unknown Chicken",
                BarColor.YELLOW,
                BarStyle.SEGMENTED_10
        );

        bar.setProgress(1.0);

        new BukkitRunnable() {
            @Override
            public void run() {

                if (!chicken.isValid() || chicken.isDead()) {
                    bar.removeAll();
                    cancel();
                    return;
                }

                for (Player p : chicken.getWorld().getPlayers()) {
                    if (p.getLocation().distance(chicken.getLocation()) <= 40) {
                        bar.addPlayer(p);
                    } else {
                        bar.removePlayer(p);
                    }
                }

                double health = chicken.getHealth();
                double max = chicken.getAttribute(Attribute.MAX_HEALTH).getBaseValue();
                bar.setProgress(Math.max(0, health / max));
            }

        }.runTaskTimer(plugin, 0L, 10L);
    }

    // =========================
    // ABILITIES
    // =========================

    private void startAbilities(Chicken chicken) {

        new BukkitRunnable() {

            int tick = 0;

            @Override
            public void run() {

                if (!chicken.isValid() || chicken.isDead()) {
                    cancel();
                    return;
                }

                tick++;

                chicken.getWorld().spawnParticle(
                        Particle.DUST,
                        chicken.getLocation().add(0, 1, 0),
                        5,
                        0.3, 0.5, 0.3,
                        new Particle.DustOptions(Color.YELLOW, 1)
                );

                if (tick % 100 == 0) {
                    strikeRandomLightning(chicken);
                }

                if (tick % 200 == 0) {

                    chicken.addPotionEffect(new PotionEffect(
                            PotionEffectType.INVISIBILITY,
                            100,
                            0,
                            false,
                            false
                    ));

                    for (Player p : chicken.getWorld().getPlayers()) {
                        if (p.getLocation().distance(chicken.getLocation()) <= 8) {
                            p.damage(6.0);
                        }
                    }
                }

                if (tick % 300 == 0) {
                    chicken.addPotionEffect(new PotionEffect(
                            PotionEffectType.INSTANT_HEALTH,
                            1,
                            1
                    ));
                }
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    // =========================
    // RANDOM LIGHTNING
    // =========================

    private void strikeRandomLightning(Chicken chicken) {

        World world = chicken.getWorld();
        double radius = 25;

        double offsetX = (random.nextDouble() * radius * 2) - radius;
        double offsetZ = (random.nextDouble() * radius * 2) - radius;

        Location base = chicken.getLocation().clone().add(offsetX, 0, offsetZ);

        int highestY = world.getHighestBlockYAt(base);
        Location strikeLoc = new Location(world, base.getX(), highestY, base.getZ());

        world.strikeLightning(strikeLoc);
    }

    // =========================
    // REVEAL ATTACKER
    // =========================

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        if (!(event.getEntity() instanceof Chicken chicken)) return;
        if (!chicken.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) return;

        if (event.getDamager() instanceof Player player) {
            RevealManager.reveal(player, 60);
        }
    }

    // =========================
    // DEATH
    // =========================

    @EventHandler
    public void onDeath(EntityDeathEvent event) {

        if (!(event.getEntity() instanceof Chicken chicken)) return;
        if (!chicken.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) return;

        Location deathLoc = chicken.getLocation();
        World world = chicken.getWorld();

        event.getDrops().clear();

        world.createExplosion(deathLoc, 0F, false, false);
        world.spawnParticle(Particle.EXPLOSION, deathLoc, 1);

        world.playSound(deathLoc, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 2f, 1f);

        // ✅ Proper item creation
        BellOfTruth bell = new BellOfTruth((com.hiddentest.HiddenTest) plugin);
        DetectivesCompass compass = new DetectivesCompass((com.hiddentest.HiddenTest) plugin);

        world.dropItemNaturally(deathLoc, bell.createBell());
        world.dropItemNaturally(deathLoc, compass.createCompass());

        Bukkit.broadcastMessage(
                ChatColor.GOLD + "" + ChatColor.BOLD +
                        "The Unknown Chicken has been defeated."
        );
    }
}
