package com.hiddentest.items;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class UnknownTotem implements Listener {

    private static final String COLOR =
            ChatColor.of("#FFA500").toString();

    public static ItemStack createUnknownTotem() {

        ItemStack item =
                new ItemStack(Material.TOTEM_OF_UNDYING);

        ItemMeta meta =
                item.getItemMeta();

        if (meta != null) {

            meta.setDisplayName(
                    COLOR +
                    "" +
                    ChatColor.BOLD +
                    "Unknown Totem"
            );

            List<String> lore =
                    new ArrayList<>();

            lore.add(
                    ChatColor.GRAY +
                    "Hold before death to prevent being caught"
            );

            lore.add(
                    ChatColor.GRAY +
                    "Consumed on use"
            );

            lore.add(
                    ChatColor.RED +
                    "Does not prevent death"
            );

            meta.setLore(lore);

            Enchantment enchant =
                    Registry.ENCHANTMENT.get(
                            NamespacedKey.minecraft("unbreaking")
                    );

            if (enchant != null) {
                meta.addEnchant(enchant, 1, true);
            }

            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

            item.setItemMeta(meta);
        }

        return item;
    }

    // =========================
    // PREVENT VANILLA TOTEM SAVE
    // =========================
    @EventHandler
    public void onResurrect(EntityResurrectEvent event) {

        if (!(event.getEntity() instanceof Player player))
            return;

        EquipmentSlot slot = event.getHand();

        ItemStack item = player.getInventory().getItem(slot);

        if (isUnknownTotem(item)) {

            // cancel vanilla "death prevention"
            event.setCancelled(true);

            // consume your custom totem
            consumeTotem(player);
        }
    }

    public static boolean isUnknownTotem(ItemStack item) {

        if (item == null)
            return false;

        if (item.getType() != Material.TOTEM_OF_UNDYING)
            return false;

        if (!item.hasItemMeta())
            return false;

        if (!item.getItemMeta().hasDisplayName())
            return false;

        return item.getItemMeta()
                .getDisplayName()
                .equals(
                        COLOR +
                        "" +
                        ChatColor.BOLD +
                        "Unknown Totem"
                );
    }

    public static boolean hasUnknownTotem(Player player) {

        return isUnknownTotem(player.getInventory().getItemInMainHand())
                || isUnknownTotem(player.getInventory().getItemInOffHand());
    }

    public static void consumeTotem(Player player) {

        ItemStack main = player.getInventory().getItemInMainHand();

        if (isUnknownTotem(main)) {
            player.getInventory().setItemInMainHand(null);
            return;
        }

        ItemStack off = player.getInventory().getItemInOffHand();

        if (isUnknownTotem(off)) {
            player.getInventory().setItemInOffHand(null);
        }
    }
}
