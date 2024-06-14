package org.example;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;

public class TridentListener implements Listener {

    private final Plugin plugin;
    private final NamespacedKey zeusKey;

    public TridentListener(Plugin plugin) {
        this.plugin = plugin;
        this.zeusKey = new NamespacedKey(plugin, "ZeusWeapon");
    }

    @EventHandler
    public void onAnvilRename(PrepareAnvilEvent event) {
        ItemStack result = event.getResult();
        if (result != null && result.getType() == Material.TRIDENT) {
            ItemMeta meta = result.getItemMeta();
            if (meta != null && meta.hasDisplayName() && "&aОрудие Зевса".equals(meta.getDisplayName())) {
                Player player = (Player) event.getView().getPlayer();
                if (player.isOp() || player.getName().equalsIgnoreCase("danila2930")) {
                    meta.getPersistentDataContainer().set(zeusKey, PersistentDataType.STRING, "true");
                    result.setItemMeta(meta);
                }
            }
        }
    }

    @EventHandler
    public void onTridentHit(ProjectileHitEvent event) {
        if (event.getEntityType() == EntityType.TRIDENT) {
            Trident trident = (Trident) event.getEntity();
            if (trident.getShooter() instanceof Player) {
                ItemStack tridentItem = trident.getItem();
                if (tridentItem != null && tridentItem.getType() == Material.TRIDENT) {
                    ItemMeta meta = tridentItem.getItemMeta();
                    if (meta != null && "true".equals(meta.getPersistentDataContainer().get(zeusKey, PersistentDataType.STRING))) {
                        if (event.getHitEntity() != null) {
                            event.getHitEntity().getWorld().strikeLightning(event.getHitEntity().getLocation());
                        }
                    }
                }
            }
        }
    }
}