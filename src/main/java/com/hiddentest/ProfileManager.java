package com.hiddentest;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.hiddentest.reveal.RevealManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProfileManager implements Listener {

    private static final Map<UUID, PlayerProfile> realProfiles = new HashMap<>();

    /* =========================
       FIXED ANONYMIZED SKIN DATA
       ========================= */

    private static final String ANON_TEXTURE =
            "ewogICJ0aW1lc3RhbXAiIDogMTc3MjM0NDA1MzkzNiwKICAicHJvZmlsZUlkIiA6ICJhYjNkNTgwMjVkOWM0NTcyODNkNTFlYTcwYTY4N2U1NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJsdWN5X2ludGhlc2t5XyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lNjM2YTY4YjAzYzg3YWIxYWFhNWMyNGY1YzNjMTMwMGVjNGM5NmVkY2RmZDc2NTAyNTU5NjdkZTEzNmJlZWFlIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";

    private static final String ANON_SIGNATURE =
            "Aw6ZW+4R3p+Hdl0DB3sbgmSQiQjpH4F9Gomlg6AQKvtCROrZ4TRv3yUFCtIx76TH9GD1N+CnNuaJadfRqEYKkG+uRRyaK11OxBmOZbhTWNaW5y5zMAZdEBtK7ak6Jtx4a5+XJsWc3J7aMIDeeOSBw/E/EUIlV3bGX89wMYGDM2rs9cJkz+NLdvVNcsaXifuthWi5TIEpVRaJOTDNaKo1ucdxd4nxrnga0MWT0TXxbGFH8aLeg9izYOJvCEVA6NGakXRA9P69ZM8tnScIfzdHGvqR7yBlpv6rpbVuTEJHdhhXjQVSnqcDl7g9BGnyHHY03PU/iMJyLISRp6qztTf/E3fKXJ9H2Dp4vvGdE5RrRGE8s0QQ/smpAt4UERNC7covk+SQygxRYSBss9awcZiGHP0wFcRO/+c9y11HgW6WaMVacW4DRSfd914S0NbqrayInXenfOVHQL1jorq3RpFcq+tqJ/AF+4kB/ybinvy2tDiprXqiOZcbliFxn/sCfg9wzJHIk2MCgov75BSyfWauGsKfHDyGyqObqim/pyV9/WjpxndwEeOft3SGdCigGagDUWCpB+xDfoorh+fJdVigdFT1ZjRx3M7w+AeGVoF70hPUKeArVbu9j89UKlrIp/7szdNMQrhgOrsXlvFzkNfo1Kudam84rmSw0D2Vks9o6B8=";

    /* =========================
       JOIN HANDLER
       ========================= */

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        cacheRealProfile(player);

        // 🔥 If still revealed, reapply reveal instead of anonymizing
        if (RevealManager.isRevealed(player.getUniqueId())) {
            RevealManager.reapplyIfStillRevealed(player);
        } else {
            anonymize(player);
        }

        event.setJoinMessage(ChatColor.YELLOW + "Player has joined");
    }

    /* ========================= */

    public static String getRealName(Player player) {
        PlayerProfile real = realProfiles.get(player.getUniqueId());
        if (real == null) return player.getName();
        return real.getName();
    }

    public static void cacheRealProfile(Player player) {
        realProfiles.put(player.getUniqueId(), player.getPlayerProfile());
    }

    public static void anonymize(Player player) {

        PlayerProfile anonymous =
                Bukkit.createProfileExact(player.getUniqueId(), "Player");

        anonymous.setProperties(List.of(
                new ProfileProperty("textures", ANON_TEXTURE, ANON_SIGNATURE)
        ));

        player.setPlayerProfile(anonymous);
        player.setDisplayName("Player");
        player.setPlayerListName("Player");

        refreshPlayer(player);
    }

    public static void restore(Player player) {

        PlayerProfile real = realProfiles.get(player.getUniqueId());
        if (real == null) return;

        player.setPlayerProfile(real);
        player.setDisplayName(real.getName());
        player.setPlayerListName(real.getName());

        refreshPlayer(player);
    }

    private static void refreshPlayer(Player player) {
        Bukkit.getOnlinePlayers().forEach(p -> {
            p.hidePlayer(HiddenTest.getInstance(), player);
            p.showPlayer(HiddenTest.getInstance(), player);
        });
    }
}
