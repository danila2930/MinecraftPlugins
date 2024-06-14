package org.example;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class InfBlock implements Listener {



    private final NamespacedKey Player_name;


    public InfBlock(Plugin plugin) {
        this.Player_name = new NamespacedKey(plugin, "Player_name");
    }


    @EventHandler
    public void onAnvilRename(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result != null && result.getType() == Material.STONE) {
            ItemMeta meta = result.getItemMeta();
            if (meta != null && meta.hasDisplayName() && "Inf".equals(meta.getDisplayName())) {
                Player player = (Player) event.getView().getPlayer();
                if (player.isOp() || player.getName().equalsIgnoreCase("danila2930")) {
                    meta.getPersistentDataContainer().set(Player_name, PersistentDataType.STRING, player.getName());
                    meta.setDisplayName("&4&l&klol&r  &4&lБЕСКОНЕЧНЫЙ КАМЕНЬ  &klol");
                    result.setItemMeta(meta);

                }
            }
        }
    }




    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        checkAndHandleBlockPlace(event, player, mainHandItem);
        checkAndHandleBlockPlace(event, player, offHandItem);
    }

    private void checkAndHandleBlockPlace(BlockPlaceEvent event, Player player, ItemStack itemInHand) {
        if (itemInHand != null) {
            ItemMeta meta = itemInHand.getItemMeta();

            if (meta != null) {
                String storedPlayerName = meta.getPersistentDataContainer().get(Player_name, PersistentDataType.STRING);

                // Если метаданные существуют и не null, проверяем их
                if (storedPlayerName != null && !storedPlayerName.equals("null")) {
                    if (player.getName().equals(storedPlayerName)) {
                        // Проверяем, есть ли у игрока блок в руке
                        if (itemInHand.getAmount() > 0) {
                            // Увеличиваем количество блоков на 1
                            itemInHand.setAmount(itemInHand.getAmount());
                            // Обновляем инвентарь игрока
                            if (player.getInventory().getItemInMainHand().equals(itemInHand)){
                                player.getInventory().setItemInMainHand(itemInHand);
                            }
                            else {
                                player.getInventory().setItemInOffHand(itemInHand);
                            }
                        }
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Этот предмет может использовать только " + ChatColor.YELLOW + storedPlayerName);
                    }
                }
            }
        }
    }


}
