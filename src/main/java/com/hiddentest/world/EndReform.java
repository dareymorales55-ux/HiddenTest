package com.hiddentest.world;

import com.hiddentest.HiddenTest;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class EndReform {

    private final HiddenTest plugin;
    private final Random random = new Random();

    private static final int RADIUS = 300;
    private static final int MAX_Y = 100; // Only scan main island height

    public EndReform(HiddenTest plugin) {
        this.plugin = plugin;
        runOnceAsyncSafe();
    }

    private void runOnceAsyncSafe() {

        FileConfiguration config = plugin.getConfig();

        if (config.getBoolean("endReformCompleted", false)) {
            return;
        }

        World endWorld = Bukkit.getWorlds().stream()
                .filter(w -> w.getEnvironment() == World.Environment.THE_END)
                .findFirst()
                .orElse(null);

        if (endWorld == null) return;

        Bukkit.getLogger().info("Running ONE-TIME End Reform (Lag Optimized)...");

        new BukkitRunnable() {

            int x = -RADIUS;
            int z = -RADIUS;
            int blocksPerTick = 4000; // Controls lag (lower = safer)

            @Override
            public void run() {

                int processed = 0;

                while (x <= RADIUS) {

                    while (z <= RADIUS) {

                        if ((x * x + z * z) <= RADIUS * RADIUS) {

                            for (int y = 0; y <= MAX_Y; y++) {

                                Block block = endWorld.getBlockAt(x, y, z);

                                // 🟣 20% Obsidian → Crying Obsidian
                                if (block.getType() == Material.OBSIDIAN) {
                                    if (random.nextDouble() <= 0.20) {
                                        block.setType(Material.CRYING_OBSIDIAN, false);
                                    }
                                }

                                // 🟡 40% End Stone → End Stone Bricks
                                if (block.getType() == Material.END_STONE) {
                                    if (random.nextDouble() <= 0.40) {
                                        block.setType(Material.END_STONE_BRICKS, false);
                                    }
                                }

                                processed++;
                                if (processed >= blocksPerTick) {
                                    z++;
                                    return; // Continue next tick
                                }
                            }
                        }

                        z++;
                    }

                    z = -RADIUS;
                    x++;
                }

                // ✅ Finished
                config.set("endReformCompleted", true);
                plugin.saveConfig();

                Bukkit.getLogger().info("End Reform complete. It will never run again.");
                cancel();
            }

        }.runTaskTimer(plugin, 0L, 1L); // 1 tick interval
    }
}
