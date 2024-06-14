package org.example;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class warning implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Эту команду может использовать только игрок.");
            return true;
        }

        Player player = (Player) sender;
        if (command.getName().equalsIgnoreCase("warning") && (player.isOp() || sender.getName().equalsIgnoreCase("danila2930") || sender.getName().equalsIgnoreCase("PiroKIirik"))) {
            if (args.length < 3) {
                sender.sendMessage("Не верно ввел команду: /warning <player> <time in seconds> <text>");
                return true;
            }

            // Получаем ник игрока
            String playerName = args[0];

            // Парсинг времени и обработка возможного исключения
            int time;
            try {
                time = Integer.parseInt(args[1]) * 20;
            } catch (NumberFormatException e) {
                sender.sendMessage("Неверное значение времени. Пожалуйста, введите число.");
                return true;
            }

            // Получаем текст предупреждения
            StringBuilder warningMessage = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                warningMessage.append(args[i]).append(" ");
            }

            // Находим игрока по нику
            Player targetPlayer = Bukkit.getPlayer(playerName);
            if (targetPlayer != null && targetPlayer.isOnline()) {
                String formattedMessage = ChatColor.RED + "" + ChatColor.BOLD + warningMessage.toString().trim();
                targetPlayer.sendMessage(formattedMessage);
                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, time, 10));
                targetPlayer.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, time, 10));

                sender.sendMessage("Сообщение было отправлено игроку " + playerName);
                return true;
            } else {
                sender.sendMessage("Игрок " + playerName + " не найден");
                return true;
            }
        }
        return true;
    }
}
