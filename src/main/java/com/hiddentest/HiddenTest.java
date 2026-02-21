package com.hiddentest;

import com.hiddentest.commands.GiveCompassCommand;
import com.hiddentest.commands.GiveBellCommand;
import com.hiddentest.commands.GiveStarCommand;
import com.hiddentest.hearts.EggHeart;
import com.hiddentest.items.DetectivesCompass;
import com.hiddentest.items.DragonEgg;
import com.hiddentest.items.ShadowStar;
import com.hiddentest.reveal.HourlyReveal;
import com.hiddentest.reveal.RevealCommand;
import com.hiddentest.reveal.RevealManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class HiddenTest extends JavaPlugin {

    private static HiddenTest instance;

    @Override
    public void onEnable() {
        instance = this;

        // Core listeners
        getServer().getPluginManager().registerEvents(new ProfileManager(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);

        // Hourly reveal system
        getServer().getPluginManager().registerEvents(new HourlyReveal(), this);

        // Items
        getServer().getPluginManager().registerEvents(new DetectivesCompass(this), this);
        getServer().getPluginManager().registerEvents(new BellOfTruth(this), this);
        getServer().getPluginManager().registerEvents(new DragonEgg(this), this);
        getServer().getPluginManager().registerEvents(new ShadowStar(this), this); // ⭐ REGISTERED

        // Hearts
        getServer().getPluginManager().registerEvents(new EggHeart(this), this);

        // Commands
        getCommand("reveal").setExecutor(new RevealCommand());
        getCommand("hide").setExecutor(new RevealCommand());
        getCommand("givecompass").setExecutor(new GiveCompassCommand());
        getCommand("givebell").setExecutor(new GiveBellCommand(this));
        getCommand("givestar").setExecutor(new GiveStarCommand()); // ⭐ REGISTERED

        RevealManager.init();
    }

    public static HiddenTest getInstance() {
        return instance;
    }
}
