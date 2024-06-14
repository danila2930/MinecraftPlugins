package org.example;

import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    private static Main instance;
    private JSONArray playersArray = new JSONArray(); // Инициализация вашего массива игроков
    private File dataFile = new File(getDataFolder(), "playersData.json");
    private File rulesFile = new File(getDataFolder(), "rulesData.json");
    private File regionsFile = new File(getDataFolder(), "regionsData.json");
    private File playTimeFile = new File(getDataFolder(), "playTimeData.json");

    @Override
    public void onEnable() {
        instance = this; // Инициализация instance
        getLogger().info("Plugin enabled!");

        // Создание файлов данных
        createDataFileIfNotExists();
        createRuleFileIfNotExists();
        createRegionsFileIfNotExists();
        createPlayTimeFileIfNotExists();

        RuleRider ruleRider = new RuleRider();
        this.getCommand("warning").setExecutor(new warning());
        this.getCommand("sorryOrb").setExecutor(new sorryOrb());
        this.getCommand("topacv").setExecutor(new TopAcv());
        this.getCommand("nextpage").setExecutor(ruleRider.new nextpage());
        this.getCommand("trusted").setExecutor(new SetTruster(this));

        PlayerPlayTime playerPlayTime = new PlayerPlayTime(this);
        playerPlayTime.runTaskTimer(this, 0L, 20L); // 20L означает выполнение задачи каждую секунду

        RegionManager regionManager = new RegionManager(this, playerPlayTime);
        getServer().getPluginManager().registerEvents(regionManager, this);

        getServer().getScheduler().runTaskTimer(this, regionManager::loadRegions, 0L, 200L); // 200L = 10 секунд

        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(ruleRider, this);

        // Регистрация слушателя acvUpdater
        new acvUpdater(this, new JSONArray());
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin disabled!");
    }

    public static Main getInstance() {
        return instance;
    }

    private void createDataFileIfNotExists() {
        if (!dataFile.exists()) {
            try {
                dataFile.getParentFile().mkdirs(); // Создание родительских директорий
                dataFile.createNewFile(); // Создание нового файла
                try (FileWriter writer = new FileWriter(dataFile)) {
                    writer.write("{\"players\":[]}"); // Запись начальной структуры JSON
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Не удалось создать файл данных", e);
            }
        }
    }

    private void createRuleFileIfNotExists() {
        if (!rulesFile.exists()) {
            try {
                rulesFile.getParentFile().mkdirs(); // Создание родительских директорий
                rulesFile.createNewFile(); // Создание нового файла
                try (FileWriter writer = new FileWriter(rulesFile)) {
                    writer.write("{\"Rules\":[]}"); // Запись начальной структуры JSON
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Не удалось создать файл данных", e);
            }
        }
    }

    private void createRegionsFileIfNotExists() {
        if (!regionsFile.exists()) {
            try {
                regionsFile.getParentFile().mkdirs(); // Создание родительских директорий
                regionsFile.createNewFile(); // Создание нового файла
                try (FileWriter writer = new FileWriter(regionsFile)) {
                    writer.write("{\"Regions\":[]}"); // Запись начальной структуры JSON
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Не удалось создать файл данных", e);
            }
        }
    }

    private void createPlayTimeFileIfNotExists() {
        if (!playTimeFile.exists()) {
            try {
                playTimeFile.getParentFile().mkdirs(); // Создание родительских директорий
                playTimeFile.createNewFile(); // Создание нового файла
                try (FileWriter writer = new FileWriter(playTimeFile)) {
                    writer.write("{\"Players\":[]}"); // Запись начальной структуры JSON
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "Не удалось создать файл данных", e);
            }
        }
    }
}
