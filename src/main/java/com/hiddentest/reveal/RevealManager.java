package com.hiddentest.reveal;

import com.hiddentest.HiddenTest;
import com.hiddentest.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Scoreboard;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RevealManager {

    private static final String TEAM_NAME = "revealed_team";

    // UUID → reveal end time (millis)
    private static final Map<UUID, Long> revealTimers = new HashMap<>();

    static {
        // Start repeating task to monitor glowing
        Bukkit.getScheduler().runTaskTimer(
                HiddenTest.getInstance(),
                RevealManager::tick,
                20L,
                20L
        );
    }

    /* =========================
       PUBLIC METHODS
       ========================= */

    // Infinite reveal (used by command)
    public static void reveal(Player player) {
        reveal(player, -1);
    }

    // Timed reveal (milliseconds)
    public static void reveal(Player player, long durationMillis) {

        ProfileManager.restore(player);
        applyRevealVisuals(player);

        if (durationMillis > 0) {
            revealTimers.put(player.getUniqueId(),
                    System.currentTimeMillis() + durationMillis);
        } else {
            revealTimers.put(player.getUniqueId(), -1L); // infinite
        }
    }

    public static void hide(Player player) {

        revealTimers.remove(player.getUniqueId());
        removeRevealVisuals(player);
        ProfileManager.anonymize(player);
    }

    /* =========================
       CORE LOGIC
       ========================= */

    private static void applyRevealVisuals(Player player) {

        // Apply scoreboard team for red glow
        org.bukkit.scoreboard.Scoreboard scoreboard =
                Bukkit.getScoreboardManager().getMainScoreboard();

        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.registerNewTeam(TEAM_NAME);
            team.setColor(ChatColor.DARK_RED);
        }

        team.addEntry(player.getName());

        // Apply real glowing potion effect
        player.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.GLOWING,
                        Integer.MAX_VALUE,
                        0,
                        false,
                        false,
                        false
                )
        );

        String realName = player.getPlayerProfile().getName();
        player.setDisplayName(ChatColor.DARK_RED + realName);
        player.setPlayerListName(ChatColor.DARK_RED + realName);
    }

    private static void removeRevealVisuals(Player player) {

        org.bukkit.scoreboard.Scoreboard scoreboard =
                Bukkit.getScoreboardManager().getMainScoreboard();

        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team != null) {
            team.removeEntry(player.getName());
        }

        player.removePotionEffect(PotionEffectType.GLOWING);
    }

    /* =========================
       TIMER + MILK DETECTION
       ========================= */

    private static void tick() {

        long now = System.currentTimeMillis();

        for (UUID uuid : revealTimers.keySet().toArray(new UUID[0])) {

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            long end = revealTimers.get(uuid);

            // Timed reveal check
            if (end != -1 && now >= end) {
                hide(player);
                continue;
            }

            // If glowing removed (milk, commands, etc)
            if (!player.hasPotionEffect(PotionEffectType.GLOWING)) {

                player.addPotionEffect(
                        new PotionEffect(
                                PotionEffectType.GLOWING,
                                Integer.MAX_VALUE,
                                0,
                                false,
                                false,
                                false
                        )
                );
            }
        }
    }
}
