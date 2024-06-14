package org.example;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class SetTruster implements CommandExecutor {

    private final JavaPlugin plugin;

    public SetTruster(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (!player.hasPermission("CuboebPlugin.trusted")) {
                player.sendMessage(ChatColor.RED + "У вас нет разрешения на выполнение этой команды.");
                return true;
            }
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Не верно ввели команду: /trusted <player> <true|false>");
            return true;
        }

        String playerName = args[0];
        boolean trustedStatus;

        try {
            trustedStatus = Boolean.parseBoolean(args[1]);
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Используйте true или false.");
            return true;
        }

        File file = new File(plugin.getDataFolder(), "playTimeData.json");

        try (FileReader reader = new FileReader(file)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            JSONArray playersArray = jsonObject.getJSONArray("Players");

            boolean playerFound = false;
            for (int i = 0; i < playersArray.length(); i++) {
                JSONObject playerData = playersArray.getJSONObject(i);
                if (playerData.getString("name").equals(playerName)) {
                    playerFound = true;
                    playerData.put("trusted", trustedStatus);
                    playersArray.put(i, playerData);
                    break;
                }
            }

            if (!playerFound) {
                sender.sendMessage(ChatColor.RED + "Игрок не найден в базе.");
                return true;
            }

            jsonObject.put("Players", playersArray);
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonObject.toString(4)); // 4 - это количество пробелов для форматирования
                writer.flush();
            }

            sender.sendMessage(ChatColor.GREEN + "Состояние игрока " + playerName + " было изменено");

        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + "Упс... что-то пошло не так.");
            return false;
        }

        return true;
    }
}
