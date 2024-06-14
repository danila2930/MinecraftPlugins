package org.example;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new TridentListener(this), this);
        getServer().getPluginManager().registerEvents(new CrystalBowListener(this), this);
        getServer().getPluginManager().registerEvents(new ForestAxeListener(this), this);
        getServer().getPluginManager().registerEvents(new InfBlock(this), this);
        getServer().getPluginManager().registerEvents(new MinecartSpeedListener(), this); // Регистрация слушателя для вагонеток



//        // Регистрация команды для спавна босса
//        this.getCommand("spawnShadowLord").setExecutor(new ShadowLordCommand(this)); // Передача ссылки на плагин
//        // Регистрация слушателя для обработки событий
//        Bukkit.getPluginManager().registerEvents(new ShadowLordSkills(this), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
