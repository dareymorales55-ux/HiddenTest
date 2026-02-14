package com.hiddentest;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProfileManager implements Listener {

    private static final Map<UUID, PlayerProfile> realProfiles = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        cacheRealProfile(player);
        anonymize(player);

        event.setJoinMessage(ChatColor.YELLOW + "Player has joined");
    }

    /* =========================
       REAL NAME ACCESSOR
       ========================= */

    public static String getRealName(Player player) {
        PlayerProfile real = realProfiles.get(player.getUniqueId());
        if (real == null) return player.getName();
        return real.getName();
    }

    /* =========================
       STORAGE
       ========================= */

    public static void cacheRealProfile(Player player) {
        realProfiles.put(player.getUniqueId(), player.getPlayerProfile());
    }

    /* =========================
       ANONYMIZE
       ========================= */

    public static void anonymize(Player player) {

        PlayerProfile anonymous =
                Bukkit.createProfile(UUID.randomUUID(), "Player");

        player.setPlayerProfile(anonymous);
        player.setDisplayName("Player");
        player.setPlayerListName("Player");

        refreshPlayer(player);
    }

    /* =========================
       RESTORE
       ========================= */

    public static void restore(Player player) {

        PlayerProfile real = realProfiles.get(player.getUniqueId());
        if (real == null) return;

        player.setPlayerProfile(real);
        player.setDisplayName(real.getName());
        player.setPlayerListName(real.getName());

        refreshPlayer(player);
    }

    /* =========================
       CLIENT REFRESH
       ========================= */

    private static void refreshPlayer(Player player) {
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.hidePlayer(HiddenTest.getInstance(), player);
            p.showPlayer(HiddenTest.getInstance(), player);
        });
    }
}
