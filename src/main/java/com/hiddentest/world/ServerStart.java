package com.hiddentest.world;

import com.hiddentest.HiddenTest;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.WindCharge;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ServerStart implements CommandExecutor {

    private final HiddenTest plugin;

    public ServerStart(HiddenTest plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        World world = Bukkit.getWorlds().get(0);
        Location spawn = world.getSpawnLocation();

        new BukkitRunnable() {

            int count = 3;

            @Override
            public void run() {

                // =========================
                // ⏳ COUNTDOWN
                // =========================
                if (count > 0) {

                    for (Player p : Bukkit.getOnlinePlayers()) {

                        if (count == 3) {
                            p.sendTitle(ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "3", "", 0, 20, 0);
                        }

                        if (count == 2) {
                            p.sendTitle(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "2", "", 0, 20, 0);
                        }

                        if (count == 1) {
                            p.sendTitle(ChatColor.RED + "" + ChatColor.BOLD + "1", "", 0, 20, 0);
                        }
                    }

                    count--;
                    return;
                }

                // =========================
                // 💀 WITHER SOUND (GLOBAL)
                // =========================
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 2f, 1f);
                }

                // =========================
                // ⚡ LIGHTNING
                // =========================
                world.strikeLightning(spawn);

                // =========================
                // 🚀 LAUNCH PLAYERS (20 BLOCK RADIUS)
                // =========================
                for (Player p : Bukkit.getOnlinePlayers()) {

                    if (!p.getWorld().equals(spawn.getWorld())) continue;

                    double distance = p.getLocation().distance(spawn);
                    if (distance > 20) continue;

                    Vector direction = p.getLocation().toVector()
                            .subtract(spawn.toVector());

                    if (direction.length() == 0) {
                        direction = new Vector(
                                Math.random() - 0.5,
                                0,
                                Math.random() - 0.5
                        );
                    }

                    direction.normalize();

                    Vector velocity = direction.multiply(3.5);
                    velocity.setY(2.8);

                    p.setVelocity(velocity);

                    // 🪂 Slow Falling
                    p.addPotionEffect(new PotionEffect(
                            PotionEffectType.SLOW_FALLING,
                            200,
                            0
                    ));
                }

                // =========================
                // 🌪️ WIND CHARGE VISUAL
                // =========================
                int charges = 100;

                for (int i = 0; i < charges; i++) {

                    Location loc = spawn.clone().add(
                            (Math.random() - 0.5) * 3,
                            1,
                            (Math.random() - 0.5) * 3
                    );

                    WindCharge charge = world.spawn(loc, WindCharge.class);

                    Vector randomVel = new Vector(
                            (Math.random() - 0.5) * 3,
                            Math.random() * 2,
                            (Math.random() - 0.5) * 3
                    );

                    charge.setVelocity(randomVel);
                }

                // =========================
                // 📢 START MESSAGE
                // =========================
                Bukkit.broadcastMessage(
                        ChatColor.AQUA + "" + ChatColor.BOLD + "THE GAME HAS BEGUN!"
                );

                cancel();
            }

        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }
}
