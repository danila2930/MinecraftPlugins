package org.example;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;



public class RuleRider implements Listener {

    private Map<String, Boolean> playerWhoReaded = new HashMap<>();
    private Map<String, Integer> playersRulePage = new HashMap<>();



    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!playerWhoReaded.getOrDefault(player.getName(), false)) {
            event.setCancelled(true);
            player.sendTitle(ChatColor.YELLOW + "ВНИМАНИЕ!", "Вы не можете двигаться, пока не прочтете правила!", 10, 70, 20);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!playerWhoReaded.getOrDefault(player.getName(), false)) {
            event.setCancelled(true);
            player.sendTitle(ChatColor.YELLOW + "ВНИМАНИЕ!", "Вы не можете двигаться, пока не прочтете правила!", 10, 70, 20);
        }
    }



    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        File Player_data_file = new File(Main.getInstance().getDataFolder(), "playersData.json");
        try (FileReader reader = new FileReader(Player_data_file)) {
            // Чтение и парсинг JSON файла
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            JSONArray playersArray = jsonObject.getJSONArray("players");

            boolean playerExists = false;
            // Проверка, существует ли запись для игрока
            for (int i = 0; i < playersArray.length(); i++) {
                String playerName = playersArray.getString(i);
                if (playerName.equals(player.getName())) {
                    playerExists = true;
                    playerWhoReaded.put(player.getName(), true);

                    break;
                }
            }

            if (!playerWhoReaded.getOrDefault(player.getName(), false)) {
                player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1));
                World world = player.getWorld();
                Location spawnLocation = world.getSpawnLocation();
                player.teleport(spawnLocation);
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {

                    @Override
                    public void run() {

                        player.sendTitle(ChatColor.YELLOW + "ВНИМАНИЕ!", "Вы не можете двигаться, пока не прочтете правила!", 10, 70, 20);
                        playersRulePage.put(player.getName(), 0); // Добавляет или обновляет запись для игрока "playerName" с номером страницы 0
                        loadRule(player);
                    }
                }, 40L); // Задержка в 40 тиков (2 секунды)
            }


        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
        }
    }




    public void loadRule(Player player) {

        File Player_rule_file = new File(Main.getInstance().getDataFolder(), "rulesData.json");

        try (FileReader reader = new FileReader(Player_rule_file)) {
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            JSONArray RulesArray = jsonObject.getJSONArray("Rules");


            Integer pageIndex = playersRulePage.get(player.getName());
            if (pageIndex != null && pageIndex >= 0 && pageIndex < RulesArray.length()) {
                String ruleText = RulesArray.getString(pageIndex);
                for (int i = 0; i < 30; i++){
                    player.sendMessage(" ");
                }
                player.sendMessage(ChatColor.COLOR_CHAR + ruleText);

                TextComponent message = new TextComponent("ДАЛЕЕ (или /nextpage)");
                message.setBold(true);
                message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nextpage"));
                player.spigot().sendMessage(message);
            } else if (pageIndex >= RulesArray.length()) {
                File file = new File(Main.getInstance().getDataFolder(), "playersData.json");
                JSONObject jsonObjectPlayers;
                JSONArray playersArrayPlayers;

                try (FileReader readerPlayers = new FileReader(file)) {
                    JSONTokener tokenerPlayers = new JSONTokener(readerPlayers);
                    jsonObjectPlayers = new JSONObject(tokenerPlayers);
                    playersArrayPlayers = jsonObjectPlayers.getJSONArray("players");
                }

                boolean playerExists = false;
// Проверка, существует ли запись для игрока
                for (int i = 0; i < playersArrayPlayers.length(); i++) {
                    if (playersArrayPlayers.getString(i).equals(player.getName())) {
                        playerExists = true;
                        break;
                    }
                }

                if (!playerExists) {
                    playersArrayPlayers.put(player.getName());
                    jsonObjectPlayers.put("players", playersArrayPlayers);

                    try (FileWriter writer = new FileWriter(file)) {
                        writer.write(jsonObjectPlayers.toString());
                        writer.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Main.getInstance().getLogger().log(Level.SEVERE, "Не удалось сохранить данные игроков", e);
                    }
                }

                player.sendMessage(ChatColor.GREEN + "Вы прочитали все правила. Приятной игры!");
                player.removePotionEffect(PotionEffectType.BLINDNESS);
                playerWhoReaded.put(player.getName(), true);


            }
        } catch (IOException | org.json.JSONException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "Не удалось загрузить правила", e);
        }
    }



    public class nextpage implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String playerName = player.getName();
                playersRulePage.put(playerName, playersRulePage.get(playerName) + 1);
                loadRule(player);
                return true;
            }
            return false;
        }
    }


}
