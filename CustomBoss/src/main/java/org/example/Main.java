package org.example;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        this.getCommand("spawnboss").setExecutor(new BossLogic(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
