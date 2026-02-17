package com.hiddentest.hearts;

import com.hiddentest.HiddenTest;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EggHeart implements Listener {

    private final HiddenTest plugin;

    private File file;
    private FileConfiguration data;

    private static final double MAX_HEALTH = 40.0; // 20 hearts
    private static final double BASE_HEALTH = 20.0;

    public EggHeart(HiddenTest plugin) {
        this.plugin = plugin;
        setupFile();
        startInventoryCheckTask();
    }

    /* =======================================================
       FILE SETUP
       ======================================================= */

    private void setupFile() {

        file = new File(plugin.getDataFolder(), "HeartLogger.yml");

        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        data = YamlConfiguration.loadConfiguration(file);
    }

    private void saveFile() {
        try {
            data.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* =======================================================
       HEART STEAL ON KILL
       ======================================================= */

    @EventHandler
    public void onKill(PlayerDeathEvent event) {

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer == null) return;
        if (!hasDragonEgg(killer)) return;

        UUID killerUUID = killer.getUniqueId();
        UUID victimUUID = victim.getUniqueId();

        String path = killerUUID.toString() + ".victims";

        List<String> victims = data.getStringList(path);

        if (victims.contains(victimUUID.toString())) {
            return; // Already stole from this player
        }

        victims.add(victimUUID.toString());
        data.set(path, victims);

        int currentHearts = data.getInt(killerUUID.toString() + ".hearts");
        currentHearts++;

        if ((BASE_HEALTH + currentHearts * 2) > MAX_HEALTH) {
            currentHearts = (int)((MAX_HEALTH - BASE_HEALTH) / 2);
        }

        data.set(killerUUID.toString() + ".hearts", currentHearts);

        saveFile();

        applyHearts(killer);

        killer.sendMessage(ChatColor.LIGHT_PURPLE + "You stole a heart from " + victim.getName() + "!");
    }

    /* =======================================================
       APPLY / REMOVE HEARTS
       ======================================================= */

    private void applyHearts(Player player) {

        UUID uuid = player.getUniqueId();
        int bonusHearts = data.getInt(uuid.toString() + ".hearts");

        double newMax = BASE_HEALTH + (bonusHearts * 2);
        if (newMax > MAX_HEALTH) newMax = MAX_HEALTH;

        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newMax);
    }

    private void removeHearts(Player player) {
        player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(BASE_HEALTH);
    }

    /* =======================================================
       INVENTORY CHECK TASK
       ======================================================= */

    private void startInventoryCheckTask() {

        Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            for (Player player : Bukkit.getOnlinePlayers()) {

                if (hasDragonEgg(player)) {
                    applyHearts(player);
                } else {
                    removeHearts(player);
                }
            }

        }, 0L, 40L); // every 2 seconds
    }

    /* =======================================================
       JOIN EVENT (restore hearts on join)
       ======================================================= */

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {

            if (hasDragonEgg(player)) {
                applyHearts(player);
            } else {
                removeHearts(player);
            }

        }, 20L);
    }

    /* =======================================================
       EGG CHECK
       ======================================================= */

    private boolean hasDragonEgg(Player player) {

        for (ItemStack item : player.getInventory().getContents()) {

            if (item == null) continue;
            if (item.getType() != Material.DRAGON_EGG) continue;
            if (!item.hasItemMeta()) continue;

            if (item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(
                        ChatColor.LIGHT_PURPLE.toString() +
                        ChatColor.BOLD +
                        "Dragon Egg")) {
                return true;
            }
        }

        return false;
    }
}
