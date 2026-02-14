package com.hiddentest;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
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
            "ewogICJ0aW1lc3RhbXAiIDogMTY5MTIxODk2MDIxMiwKICAicHJvZmlsZUlkIiA6ICJiNGJmZDZhNmRiZGQ0MDg2ODRhYmIzYzlmNDQyNmRiYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJWZXJzYWNlNjciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmYxZWZhN2YxODE3MTE3ZDZlMjQyOGY1YjM4OGEzNWI5MzcwMWEwN2ViOGUzNWEzZmFkY2ZhZmVjMmRjNzdlIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";

    private static final String ANON_SIGNATURE =
            "lBwv7s/JkrSLa5sHzjx6JUBz7ALWa2qgW/2ugvktSxY8fD4gPYuMgvrkhJ3I9YB+87H2PMPwsW8hjysz42cSvTl8G0MbnH4PpEZIiiw7be25wSaVSeTuN2E1WQeKGwkJ/O1CXniED628ElBY1WyGkC7Dtea5pKHVkud3p+3LG6ZiTOGAS1QkMV9Iu7EZ9ZvRnhf7F8bd5EolTEWDtlAuxNuS1E53xtlIRPaHnMZszA31HhoeeRRPudS1w6uLMwcaO8hGnxYxQKVauZ/yv8km/Bi48cUGD2j7TLQTw66UzniiDkLYfiDHNx3HtPCVB7ICrPqFVa82zcA4RnJiqyeKEuyoRiwcXv9HDSTdPG8PrTLcPdDNvOqN0SmXHgGZhUIMDKv3QXEHOh6SpSBF5u54bkHr/MEXxhO6WachRDv+/hA8OklOI99V2fElMl+3A5v5v+TqgVsbJTL7fQG2nWsGAqhf7JddnxEnMxkCh1At9CiEnEwzUq4/XHGO+l6iVxZMIHyTzZlQbZfc1kOS1xIBSqR+TU4dNPdQ39hKqbQGh+eDjL86Ql7Chf3YLX957DPAft6Pccj9oGMsSsJ86/y7xMcqar2QVocsAgK2hipyuQ34mMd2JHYvkp/3siUy3pXDscN4c06l3FtmNQ/WgvRqRyulBoc9Ym3R9+ZZzQglUnE=";

    /* =========================
       JOIN HANDLER
       ========================= */

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
       ANONYMIZE (FIXED SKIN)
       ========================= */

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

    /* =========================
       RESTORE REAL PROFILE
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
