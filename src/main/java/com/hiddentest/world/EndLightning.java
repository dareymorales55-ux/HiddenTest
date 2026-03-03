package com.hiddentest.world;

import com.hiddentest.HiddenTest;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EndLightning {

    private final HiddenTest plugin;
    private final Random random = new Random();

    private static final int RADIUS = 200;
    private static final int STRIKES = 5;
    private static final double MIN_DISTANCE = 25.0;

    public EndLightning(HiddenTest plugin) {
        this.plugin = plugin;
        startTask();
    }

    private void startTask() {

        new BukkitRunnable() {
            @Override
            public void run() {

                World endWorld = Bukkit.getWorlds().stream()
                        .filter(w -> w.getEnvironment() == World.Environment.THE_END)
                        .findFirst()
                        .orElse(null);

                if (endWorld == null) return;

                List<Location> strikeLocations = new ArrayList<>();

                int attempts = 0;

                while (strikeLocations.size() < STRIKES && attempts < 100) {
                    attempts++;

                    double x = random.nextDouble() * (RADIUS * 2) - RADIUS;
                    double z = random.nextDouble() * (RADIUS * 2) - RADIUS;

                    Location loc = new Location(endWorld, x, 0, z);

                    // Make sure inside circle radius
                    if (loc.distanceSquared(new Location(endWorld, 0, 0, 0)) > RADIUS * RADIUS)
                        continue;

                    int y = endWorld.getHighestBlockYAt(loc);
                    loc.setY(y);

                    boolean tooClose = false;

                    for (Location existing : strikeLocations) {
                        if (existing.distance(loc) < MIN_DISTANCE) {
                            tooClose = true;
                            break;
                        }
                    }

                    if (!tooClose) {
                        strikeLocations.add(loc);
                    }
                }

                for (Location loc : strikeLocations) {
                    spawnLightningEffect(loc);
                }
            }

        }.runTaskTimer(plugin, 0L, 20L * 20); // every 20 seconds
    }

    private void spawnLightningEffect(Location loc) {

        World world = loc.getWorld();
        if (world == null) return;

        // ⚡ Lightning
        world.strikeLightning(loc);

        // 🌸 Pink -> Purple transition
        Particle.DustTransition dust = new Particle.DustTransition(
                Color.fromRGB(255, 105, 180), // pink
                Color.fromRGB(160, 32, 240),  // purple
                1.5f
        );

        new BukkitRunnable() {

            int ticks = 0;

            @Override
            public void run() {

                if (ticks >= 60) { // 3 seconds
                    cancel();
                    return;
                }

                world.spawnParticle(
                        Particle.DUST_COLOR_TRANSITION,
                        loc.clone().add(0, 0.2, 0),
                        20,
                        1, 0.3, 1,
                        0,
                        dust
                );

                world.spawnParticle(
                        Particle.PORTAL,
                        loc.clone().add(0, 0.5, 0),
                        30,
                        1.5, 0.5, 1.5,
                        0.1
                );

                ticks += 5;

            }

        }.runTaskTimer(plugin, 0L, 5L);
    }
}
