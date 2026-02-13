package com.hiddentest.reveal;

import com.hiddentest.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Scoreboard;
import org.bukkit.Team;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RevealManager {

    private static final Set<UUID> revealed = new HashSet<>();
    private static final String TEAM_NAME = "revealed_team";

    /* =========================
       REVEAL PLAYER
       ========================= */

    public static void reveal(Player player) {

        revealed.add(player.getUniqueId());

        // Restore real profile first
        ProfileManager.restore(player);

        // Apply glowing + red name
        applyRevealVisuals(player);
    }

    /* =========================
       HIDE PLAYER
       ========================= */

    public static void hide(Player player) {

        revealed.remove(player.getUniqueId());

        removeRevealVisuals(player);

        ProfileManager.anonymize(player);
    }

    /* =========================
       VISUAL LOGIC
       ========================= */

    private static void applyRevealVisuals(Player player) {

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

        Team team = scoreboard.getTeam(TEAM_NAME);
        if (team == null) {
            team = scoreboard.registerNewTeam(TEAM_NAME);
            team.setColor(ChatColor.DARK_RED);
        }

        team.addEntry(player.getName());

        player.setGlowing(true);

        String realName = player.getPlayerProfile().getName();

        player.setDisplayName(ChatColor.DARK_RED + realName);
        player.setPlayerListName(ChatColor.DARK_RED + realName);
    }

    private static void removeRevealVisuals(Player player) {

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(TEAM_NAME);

        if (team != null) {
            team.removeEntry(player.getName());
        }

        player.setGlowing(false);
    }

    public static boolean isRevealed(Player player) {
        return revealed.contains(player.getUniqueId());
    }
}
