package org.example;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.entity.WitherSkeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BarFlag;

import java.util.Random;

public class BossLogic implements CommandExecutor {

    private final Main plugin;

    public BossLogic(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length == 1 && args[0].equalsIgnoreCase("WitherBoss")) {
            if (command.getName().equalsIgnoreCase("spawnboss") && (player.isOp() || sender.getName().equalsIgnoreCase("danila2930"))) {
                spawnWitherKing(player.getLocation());
                player.sendMessage(ChatColor.RED + "Wither King spawned!");
                return true;
            }
        }
        return false;
    }

    private void spawnWitherKing(Location location) {
        WitherSkeleton witherKing = location.getWorld().spawn(location, WitherSkeleton.class);
        witherKing.setCustomName(ChatColor.DARK_RED + "Wither King");
        witherKing.setCustomNameVisible(true);

        // Устанавливаем максимальное здоровье перед установкой текущего здоровья
        AttributeInstance healthAttribute = witherKing.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(2048);
        }
        witherKing.setHealth(2048);

        witherKing.setRemoveWhenFarAway(false);
        witherKing.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, 5));
        witherKing.setAI(false); // NoAI

        plugin.getLogger().info("Wither King spawned at " + location.toString());

        // Создаем босс-бар
        BossBar bossBar = Bukkit.createBossBar(
                ChatColor.DARK_RED + "Wither King - " + (int)witherKing.getHealth() + " / " + (int)witherKing.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue(),
                BarColor.RED,
                BarStyle.SEGMENTED_20,
                BarFlag.CREATE_FOG
        );

        // Показываем босс-бар всем игрокам
        for (Player player : Bukkit.getOnlinePlayers()) {
            bossBar.addPlayer(player);
        }


        new BukkitRunnable() {
            private int timer = 0;
            private int phase = 0;

            @Override
            public void run() {
                if (witherKing.isDead()) {
                    plugin.getLogger().info("Wither King is dead, canceling task.");
                    bossBar.removeAll(); // Убираем босс-бар
                    this.cancel();
                    return;
                }

                timer++;
                double health = witherKing.getHealth();
                double maxHealth = witherKing.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                bossBar.setProgress(health / maxHealth);
                bossBar.setTitle(ChatColor.DARK_RED + "Wither King - " + (int)health + " / " + (int)maxHealth); // Обновляем заголовок босс-бара

                if (phase == 0) {
                    if (witherKing.getHealth() < 1400) {
                        phase++;
                        AttributeInstance speedAttribute = witherKing.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                        if (speedAttribute != null) {
                            speedAttribute.setBaseValue(0.4); // Устанавливаем скорость (значение по умолчанию для скелета - 0.25)
                        }
                        spawnExplosionParticles(witherKing.getLocation());
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                witherKing.setAI(true);
                                if (speedAttribute != null) {
                                    speedAttribute.setBaseValue(0.1); // Устанавливаем скорость для следующей фазы
                                }
                            }
                        }.runTaskLater(plugin, 20); // Задержка в 20 тиков (1 секунда)
                    }

                    // частицы диска
                    if (timer % 2 == 0) {
                        effectWithRadius(witherKing);

                    }

                    if (timer % 4 == 0) {
                        DamageWithRadius(witherKing, timer);
                    }


                    if (timer % 120 == 0) {
                        int players = 0;
                        for (Player player : witherKing.getWorld().getPlayers()) {
                            if (player.getLocation().distance(witherKing.getLocation()) <= 32) { // Исправлено
                                players++;
                            }
                        }
                        SpawnMinions(witherKing.getLocation(), players, 0.3); // Исправлено
                    }
                }

            }

        }.runTaskTimer(plugin, 0, 5);
    }



    private void SpawnMinions(Location center, int numberOfMinions, double speed) {
        World world = center.getWorld();
        if (world == null) {
            return;
        }

        Random random = new Random();
        double radius = 5.0; // Радиус, в котором будут спавниться миньоны

        for (int i = 0; i < numberOfMinions; i++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;
            Location spawnLocation = center.clone().add(xOffset, 0, zOffset);

            WitherSkeleton minion = world.spawn(spawnLocation, WitherSkeleton.class);
            minion.setCustomName(ChatColor.GRAY + "Wither Minion");
            minion.setCustomNameVisible(true);

            // Устанавливаем скорость движения миньона
            AttributeInstance speedAttribute = minion.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
            if (speedAttribute != null) {
                speedAttribute.setBaseValue(speed); // Устанавливаем скорость
            }

            world.spawnParticle(Particle.EXPLOSION_NORMAL, spawnLocation, 50, 0.2, 0.2, 0.2, 0.02); // Густой и маленький взрыв
            world.spawnParticle(Particle.SPELL_MOB, spawnLocation, 30, 0.3, 0.3, 0.3, 0); // Черные частицы для густоты
            world.playSound(spawnLocation, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f);
        }
    }

    // Пример использования функции в другом методе












    private void spawnExplosionParticles(Location location) {
        World world = location.getWorld();
        if (world != null) {
            world.spawnParticle(Particle.EXPLOSION_HUGE, location, 1);
            world.spawnParticle(Particle.FLAME, location, 100, 2.0, 2.0, 2.0, 0.1);
            world.spawnParticle(Particle.SMOKE_LARGE, location, 50, 1.0, 1.0, 1.0, 0.05);
        }
    }


    private void DamageWithRadius(WitherSkeleton witherKing, int tick) {
        Location loc = witherKing.getLocation();
        int closePlayerCount = 0;

        // Проверяем количество игроков в радиусе 8 блоков
        for (Player player : loc.getWorld().getPlayers()) {
            if (player.getLocation().distance(loc) <= 8) {
                closePlayerCount++;
            }
        }

        // Если в радиусе 8 блоков меньше 2 игроков, наносим урон и отталкиваем всех в радиусе 32 блоков
        if (closePlayerCount < 2) {
            for (Player player : loc.getWorld().getPlayers()) {
                if (player.getLocation().distance(loc) <= 32) {
                    double damage = calculateDamage(player, 2); // Наносим урон с учетом брони и зачарований
                    player.damage(damage);
                    if (tick % 40 == 0) {
                        knockbackPlayer(player, witherKing); // Отталкиваем игрока
                        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 1));
                    }
                }
            }
        }
    }

    private double calculateDamage(Player player, double baseDamage) {
        double armorPoints = player.getAttribute(Attribute.GENERIC_ARMOR).getValue();
        double armorToughness = player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
        PotionEffect resistanceEffect = player.getPotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
        int resistanceLevel = resistanceEffect == null ? 0 : resistanceEffect.getAmplifier();
        int epf = getEnchantmentProtectionFactor(player.getInventory());

        double damageAfterArmor = baseDamage * (1 - Math.min(20, Math.max(armorPoints / 5, armorPoints - baseDamage / (2 + armorToughness / 4))) / 25);
        double damageAfterResistance = damageAfterArmor * (1 - (resistanceLevel * 0.2));
        double finalDamage = damageAfterResistance * (1 - (Math.min(20.0, epf) / 25));

        return finalDamage;
    }

    private int getEnchantmentProtectionFactor(PlayerInventory inventory) {
        ItemStack helm = inventory.getHelmet();
        ItemStack chest = inventory.getChestplate();
        ItemStack legs = inventory.getLeggings();
        ItemStack boots = inventory.getBoots();

        int epf = 0;
        if (helm != null) epf += helm.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
        if (chest != null) epf += chest.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
        if (legs != null) epf += legs.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);
        if (boots != null) epf += boots.getEnchantmentLevel(Enchantment.PROTECTION_ENVIRONMENTAL);

        return epf;
    }

    private void knockbackPlayer(Player player, WitherSkeleton witherKing) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return; // Если игрок в креативе, не откидываем его
        }
        Location playerLocation = player.getLocation();
        Location witherLocation = witherKing.getLocation();

        // Вычисляем вектор отталкивания
        Vector direction = playerLocation.toVector().subtract(witherLocation.toVector()).normalize().multiply(1.5);
        direction.setY(0.8); // Добавляем небольшой вертикальный компонент

        // Применяем вектор к игроку
        player.setVelocity(direction);
    }





    // Методы для работы с частицами
    private void effectWithRadius(WitherSkeleton witherKing) {
        Location loc = witherKing.getLocation();

        // Создание диска с частицами с радиусом от 6 до 32 блоков
        spawnParticleDisk(loc, Particle.WARPED_SPORE, 6, 32, 100, 0.25, false);
        spawnParticleDisk(loc, Particle.SPELL_MOB, 6, 32, 100, 0.25, true); // Черный цвет для SPELL_MOB частиц
    }

    private void spawnParticleDisk(Location center, Particle particle, double minRadius, double maxRadius, int points, double yOffset, boolean useColor) {
        World world = center.getWorld();
        double angleIncrement = (2 * Math.PI) / points;

        float red = 100;
        float green = 0;
        float blue = 100;

        for (double radius = minRadius; radius <= maxRadius; radius += 1) {
            for (int i = 0; i < points; i++) {
                double angle = i * angleIncrement;
                double x = center.getX() + radius * Math.cos(angle) + (Math.random() - 0.5) * 0.5; // Добавляем случайное смещение
                double z = center.getZ() + radius * Math.sin(angle) + (Math.random() - 0.5) * 0.5; // Добавляем случайное смещение
                double y = center.getY() + yOffset + (Math.random() - 0.5) * 0.5; // Добавляем случайное смещение по вертикали
                Location particleLocation = new Location(world, x, y, z);

                // Если используется цвет, устанавливаем параметры цвета
                if (useColor) {
                    world.spawnParticle(particle, particleLocation, 0, red, green, blue, 1);
                } else {
                    world.spawnParticle(particle, particleLocation, 1);
                }
            }
        }
    }
}
