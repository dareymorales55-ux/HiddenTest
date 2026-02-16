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

        Bukkit.getScheduler().runTaskLater(
                HiddenTest.getInstance(),
                HourlyReveal::executeReveal,
                5 * 20L
        );
    }

    private static void executeReveal() {

        List<Player> eligible = new ArrayList<>();

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!RevealManager.isRevealed(p)) {
                eligible.add(p);
            }
        }

        if (eligible.isEmpty()) {
            Bukkit.broadcastMessage(
                    ChatColor.RED.toString() +
                    ChatColor.BOLD +
                    "All players are already revealed, waiting for available candidates."
            );

            waitForEligiblePlayers();
            return;
        }

        // Play respawn anchor charge sound
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 1f);
        }

        Collections.shuffle(eligible);

        int revealCount = eligible.size() >= 2 ? random.nextInt(2) + 1 : 1;

        int minutes = 5 + random.nextInt(11); // 5-15 inclusive
        long durationMillis = minutes * 60L * 1000L;

        for (int i = 0; i < revealCount && i < eligible.size(); i++) {
            Player target = eligible.get(i);

            RevealManager.reveal(target, durationMillis);

            target.sendMessage(
                    ChatColor.DARK_RED +
                    "You have been revealed for " +
                    minutes +
                    " minutes."
            );

            Bukkit.broadcastMessage(
                    ChatColor.RED +
                    target.getName() +
                    " has been revealed for " +
                    minutes +
                    " minutes."
            );
        }

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

    private static void waitForEligiblePlayers() {

        Bukkit.getScheduler().runTaskTimer(
                HiddenTest.getInstance(),
                task -> {

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (!RevealManager.isRevealed(p)) {
                            task.cancel();
                            executeReveal();
                            return;
                        }
                    }

                },
                20L,
                20L
        );
    }

    private static void scheduleNextCycle() {

        Bukkit.getScheduler().runTaskLater(
                HiddenTest.getInstance(),
                HourlyReveal::startCycle,
                60 * 60 * 20L // 1 hour
        );
    }
}
