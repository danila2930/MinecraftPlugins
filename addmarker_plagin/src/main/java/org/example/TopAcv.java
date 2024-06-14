package org.example;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

public class TopAcv implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("topacv")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

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

                JSONArray playersArray = null;

                // обновление данных
                try (FileReader reader = new FileReader(file)) {
                    // Чтение и парсинг JSON файла
                    JSONTokener tokener = new JSONTokener(reader);
                    JSONObject jsonObject = new JSONObject(tokener);
                    playersArray = jsonObject.getJSONArray("players");

                    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                        updateAcvData(onlinePlayer, playersArray);
                    }

                    // Сортировка игроков по очкам
                    sortPlayersByScore(playersArray);

                    // Запись обновленного и отсортированного массива обратно в файл
                    try (FileWriter writer = new FileWriter(file)) {
                        jsonObject.put("players", playersArray);
                        writer.write(jsonObject.toString());
                        writer.flush();
                    } catch (IOException | org.json.JSONException e) {
                        e.printStackTrace();
                    }

                } catch (IOException | org.json.JSONException e) {
                    e.printStackTrace();
                }

                if (playersArray != null) {
                    // вывод сообщения
                    String playerList = ChatColor.GREEN + "" + ChatColor.BOLD + "\n" +
                            ChatColor.LIGHT_PURPLE + "          " + ChatColor.BOLD +  "\uD83C\uDFC6 TOP по достижениям \uD83C\uDFC6\n" +
                            ChatColor.GREEN + "" + ChatColor.BOLD + "\n";

                    for (int i = 0; i < Math.min(playersArray.length(), 10); i++) {
                        playerList += "          ";
                        if (i < 3) {
                            playerList += ChatColor.RED + "" + ChatColor.BOLD + (i + 1) + ". ";
                        }

                        JSONObject playerObj = playersArray.getJSONObject(i);
                        playerList += ChatColor.YELLOW + "" + ChatColor.BOLD + playerObj.getString("name") + " - " + playerObj.getInt("score") + "\n";
                    }

                    playerList += ChatColor.GREEN + "" + ChatColor.BOLD + "\n";
                    player.sendMessage(playerList);
                }

            } else {
                sender.sendMessage("Эту команду может использовать только игрок.");
            }
        }
        return true;
    }

    public void updateAcvData(Player player, JSONArray playersArray) {
        int completedCount = checkAcv(player);

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
