package org.example;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;


public class acvUpdater implements Listener {



    private String currentLeader = "";

    private JSONArray playersArray; // Ваш JSON массив игроков
    private JavaPlugin plugin; // Ссылка на ваш основной плагин класс

    public acvUpdater(JavaPlugin plugin, JSONArray playersArray) {
        this.plugin = plugin;
        this.playersArray = playersArray;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {


        File unsorted_file = new File(Main.getInstance().getDataFolder(), "achievements.json");
        try (FileReader reader = new FileReader(unsorted_file)) {
            // Чтение и парсинг JSON файла
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            JSONArray playersArray = jsonObject.getJSONArray("players");
            sortPlayersByScore(playersArray);

            if (playersArray.length() > 0) {
                String newLeader = playersArray.getJSONObject(0).getString("name");
                if (!newLeader.equals(currentLeader)) {
                    currentLeader = newLeader;
                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        onlinePlayer.sendMessage(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "ᴀ " + ChatColor.RESET + "" + ChatColor.WHITE
                                + "Новый лидер по достижениям: " + ChatColor.YELLOW + newLeader + ChatColor.WHITE + ".\n " +
                                ChatColor.GRAY + "(посмотреть статистику: /topacv)");
                    }
                }
            }

        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
        }


        File file = new File(Main.getInstance().getDataFolder(), "achievements.json");
        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs(); // Создание родительских директорий
                file.createNewFile(); // Создание нового файла
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write("{\"players\":[]}"); // Запись начальной структуры JSON
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        try (FileReader reader = new FileReader(file)) {
            // Чтение и парсинг JSON файла
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);


            // Найти игрока в JSON массиве
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                updateAcvData(onlinePlayer, file);
            }
        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
        }
        // Если игрок не найден в JSON массиве, можно добавить его

    }

    public void updateAcvData(Player player, File file) {
        int completedCount = checkAcv(player);

        try (FileReader reader = new FileReader(file)) {
            // Чтение и парсинг JSON файла
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            JSONArray playersArray = jsonObject.getJSONArray("players");

            boolean playerExists = false;
            // Проверка, существует ли запись для игрока
            for (int i = 0; i < playersArray.length(); i++) {
                JSONObject playerObj = playersArray.getJSONObject(i);
                if (playerObj.getString("name").equals(player.getName())) {
                    playerExists = true;
                    int currentScore = playerObj.getInt("score");
                    if (completedCount > currentScore) {
                        playerObj.put("score", completedCount); // Обновляем score
                    }
                    break;
                }
            }

            if (!playerExists) {
                JSONObject newPlayer = new JSONObject();
                newPlayer.put("name", player.getName()); // Запись имени игрока
                newPlayer.put("score", completedCount); // Запись значения
                playersArray.put(newPlayer); // Добавление записи в массив
            }
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonObject.toString());
                writer.flush();
            }
            catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
        }



    }


    public int checkAcv(Player player) {
        // Используем точный идентификатор достижения
        NamespacedKey namespacedKey = new NamespacedKey("blazeandcave", "bacap/advancement_legend");
        int completedCount = 0;
        Advancement advancement = Bukkit.getAdvancement(namespacedKey);
        if (advancement != null) {
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            completedCount = progress.getAwardedCriteria().size();
        }
        return completedCount;
    }

    public void sortPlayersByScore(JSONArray playersArray) {
        // Конвертируем JSONArray в List для удобства сортировки
        List<JSONObject> playersList = new ArrayList<>();
        for (int i = 0; i < playersArray.length(); i++) {
            playersList.add(playersArray.getJSONObject(i));
        }

        // Сортируем список по очкам
        Collections.sort(playersList, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject a, JSONObject b) {
                return Integer.compare(b.getInt("score"), a.getInt("score"));
            }
        });

        // Создаем новый отсортированный JSONArray
        JSONArray sortedArray = new JSONArray();
        for (JSONObject playerObj : playersList) {
            sortedArray.put(playerObj);
        }

        // Заменяем старый playersArray на новый отсортированный
        for (int i = 0; i < sortedArray.length(); i++) {
            playersArray.put(i, sortedArray.get(i));
        }
    }
}
