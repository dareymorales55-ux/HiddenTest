package com.hiddentest;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class EasyRecipes {

    private final JavaPlugin plugin;

    public EasyRecipes(JavaPlugin plugin) {
        this.plugin = plugin;
        registerRecipes();
    }

    private void registerRecipes() {

        // =========================
        // GOLDEN APPLE (Cheaper)
        // =========================
        ItemStack goldenApple = new ItemStack(Material.GOLDEN_APPLE);

        ShapedRecipe gappleRecipe = new ShapedRecipe(
                new NamespacedKey(plugin, "easy_golden_apple"),
                goldenApple
        );

        gappleRecipe.shape(
                " G ",
                "GAG",
                " G "
        );

        gappleRecipe.setIngredient('G', Material.GOLD_INGOT);
        gappleRecipe.setIngredient('A', Material.APPLE);

        Bukkit.addRecipe(gappleRecipe);


        // =========================
        // COBWEB
        // =========================
        ItemStack cobweb = new ItemStack(Material.COBWEB);

        ShapedRecipe cobwebRecipe = new ShapedRecipe(
                new NamespacedKey(plugin, "easy_cobweb"),
                cobweb
        );

        cobwebRecipe.shape(
                "S S",
                " S ",
                "S S"
        );

        cobwebRecipe.setIngredient('S', Material.STRING);

        Bukkit.addRecipe(cobwebRecipe);


        // =========================
        // ANVIL (Cheap Version)
        // =========================
        ItemStack anvil = new ItemStack(Material.ANVIL);

        ShapedRecipe anvilRecipe = new ShapedRecipe(
                new NamespacedKey(plugin, "easy_anvil"),
                anvil
        );

        anvilRecipe.shape(
                "III",
                " I ",
                "III"
        );

        anvilRecipe.setIngredient('I', Material.IRON_INGOT);

        Bukkit.addRecipe(anvilRecipe);


        // =========================
        // APPLE (From Leaves + Redstone) - Gives 2 Apples
        // =========================
        ItemStack apple = new ItemStack(Material.APPLE, 2);

        ShapedRecipe appleRecipe = new ShapedRecipe(
                new NamespacedKey(plugin, "easy_apple"),
                apple
        );

        appleRecipe.shape(
                " L ",
                "LRL",
                " L "
        );

        // Allow ANY type of leaves
        RecipeChoice.MaterialChoice leavesChoice = new RecipeChoice.MaterialChoice(Arrays.asList(
                Material.OAK_LEAVES,
                Material.SPRUCE_LEAVES,
                Material.BIRCH_LEAVES,
                Material.JUNGLE_LEAVES,
                Material.ACACIA_LEAVES,
                Material.DARK_OAK_LEAVES,
                Material.MANGROVE_LEAVES,
                Material.CHERRY_LEAVES,
                Material.AZALEA_LEAVES,
                Material.FLOWERING_AZALEA_LEAVES
        ));

        appleRecipe.setIngredient('L', leavesChoice);
        appleRecipe.setIngredient('R', Material.REDSTONE);

        Bukkit.addRecipe(appleRecipe);
    }
}
