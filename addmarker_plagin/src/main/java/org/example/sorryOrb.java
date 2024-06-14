package org.example;

import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


import java.util.concurrent.ThreadLocalRandom;


public class sorryOrb implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("sorryOrb") && (player.isOp() || sender.getName().equalsIgnoreCase("danila2930"))) {
            if (args.length < 1) {
                sender.sendMessage("Не верно ввел команду: /sorryOrb <player>");
                return true;
            }

            // Получаем ник игрока
            String playerName = args[0];


            // Находим игрока по нику
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer != null && targetPlayer.isOnline()) {

                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 100, 3));
                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 1000, 1));
                StringBuilder sorryMessage = new StringBuilder("Такое бывает))");
                String formattedMessage = ChatColor.GREEN + "" + ChatColor.BOLD + sorryMessage.toString().trim();
                targetPlayer.sendMessage(formattedMessage);
                spawnRandomExperienceOrb(targetPlayer.getLocation());
                return true;
            }
            else if (playerName.equalsIgnoreCase("all")){


                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

                    onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.HEAL, 100, 3));
                    onlinePlayer.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 100, 3));
                    StringBuilder sorryMessage = new StringBuilder("Простите. Такое бывает))");
                    String formattedMessage = ChatColor.GREEN + "" + ChatColor.BOLD + sorryMessage.toString().trim();
                    onlinePlayer.sendMessage(formattedMessage);
                    Location PlayerPos = onlinePlayer.getLocation();
                    for (int i = 0; i < 10; i++){
                        spawnRandomExperienceOrb(PlayerPos);
                    }

                }
            }



            else {
                sender.sendMessage("Игрок " + playerName + " не найден");
                return true;
            }
        }
        return true;
    }

    private void spawnRandomExperienceOrb(Location location) {
        // Генерация случайных смещений в пределах -10 до 10 блоков
        int randomX = ThreadLocalRandom.current().nextInt(-3, 4);
        int randomZ = ThreadLocalRandom.current().nextInt(-3, 4);

        // Создание новой локации с учетом случайных смещений
        Location randomLocation = location.clone().add(randomX, 0, randomZ);

        // Спавн сферы опыта в новой локации
        ExperienceOrb orb = randomLocation.getWorld().spawn(randomLocation, ExperienceOrb.class);
        orb.setExperience(320); // Устанавливаем количество опыта
    }
}
