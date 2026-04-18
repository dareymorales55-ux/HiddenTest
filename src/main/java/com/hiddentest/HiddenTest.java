package com.hiddentest;

import com.hiddentest.commands.GiveCompassCommand;
import com.hiddentest.commands.GiveBellCommand;
import com.hiddentest.hearts.EggHeart;
import com.hiddentest.items.DetectivesCompass;
import com.hiddentest.items.DragonEgg;
import com.hiddentest.items.BellOfTruth;
import com.hiddentest.items.BookOfNames;
// ❌ removed MaceAbilities import
import com.hiddentest.mobs.UnknownChicken;
import com.hiddentest.reveal.HourlyReveal;
import com.hiddentest.reveal.RevealCommand;
import com.hiddentest.reveal.RevealManager;
import com.hiddentest.world.EndLightning;
import com.hiddentest.world.EndReform;
import com.hiddentest.world.ServerStart;

import org.bukkit.plugin.java.JavaPlugin;

public final class HiddenTest extends JavaPlugin {

    private static HiddenTest instance;

    @Override
    public void onEnable() {

        instance = this;

        // =========================
        // CORE LISTENERS
        // =========================
        getServer().getPluginManager().registerEvents(new ProfileManager(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);

        // =========================
        // HOURLY REVEAL SYSTEM
        // =========================
        getServer().getPluginManager().registerEvents(new HourlyReveal(), this);

        // =========================
        // ITEMS
        // =========================
        getServer().getPluginManager().registerEvents(new DetectivesCompass(this), this);
        getServer().getPluginManager().registerEvents(new BellOfTruth(this), this);
        getServer().getPluginManager().registerEvents(new DragonEgg(this), this);
        // ❌ removed MaceAbilities registration

        // =========================
        // HEART SYSTEM
        // =========================
        getServer().getPluginManager().registerEvents(new EggHeart(this), this);

        // =========================
        // UNKNOWN CHICKEN BOSS
        // =========================
        UnknownChicken unknownChicken = new UnknownChicken(this);
        getServer().getPluginManager().registerEvents(unknownChicken, this);
        getCommand("summonchicken").setExecutor(unknownChicken);

        // =========================
        // COMMANDS
        // =========================
        getCommand("reveal").setExecutor(new RevealCommand());
        getCommand("hide").setExecutor(new RevealCommand());
        getCommand("givecompass").setExecutor(new GiveCompassCommand());
        getCommand("givebell").setExecutor(new GiveBellCommand(this));
        getCommand("givebook").setExecutor(new BookOfNames());

        // SERVER START COMMAND
        getCommand("serverstart").setExecutor(new ServerStart(this));

        // =========================
        // EASY RECIPES
        // =========================
        new EasyRecipes(this);

        // =========================
        // INIT REVEAL MANAGER
        // =========================
        RevealManager.init();

        // =========================
        // END SYSTEMS
        // =========================
        new EndReform(this);
        new EndLightning(this);
    }

    public static HiddenTest getInstance() {
        return instance;
    }
}
