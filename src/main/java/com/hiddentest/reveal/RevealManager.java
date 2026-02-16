package com.hiddentest.reveal;

import com.hiddentest.HiddenTest;
import com.hiddentest.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RevealManager {

    private static final String TEAM_NAME = "revealed_team";

    // UUID → reveal end time
    private static final Map<UUID, Long> revealTimers = new HashMap<>();

    public static void init() {
        Bukkit.getScheduler().runTaskTimer(
                HiddenTest.getInstance(),
                RevealManager::tick,
                20L,
                20L
        );
    }

    /* ========================= */

    public static void reveal(Player player) {
        reveal(player, -1);
    }

    public static void reveal(Player player, long durationMillis) {

        ProfileManager.restore(player);
        applyRevealVisuals(player);

        if (durationMillis > 0) {
            revealTimers.put(player.getUniqueId(),
                    System.currentTimeMillis() + durationMillis);
        } else {
            revealTimers.put(player.getUniqueId(), -1L);
        }
    }

    public static void hide(Player player) {

        revealTimers.remove(player.getUniqueId());
        removeRevealVisuals(player);
        ProfileManager.anonymize(player);
    }

    // ✅ Added method
    public static boolean isRevealed(Player player) {
        return revealTimers.containsKey(player.getUniqueId());
    }

    /* ========================= */

    private static void applyRevealVisuals(Player player) {

        Scoreboard scoreboard =
                Bukkit.getScoreboardManager().getMainScoreboard();

        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.registerNewTeam(TEAM_NAME);
            team.setColor(ChatColor.DARK_RED);
        }

        team.addEntry(player.getName());

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

        String realName = ProfileManager.getRealName(player);

        player.setDisplayName(ChatColor.DARK_RED + realName);
        player.setPlayerListName(ChatColor.DARK_RED + realName);
    }

    private static void removeRevealVisuals(Player player) {

        Scoreboard scoreboard =
                Bukkit.getScoreboardManager().getMainScoreboard();

        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team != null) {
            team.removeEntry(player.getName());
        }

        player.removePotionEffect(PotionEffectType.GLOWING);
    }

    private static void tick() {

        long now = System.currentTimeMillis();

        for (UUID uuid : revealTimers.keySet().toArray(new UUID[0])) {

            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;

            long end = revealTimers.get(uuid);

            if (end != -1 && now >= end) {
                hide(player);
                continue;
            }

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
