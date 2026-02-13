package com.hiddentest.reveal;

import com.hiddentest.ProfileManager;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RevealManager {

    private static final Set<UUID> revealed = new HashSet<>();

    public static void reveal(Player player) {
        revealed.add(player.getUniqueId());
        ProfileManager.restore(player);
    }

    public static void hide(Player player) {
        revealed.remove(player.getUniqueId());
        ProfileManager.anonymize(player);
    }

    public static boolean isRevealed(Player player) {
        return revealed.contains(player.getUniqueId());
    }
}
