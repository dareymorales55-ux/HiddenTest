package com.hiddentest.reveal;

import com.hiddentest.HiddenTest;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.*;

public class HourlyReveal implements Listener {

    private static boolean started = false;
    private static final Random random = new Random();

    @EventHandler
    public void onFirstJoin(PlayerJoinEvent event) {
        if (started) return;

        if (Bukkit.getOnlinePlayers().size() == 1) {
            started = true;
            startCycle();
        }
    }

    private static void startCycle() {

        // 10 minute delay before warning
        Bukkit.getScheduler().runTaskLater(
                HiddenTest.getInstance(),
                HourlyReveal::warningPhase,
                10 * 60 * 20L // 10 minutes
        );
    }

    private static void warningPhase() {

        Bukkit.broadcastMessage(
                ChatColor.RED.toString() +
                ChatColor.BOLD +
                "Player(s) will be revealed promptly."
        );

        // Wait 5 seconds after warning
        Bukkit.getScheduler().runTaskLater(
                HiddenTest.getInstance(),
                HourlyReveal::executeReveal,
                5 * 20L
        );
    }

    private static void executeReveal() {

        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        if (online.isEmpty()) {
            scheduleNextCycle();
            return;
        }

        // Play respawn anchor charge sound to all players
        for (Player p : online) {
            p.playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);
        }

        List<Player> players = new ArrayList<>(online);
        Collections.shuffle(players);

        int revealCount = players.size() >= 2 ? random.nextInt(2) + 1 : 1;

        int minutes = 5 + random.nextInt(11); // 5-15 inclusive
        long durationMillis = minutes * 60L * 1000L;

        for (int i = 0; i < revealCount && i < players.size(); i++) {
            Player target = players.get(i);

            RevealManager.reveal(target, durationMillis);

            // Personal message
            target.sendMessage(
                    ChatColor.DARK_RED +
                    "You have been revealed for " +
                    minutes +
                    " minutes."
            );

            // Global message
            Bukkit.broadcastMessage(
                    ChatColor.RED +
                    target.getName() +
                    " has been revealed for " +
                    minutes +
                    " minutes."
            );
        }

        // Schedule end sound when reveal expires
        Bukkit.getScheduler().runTaskLater(
                HiddenTest.getInstance(),
                () -> {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.playSound(p.getLocation(), Sound.BLOCK_BREWING_STAND_BREW, 1f, 1f);
                    }
                    scheduleNextCycle();
                },
                minutes * 60 * 20L
        );
    }

    private static void scheduleNextCycle() {

        // Wait 1 hour before restarting cycle
        Bukkit.getScheduler().runTaskLater(
                HiddenTest.getInstance(),
                HourlyReveal::startCycle,
                60 * 60 * 20L // 1 hour
        );
    }
}
