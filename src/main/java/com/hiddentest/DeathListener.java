package com.hiddentest;

import com.hiddentest.reveal.RevealCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class HiddenTest extends JavaPlugin {

    private static HiddenTest instance;

    @Override
    public void onEnable() {
        instance = this;

        getServer().getPluginManager().registerEvents(new ProfileManager(), this);
        getServer().getPluginManager().registerEvents(new DeathListener(), this);

        getCommand("reveal").setExecutor(new RevealCommand());
        getCommand("hide").setExecutor(new RevealCommand());
    }

    public static HiddenTest getInstance() {
        return instance;
    }
}
