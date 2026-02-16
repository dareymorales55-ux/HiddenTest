package com.hiddentest;

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

        // Commands
        getCommand("reveal").setExecutor(new RevealCommand());
        getCommand("hide").setExecutor(new RevealCommand());

        // Start RevealManager timer system
        RevealManager.init();
    }

    public static HiddenTest getInstance() {
        return instance;
    }
}
