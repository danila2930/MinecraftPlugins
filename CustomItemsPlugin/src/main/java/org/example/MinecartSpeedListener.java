package org.example;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.util.Vector;



public class MinecartSpeedListener implements Listener {

    @EventHandler
    public void onMinecartMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Minecart) {
            Minecart minecart = (Minecart) event.getVehicle();
            Vector velocity = minecart.getVelocity();
            Block railBlock = minecart.getLocation().getBlock();
            Block blockBelowRail = railBlock.getRelative(0, -1, 0);

            if (blockBelowRail.getType() == Material.GRAVEL) {
                boolean chainsAbove = false;


                Block blockAbove = railBlock.getLocation().add(0, 3, 0).getBlock();
                if (blockAbove.getType() == Material.CHAIN || blockAbove.getType() == Material.DEEPSLATE_TILE_SLAB) {
                    chainsAbove = true;
                }


                // Проверка, сидит ли в вагонетке игрок
                if (minecart.getPassenger() instanceof Player) {
                    Player player = (Player) minecart.getPassenger();

                    if (chainsAbove) {
                        double speedMultiplier = 1.2; // Коэффициент увеличения скорости
                        Vector newVelocity = velocity.multiply(speedMultiplier);
                        minecart.setMaxSpeed(3); // Установите максимальную скорость вагонетки
                        minecart.setVelocity(newVelocity);
                    } else {
                        double speedMultiplier = 1.01; // Коэффициент увеличения скорости
                        Vector newVelocity = velocity.multiply(speedMultiplier);
                        minecart.setMaxSpeed(1); // Установите максимальную скорость вагонетки
                        minecart.setVelocity(newVelocity);
                    }
                } else {
                    // Если под вагонеткой нет гравия
                    double speedMultiplier = 0; // Коэффициент увеличения скорости
                    Vector newVelocity = velocity.multiply(speedMultiplier);
                    minecart.setMaxSpeed(0.4); // Установите максимальную скорость вагонетки
                    minecart.setVelocity(newVelocity);
                }

            }
            else {
                if (minecart.getPassenger() instanceof Player) {
                    {
                        if (minecart.getMaxSpeed() > 0.4) {
                            minecart.setMaxSpeed(0.4); // Установите максимальную скорость вагонетки
                        }
                    }
                }
            }

        }

    }

}


