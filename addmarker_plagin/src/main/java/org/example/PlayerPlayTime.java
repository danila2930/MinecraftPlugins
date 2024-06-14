package org.example;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class PlayerPlayTime extends BukkitRunnable {

    private Set<String> notTrustedPlayers = new HashSet<>();
    private final JavaPlugin plugin;
    private final File file;

    public PlayerPlayTime(JavaPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "playTimeData.json");
    }

    @Override
    public void run() {
        try (FileReader reader = new FileReader(file)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            JSONArray playersArray = jsonObject.getJSONArray("Players");

            for (Player player : Bukkit.getOnlinePlayers()) {
                updatePlayerPlayTime(playersArray, player);
            }

            jsonObject.put("Players", playersArray);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonObject.toString(4)); // 4 - это количество пробелов для форматирования
                writer.flush();
            }

        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
        }
    }

    private void updatePlayerPlayTime(JSONArray playersArray, Player player) {
        long playTimeTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
        long playTimeMillis = playTimeTicks * 50; // Преобразование тиков в миллисекунды

        boolean playerFound = false;
        for (int i = 0; i < playersArray.length(); i++) {
            JSONObject playerData = playersArray.getJSONObject(i);
            if (playerData.getString("name").equals(player.getName())) {
                playerFound = true;
                playerData.put("playTime", playTimeMillis);
                boolean isTrusted = playerData.getBoolean("trusted");

                if (isTrusted || playTimeMillis >= 36000000) {
                    playerData.put("trusted", true);
                    notTrustedPlayers.remove(player.getName()); // Удаляем игрока из Set, если он стал доверенным
                } else if (!isTrusted && playTimeMillis < 36000000) {
                    playerData.put("trusted", false);
                    notTrustedPlayers.add(player.getName()); // Добавляем игрока в Set, если он не доверенный
                }

                playersArray.put(i, playerData);
                break;
            }
        }

        if (!playerFound) {
            JSONObject newPlayerData = new JSONObject();
            newPlayerData.put("name", player.getName());
            newPlayerData.put("playTime", playTimeMillis);
            boolean trustedStatus = playTimeMillis >= 36000000;
            newPlayerData.put("trusted", trustedStatus);
            playersArray.put(newPlayerData);
            if (!trustedStatus) {
                notTrustedPlayers.add(player.getName()); // Добавляем игрока в Set, если он не доверенный
            }
        }
    }

    public Set<String> getNotTrustedPlayers() {
        return notTrustedPlayers;
    }
}
