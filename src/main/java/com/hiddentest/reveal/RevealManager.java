package com.hiddentest.reveal;

import com.hiddentest.HiddenTest;
import com.hiddentest.ProfileManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class RevealManager {

    private static final String TEAM_NAME = "revealed_team";

    // ✅ YOUR CUSTOM HEX COLOR
    private static final String REVEAL_COLOR = ChatColor.of("#7E0810").toString();

    // Remaining reveal time in TICKS (-1 = permanent)
    private static final Map<UUID, Integer> revealTimers = new HashMap<>();

    public static void init() {
        Bukkit.getScheduler().runTaskTimer(
                HiddenTest.getInstance(),
                RevealManager::tick,
                1L,
                1L
        );
    }

    /* ================================================= */

    public static void reveal(Player player) {
        reveal(player, -1);
    }

    public static void reveal(Player player, int durationTicks) {

        ProfileManager.restore(player);

        if (durationTicks > 0) {
            revealTimers.put(player.getUniqueId(), durationTicks);
        } else {
            revealTimers.put(player.getUniqueId(), -1);
        }

        applyRevealVisuals(player, durationTicks);
    }

    public static void hide(Player player) {
        revealTimers.remove(player.getUniqueId());
        removeRevealVisuals(player);
        ProfileManager.anonymize(player);
    }

    public static boolean isRevealed(Player player) {
        return revealTimers.containsKey(player.getUniqueId());
    }

    public static boolean isRevealed(UUID uuid) {
        return revealTimers.containsKey(uuid);
    }

    public static int getRemainingTicks(UUID uuid) {
        return revealTimers.getOrDefault(uuid, 0);
    }

    public static void reapplyIfStillRevealed(Player player) {

        UUID uuid = player.getUniqueId();
        if (!revealTimers.containsKey(uuid)) return;

        int remaining = revealTimers.get(uuid);

        if (remaining == 0) {
            hide(player);
            return;
        }

        ProfileManager.restore(player);
        applyRevealVisuals(player, remaining);
    }

    /* ================================================= */

    private static void applyRevealVisuals(Player player, int durationTicks) {

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(TEAM_NAME);

        if (team == null) {
            team = scoreboard.registerNewTeam(TEAM_NAME);

            // ✅ KEEP DEFAULT DARK RED GLOW COLOR
            team.setColor(org.bukkit.ChatColor.DARK_RED);
        }

        team.addEntry(player.getName());

        int glowDuration = (durationTicks == -1)
                ? Integer.MAX_VALUE
                : Math.max(1, durationTicks);

        player.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING,
                glowDuration,
                0,
                false,
                false,
                false
        ));

        String realName = ProfileManager.getRealName(player);

        // ✅ APPLY CUSTOM HEX COLOR TO NAME + TAB
        player.setDisplayName(REVEAL_COLOR + realName);
        player.setPlayerListName(REVEAL_COLOR + realName);
    }

    private static void removeRevealVisuals(Player player) {

        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(TEAM_NAME);

        if (team != null) {
            team.removeEntry(player.getName());
        }

        player.removePotionEffect(PotionEffectType.GLOWING);
    }

    private static void tick() {

        Iterator<Map.Entry<UUID, Integer>> iterator =
                revealTimers.entrySet().iterator();

        while (iterator.hasNext()) {

            Map.Entry<UUID, Integer> entry = iterator.next();
            UUID uuid = entry.getKey();
            int remaining = entry.getValue();

            if (remaining == -1) continue;

            remaining--;

            if (remaining <= 0) {

                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    hide(player);
                }

                iterator.remove();
                continue;
            }

            entry.setValue(remaining);
        }
    }
}
