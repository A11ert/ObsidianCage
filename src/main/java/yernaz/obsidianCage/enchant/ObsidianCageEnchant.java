package yernaz.obsidianCage.enchant;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import yernaz.obsidianCage.ObsidianCage;

import java.util.*;

public class ObsidianCageEnchant {

    private final Map<UUID, Long> cageCooldowns = new HashMap<>();
    private static final long CAGE_COOLDOWN_MS = 10_000;
    private final Map<UUID, Set<Block>> activeCages = new HashMap<>();
    private final Map<UUID, Bukkit> cageDeleteTime = new HashMap<>();

   // Основной метод — вызывать при ударе
    public void apply(Player damager, Entity target) {
        long now = System.currentTimeMillis();
        if (now - cageCooldowns.getOrDefault(damager.getUniqueId(), 0L) < CAGE_COOLDOWN_MS) {
            long remaining = (CAGE_COOLDOWN_MS - (now - cageCooldowns.get(damager.getUniqueId()))) / 1000;
            damager.sendMessage(ChatColor.RED + "❌ Подожди ещё " + remaining + " сек!");
            return;
        }
        cageCooldowns.put(damager.getUniqueId(), now);

        // Удаляем старую коробку игрока
        removeCage(damager);

        // Телепорт игрока к цели
        Location from = damager.getLocation();
        Location to = target.getLocation();
        to.setYaw(from.getYaw());
        to.setPitch(from.getPitch());
        damager.teleport(to);

        World world = target.getWorld();
        Location center = target.getLocation().toBlockLocation();
        int r = 3;

        // Построение новой коробки
        Set<Block> cageBlocks = buildCage(world, center, r);
        activeCages.put(damager.getUniqueId(), cageBlocks);

        damager.playSound(center, Sound.ITEM_TOTEM_USE, 1f, 0.6f);
        world.spawnParticle(Particle.ENCHANT, center.clone().add(0.5, 1, 0.5), 80, 1.3, 1.2, 1.3, 0.1);

        // 🕒 Удаление через 20 секунд
        if()
        Bukkit.getScheduler().runTaskLater(ObsidianCage.getInstance(), () -> {
            removeCage(damager);
            damager.sendMessage(ChatColor.GRAY + "☁ Коробка исчезла.");
        }, 20L * 20);


    }

    // Создание обсидиановой коробки
    private Set<Block> buildCage(World world, Location center, int r) {
        Set<Block> cageBlocks = new HashSet<>();
        int minY = center.getBlockY() - r + 2;
        int maxY = center.getBlockY() + r + 2;

        for (int x = center.getBlockX() - r; x <= center.getBlockX() + r; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = center.getBlockZ() - r; z <= center.getBlockZ() + r; z++) {
                    boolean wall = (x == center.getBlockX() - r || x == center.getBlockX() + r
                            || y == minY || y == maxY
                            || z == center.getBlockZ() - r || z == center.getBlockZ() + r);
                    if (!wall) continue;

                    Block block = world.getBlockAt(x, y, z);
                    Material type = (y == maxY) ? Material.CRYING_OBSIDIAN : Material.OBSIDIAN;
                    block.setType(type, false);
                    cageBlocks.add(block);
                }
            }
        }
        return cageBlocks;
    }

    // Удаление текущей коробки игрока
    private void removeCage(Player player) {
        Set<Block> blocks = activeCages.remove(player.getUniqueId());
        if (blocks == null) return;

        for (Block b : blocks) {
            if (b.getType() == Material.OBSIDIAN ||
                    b.getType() == Material.CRYING_OBSIDIAN) {
                b.setType(Material.AIR, false);
            }
        }
    }
}
