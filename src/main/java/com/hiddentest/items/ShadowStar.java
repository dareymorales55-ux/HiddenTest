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

import java.util.ArrayList;
import java.util.List;

public class ShadowStar implements Listener {

    private final HiddenTest plugin;

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

        // Consume item
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
        } else {
            player.getInventory().remove(item);
        }

        // Hide player
        RevealManager.hide(player);

        player.playSound(player.getLocation(),
                Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE,
                1f,
                1f);

        player.sendMessage(ChatColor.GRAY + "You have vanished into the shadows.");
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
        lore.add(ChatColor.GRAY + "Right-click to remove your reveal status");
        lore.add(ChatColor.RED + "Consumed on use"); // 🔥 RED now

        meta.setLore(lore);
        star.setItemMeta(meta);

        return star;
    }
}
