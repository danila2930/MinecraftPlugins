package org.example;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;


public class CrystalBowListener implements Listener {

    private final Plugin plugin;
    private final NamespacedKey crystalBowKey;

    public CrystalBowListener(Plugin plugin) {
        this.plugin = plugin;
        this.crystalBowKey = new NamespacedKey(plugin, "CrystalBow");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.BOW) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null && meta.hasDisplayName() && "Кристальный Лук".equals(meta.getDisplayName())) {

                if (player.isOp() || player.getName().equalsIgnoreCase("danila2930")) {
                    meta.getPersistentDataContainer().set(crystalBowKey, PersistentDataType.STRING, "true");
                    item.setItemMeta(meta);
                }
            }
        }
    }




    @EventHandler
    public void onAnvilRename(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result != null && result.getType() == Material.BOW) {
            ItemMeta meta = result.getItemMeta();
            if (meta != null && meta.hasDisplayName() && "&acrystalBow".equals(meta.getDisplayName())) {
                Player player = (Player) event.getView().getPlayer();
                if (player.isOp() || player.getName().equalsIgnoreCase("danila2930")) {
                    meta.getPersistentDataContainer().set(crystalBowKey, PersistentDataType.STRING, "true");
                    result.setItemMeta(meta);
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() instanceof Player) {
            Player player = (Player) projectile.getShooter();
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.BOW) {
                item = player.getInventory().getItemInOffHand();
            }
            if (item != null && item.getType() == Material.BOW) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && "true".equals(meta.getPersistentDataContainer().get(crystalBowKey, PersistentDataType.STRING))) {
                    if (event.getHitBlock() != null) {
                        if (player.getInventory().contains(Material.TORCH)) {
                            Block hitBlock = event.getHitBlock();
                            BlockFace hitFace = event.getHitBlockFace();
                            Block placeBlock = hitBlock.getRelative(hitFace);

                            // Убедитесь, что факел не ставится на потолок
                            if (hitFace == BlockFace.DOWN) {
                                return;
                            }

                            // Убедитесь, что блок, на который ставится факел, это воздух
                            if (placeBlock.getType() == Material.AIR) {
                                player.getInventory().removeItem(new ItemStack(Material.TORCH, 1));

                                if (hitFace == BlockFace.UP) {
                                    placeBlock.setType(Material.TORCH);
                                } else {
                                    placeBlock.setType(Material.WALL_TORCH);

                                    // Установите правильное направление факела на стене
                                    Directional wallTorchData = (Directional) placeBlock.getBlockData();
                                    wallTorchData.setFacing(hitFace);
                                    placeBlock.setBlockData(wallTorchData);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}