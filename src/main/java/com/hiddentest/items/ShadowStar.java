package com.hiddentest.items;

import com.hiddentest.HiddenTest;
import com.hiddentest.reveal.RevealManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;

import java.util.*;

public class ShadowStar implements Listener {

    private final HiddenTest plugin;

    // 15 minutes in milliseconds
    private static final long PROTECTION_TIME = 15 * 60 * 1000L;

    // Protected players map
    private static final Map<UUID, Long> protection = new HashMap<>();

    public ShadowStar(HiddenTest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {

        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
            event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (!event.hasItem()) return;

        ItemStack item = event.getItem();
        if (!isShadowStar(item)) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Consume item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }

        // Remove reveal immediately
        RevealManager.hide(player);

        // Apply protection
        protection.put(uuid, System.currentTimeMillis() + PROTECTION_TIME);

        player.playSound(player.getLocation(),
                Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,
                1f,
                1f);

        player.sendMessage(ChatColor.GRAY + "You are protected from reveals for 15 minutes.");
    }

    public static boolean isProtected(Player player) {
        Long expiry = protection.get(player.getUniqueId());
        if (expiry == null) return false;

        if (System.currentTimeMillis() > expiry) {
            protection.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    private boolean isShadowStar(ItemStack item) {

        if (item == null) return false;
        if (item.getType() != Material.NETHER_STAR) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) return false;

        return meta.getDisplayName().equals(
                ChatColor.GRAY.toString() +
                ChatColor.BOLD +
                "Shadow Star"
        );
    }

    public static ItemStack createShadowStar() {

        ItemStack star = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = star.getItemMeta();

        meta.setDisplayName(ChatColor.GRAY.toString() + ChatColor.BOLD + "Shadow Star");

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Right-click to remove reveal");
        lore.add(ChatColor.GRAY + "Grants 15 minutes of reveal immunity");
        lore.add(ChatColor.RED + "Consumed on use");

        meta.setLore(lore);
        star.setItemMeta(meta);

        return star;
    }
}
