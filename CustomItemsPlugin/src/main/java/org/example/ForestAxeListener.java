package org.example;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class ForestAxeListener implements Listener {

    private final Plugin plugin;
    private final NamespacedKey forestAxeKey;
    private final Random random;

    public ForestAxeListener(Plugin plugin) {
        this.plugin = plugin;
        this.forestAxeKey = new NamespacedKey(plugin, "ForestAxe");
        this.random = new Random();
    }

    @EventHandler
    public void onAnvilRename(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result != null && result.getType() == Material.DIAMOND_AXE) {
            ItemMeta meta = result.getItemMeta();
            if (meta != null && meta.hasDisplayName() && "&aforestAxe".equals(meta.getDisplayName())) {
                Player player = (Player) event.getView().getPlayer();
                if (player.isOp() || player.getName().equalsIgnoreCase("danila2930")) {
                    meta.getPersistentDataContainer().set(forestAxeKey, PersistentDataType.STRING, "true");
                    result.setItemMeta(meta);
                }
            }
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (block.getType() == Material.OAK_LOG || block.getType() == Material.SPRUCE_LOG || block.getType() == Material.BIRCH_LOG ||
                block.getType() == Material.JUNGLE_LOG || block.getType() == Material.ACACIA_LOG || block.getType() == Material.DARK_OAK_LOG) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (!item.getType().name().endsWith("_AXE")) {
                item = player.getInventory().getItemInOffHand();
            }
            if (item != null && item.getType().name().endsWith("_AXE")) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && "true".equals(meta.getPersistentDataContainer().get(forestAxeKey, PersistentDataType.STRING))) {
                    if (random.nextDouble() < 0.5) {
                        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(block.getType()));
                    }
                }
            }
        }
    }
}
