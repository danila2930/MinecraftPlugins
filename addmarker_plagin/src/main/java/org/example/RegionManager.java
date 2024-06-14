package org.example;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class RegionManager implements Listener {

    private final Main plugin;
    private final PlayerPlayTime playerPlayTime;
    private Map<String, Set<Location>> regionsMap = new HashMap<>();
    private Map<UUID, Long> lastMessageTime = new HashMap<>();


    public RegionManager(Main plugin, PlayerPlayTime playerPlayTime) {
        this.plugin = plugin;
        this.playerPlayTime = playerPlayTime;
        loadRegions(); // Загрузка регионов при создании экземпляра
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location blockLocation = event.getBlock().getLocation();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        if (isPlayerInProtectedRegion(player, blockLocation)) {
            event.setCancelled(true);
            player.sendMessage("Вы не можете ставить здесь блоки пока не наиграете 10 часов.");
        } else if (mainHandItem != null && mainHandItem.getType().toString().endsWith("_BANNER")) { // Проверка на любой флаг
            ItemMeta meta = mainHandItem.getItemMeta();
            if (meta != null && meta.hasDisplayName()) { // Проверка, что флаг переименован
                File file = new File(Main.getInstance().getDataFolder(), "regionsData.json");
                placeRegion(event.getBlock().getLocation(), player, file);
            } else {
                player.sendMessage(ChatColor.RED + "Флаг должен быть переименован, чтобы использовать его для создания региона.");
            }
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location blockLocation = event.getBlock().getLocation();
        if (isPlayerInProtectedRegion(player, blockLocation)) {
            event.setCancelled(true);
            player.sendMessage("Вы не можете ломать здесь блоки пока не наиграете 10 часов.");
        } else if (event.getBlock().getType().toString().endsWith("_BANNER")) { // Проверка на любой флаг
            File file = new File(Main.getInstance().getDataFolder(), "regionsData.json");
            try (FileReader reader = new FileReader(file)) {
                JSONTokener tokener = new JSONTokener(reader);
                JSONObject jsonObject = new JSONObject(tokener);
                JSONArray regionsArray = jsonObject.getJSONArray("Regions");

                boolean regionFound = false;
                String playerName = "";

                for (int i = 0; i < regionsArray.length(); i++) {
                    JSONObject region = regionsArray.getJSONObject(i);
                    int x = region.getInt("x");
                    int y = region.getInt("y");
                    int z = region.getInt("z");
                    playerName = region.getString("player");

                    if (blockLocation.getBlockX() == x && blockLocation.getBlockY() == y && blockLocation.getBlockZ() == z) {
                        regionsArray.remove(i);
                        regionFound = true;
                        break;
                    }
                }

                if (regionFound) {
                    if (playerName.equals(player.getName()) || player.isOp()) {
                        jsonObject.put("Regions", regionsArray);
                        try (FileWriter writer = new FileWriter(file)) {
                            writer.write(jsonObject.toString(4)); // 4 - это количество пробелов для форматирования
                            writer.flush();
                            player.sendMessage(ChatColor.GREEN + "Регион успешно удален!");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // Обновление regionsMap
                        regionsMap.get(playerName).remove(blockLocation);
                    } else {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Вы не можете уничтожить чужой регион");
                    }
                }

            } catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
            }
        }

    }
    @EventHandler
    public void onHangingBreak(HangingBreakEvent event) {
        if (event.getEntity() instanceof Hanging) {
            if (event.getCause() == HangingBreakEvent.RemoveCause.ENTITY) {
                Entity remover = ((HangingBreakByEntityEvent) event).getRemover();
                if (remover instanceof Player) {
                    Player player = (Player) remover;
                    Location entityLocation = event.getEntity().getLocation();
                    if (isPlayerInProtectedRegion(player, entityLocation)) {
                        event.setCancelled(true);
                        player.sendMessage("Вы не можете ломать в защищенной области.");
                    }
                }
            }
        }
    }


    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Player player = (Player) event.getPlayer();
        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof org.bukkit.block.BlockState) {
            Location blockLocation = ((org.bukkit.block.BlockState) holder).getLocation();
            if (blockLocation.getBlock().getType() == Material.CHEST) {
                if (isPlayerInProtectedRegion(player, blockLocation)) {
                    event.setCancelled(true);
                    player.sendMessage("Вы не можете открывать сундуки в защищенной области.");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        Location blockLocation = block.getLocation();
        Material blockType = block.getType();

        // Проверка взаимодействия с любыми блоками, кроме сундуков Эндера и верстаков
        boolean isRestrictedBlock = blockType != Material.ENDER_CHEST && blockType != Material.CRAFTING_TABLE;

        // Проверка взаимодействия с нажимными плитами
        boolean isPressurePlate = blockType == Material.STONE_PRESSURE_PLATE ||
                blockType == Material.OAK_PRESSURE_PLATE ||
                blockType == Material.SPRUCE_PRESSURE_PLATE ||
                blockType == Material.BIRCH_PRESSURE_PLATE ||
                blockType == Material.JUNGLE_PRESSURE_PLATE ||
                blockType == Material.ACACIA_PRESSURE_PLATE ||
                blockType == Material.DARK_OAK_PRESSURE_PLATE ||
                blockType == Material.CRIMSON_PRESSURE_PLATE ||
                blockType == Material.WARPED_PRESSURE_PLATE ||
                blockType == Material.LIGHT_WEIGHTED_PRESSURE_PLATE ||
                blockType == Material.HEAVY_WEIGHTED_PRESSURE_PLATE;

        if ((action == Action.RIGHT_CLICK_BLOCK && isRestrictedBlock) || (action == Action.PHYSICAL && isPressurePlate)) {
            if (isPlayerInProtectedRegion(player, blockLocation)) {
                event.setCancelled(true);
                sendMessageWithCooldown(player, "Вы не можете взаимодействовать с этим объектом в защищенной области.");
            }
        }
    }

    private void sendMessageWithCooldown(Player player, String message) {
        long currentTime = System.currentTimeMillis();
        long lastTime = lastMessageTime.getOrDefault(player.getUniqueId(), 0L);
        if (currentTime - lastTime >= 1000) { // Проверка на интервал в 1 секунду
            player.sendMessage(message);
            lastMessageTime.put(player.getUniqueId(), currentTime);
        }
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        Location blockLocation = event.getBlock().getLocation();
        if (isPlayerInProtectedRegion(player, blockLocation)) {
            event.setCancelled(true);
            player.sendMessage("Вы не можете менять таблички в защищенной области.");
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        Location blockLocation = event.getBlock().getLocation();
        if (isPlayerInProtectedRegion(player, blockLocation)) {
            event.setCancelled(true);
            player.sendMessage("Вы не можете зажигать огонь в защищенной области.");
        }
    }
    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            Entity remover = event.getRemover();
            if (remover instanceof Player) {
                Player player = (Player) remover;
                Location entityLocation = event.getEntity().getLocation();
                if (isPlayerInProtectedRegion(player, entityLocation)) {
                    event.setCancelled(true);
                    player.sendMessage("Вы не можете ломать рамки для предметов в защищенной области.");
                }
            }
        }
    }



    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Location entityLocation = event.getRightClicked().getLocation();
        if (event.getRightClicked() instanceof ItemFrame && isPlayerInProtectedRegion(player, entityLocation)) {
            event.setCancelled(true);
            player.sendMessage("Вы не можете взаимодействовать с предметами в защищенной области.");
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ItemFrame) {
            Entity damager = event.getDamager();
            if (damager instanceof Player) {
                Player player = (Player) damager;
                Location entityLocation = event.getEntity().getLocation();
                if (isPlayerInProtectedRegion(player, entityLocation)) {
                    event.setCancelled(true);
                    player.sendMessage("Вы не можете ломать в защищенной области.");
                }
            }
        } else if (event.getEntity() instanceof Animals) {
            Entity damager = event.getDamager();
            if (damager instanceof Player) {
                Player player = (Player) damager;
                Location entityLocation = event.getEntity().getLocation();
                if (isPlayerInProtectedRegion(player, entityLocation)) {
                    event.setCancelled(true);
                    player.sendMessage("Вы не можете атаковать мирных мобов в защищенной области.");
                }
            }
        }
    }



    private boolean isPlayerInProtectedRegion(Player player, Location blockLocation) {
        if (playerPlayTime.getNotTrustedPlayers().contains(player.getName())) {
            for (Map.Entry<String, Set<Location>> entry : regionsMap.entrySet()) {
                String owner = entry.getKey();
                Set<Location> locations = entry.getValue();
                if (!player.getName().equals(owner)) {
                    for (Location region : locations) {
                        if (blockLocation.getWorld().equals(region.getWorld()) &&
                                blockLocation.getBlockX() < region.getBlockX() + 64 && blockLocation.getBlockX() > region.getBlockX() - 64 &&
                                blockLocation.getBlockY() < 320 && blockLocation.getBlockY() > -64 &&
                                blockLocation.getBlockZ() < region.getBlockZ() + 64 && blockLocation.getBlockZ() > region.getBlockZ() - 64) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }



    public void loadRegions() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Map<String, Set<Location>> newRegionsMap = new HashMap<>();
            File file = new File(plugin.getDataFolder(), "regionsData.json");
            if (!file.exists()) {
                plugin.getLogger().warning("regionsData.json does not exist!");
                return;
            }

            try (FileReader reader = new FileReader(file)) {
                JSONTokener tokener = new JSONTokener(reader);
                JSONObject jsonObject = new JSONObject(tokener);
                JSONArray regionsArray = jsonObject.getJSONArray("Regions");

                for (int i = 0; i < regionsArray.length(); i++) {
                    JSONObject region = regionsArray.getJSONObject(i);
                    int x = region.getInt("x");
                    int y = region.getInt("y");
                    int z = region.getInt("z");
                    String player = region.getString("player");
                    // Assuming world is the default world for this example
                    Location location = new Location(Bukkit.getWorlds().get(0), x, y, z);

                    newRegionsMap.computeIfAbsent(player, k -> new HashSet<>()).add(location);
                }
            } catch (IOException | org.json.JSONException e) {
                e.printStackTrace();
            }

            // Update the regions map in the main thread
            Bukkit.getScheduler().runTask(plugin, () -> regionsMap = newRegionsMap);
        });
    }

    private void placeRegion(Location location, Player player, File file) {
        try (FileReader reader = new FileReader(file)) {
            // Чтение и парсинг JSON файла
            JSONTokener tokener = new JSONTokener(reader);
            JSONObject jsonObject = new JSONObject(tokener);
            JSONArray regionsArray = jsonObject.getJSONArray("Regions");

            int count = 0;
            // Подсчет записей с ником игрока
            for (int i = 0; i < regionsArray.length(); i++) {
                JSONObject region = regionsArray.getJSONObject(i);
                if (region.getString("player").equals(player.getName())) {
                    count++;
                }
            }
            if (count < 3 || player.isOp()) {
                JSONObject newRegion = new JSONObject();
                newRegion.put("player", player.getName());
                newRegion.put("x", location.getBlockX());
                newRegion.put("y", location.getBlockY());
                newRegion.put("z", location.getBlockZ());
                regionsArray.put(newRegion);
                jsonObject.put("Regions", regionsArray);

                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(jsonObject.toString(4)); // 4 - это количество пробелов для форматирования
                    writer.flush();
                    player.sendMessage(ChatColor.GREEN + "Новый регион успешно добавлен!");
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Обновление regionsMap
                regionsMap.computeIfAbsent(player.getName(), k -> new HashSet<>()).add(location);
            } else {
                player.sendMessage(ChatColor.YELLOW + "У вас уже есть 3 региона. Если это не декоративный флаг, обратитесь к администрации");
            }

        } catch (IOException | org.json.JSONException e) {
            e.printStackTrace();
        }
    }
}
