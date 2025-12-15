package yernaz.obsidianCage.enchant;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import yernaz.obsidianCage.ObsidianCage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ObsidianCageEnchant {

    private static final long CAGE_COOLDOWN_MS = 10_000L; // 10 sec

    // last time player used cage
    private final Map<UUID, Long> cageCooldowns = new HashMap<>();

    // blocks that belong to this player's current cage
    private final Map<UUID, Set<Block>> activeCages = new HashMap<>();

    // original block states before cage replaced them
    private final Map<UUID, Map<Block, BlockState>> originalBlockStates = new HashMap<>();

    // scheduled task id to auto-remove cage
    private final Map<UUID, Integer> removalTasks = new HashMap<>();

    /**
     * Call this when the player hits the target (from your listener)
     */

    public void applyTrap(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // cooldown
        long lastUse = cageCooldowns.getOrDefault(uuid, 0L); // <-- make sure this is cageCooldowns (no extra n)
        if (now - lastUse < CAGE_COOLDOWN_MS) {
            long remaining = (CAGE_COOLDOWN_MS - (now - lastUse) + 999) / 1000;
            player.sendMessage(ChatColor.RED + "✗ Подожди ещё " + remaining + " сек!");
            return;
        }
        cageCooldowns.put(uuid, now);

        // remove old cage for this player
        clearCage(uuid);

        World world = player.getWorld();
        Location center = player.getLocation().toBlockLocation();
        int radius = 3;

        Map<Block, BlockState> originals = new HashMap<>();
        Set<Block> cageBlocks = buildCage(world, center, radius, originals);

        activeCages.put(uuid, cageBlocks);
        originalBlockStates.put(uuid, originals);

        player.playSound(center, Sound.ITEM_TOTEM_USE, 1f, 0.6f);
        world.spawnParticle(
                Particle.ENCHANT,
                center.clone().add(0.5, 1, 0.5),
                80, 1.3, 1.2, 1.3, 0.1
        );

        scheduleRemoval(uuid);
    }


    public void apply(Player damager, Entity target) {
        UUID uuid = damager.getUniqueId();
        long now = System.currentTimeMillis();

        // ---- COOLDOWN ----
        long lastUse = cageCooldowns.getOrDefault(uuid, 0L);
        if (now - lastUse < CAGE_COOLDOWN_MS) {
            long remaining = (CAGE_COOLDOWN_MS - (now - lastUse)) / 1000;
            damager.sendMessage(ChatColor.RED + "❌ Подожди ещё " + remaining + " сек!");
            return;
        }
        cageCooldowns.put(uuid, now);

        // ---- remove old cage + timer for this player ----
        clearCage(uuid);

        // ---- teleport to target ----
        Location from = damager.getLocation();
        Location to = target.getLocation();
        to.setYaw(from.getYaw());
        to.setPitch(from.getPitch());
        damager.teleport(to);

        World world = target.getWorld();
        Location center = target.getLocation().toBlockLocation();
        int radius = 3;

        // store originals of all blocks we touch
        Map<Block, BlockState> originals = new HashMap<>();

        // build cage, clearing the inside to AIR
        Set<Block> cageBlocks = buildCage(world, center, radius, originals);

        activeCages.put(uuid, cageBlocks);
        originalBlockStates.put(uuid, originals);

        damager.playSound(center, Sound.ITEM_TOTEM_USE, 1f, 0.6f);
        world.spawnParticle(
                Particle.ENCHANT,
                center.clone().add(0.5, 1, 0.5),
                80, 1.3, 1.2, 1.3, 0.1
        );

        // auto-remove after 60 seconds
        scheduleRemoval(uuid);
    }

    /**
     * Builds a hollow cage, clears interior to AIR, adds chain + lantern,
     * and remembers all previous block states in originalBlocks.
     */
    private Set<Block> buildCage(World world,
                                 Location center,
                                 int radius,
                                 Map<Block, BlockState> originalBlocks) {

        Set<Block> cageBlocks = new HashSet<>();

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();

        int minX = cx - radius;
        int maxX = cx + radius;
        int minZ = cz - radius;
        int maxZ = cz + radius;
        int minY = cy - radius + 2;
        int maxY = cy + radius + 2;

        // walls / floor / roof + clear inside
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {

                    boolean shell = (x == minX || x == maxX
                            || y == minY || y == maxY
                            || z == minZ || z == maxZ);

                    Block block = world.getBlockAt(x, y, z);
                    // remember what was here before cage
                    originalBlocks.put(block, block.getState());

                    if (shell) {
                        // walls, floor, roof
                        Material type = Material.OBSIDIAN;
                        if((y==maxY || y==minY)){
                            type=Material.CRYING_OBSIDIAN;
                        }
                        block.setType(type, false);
                    } else {
                        block.setType(Material.AIR, false);
                    }

                    cageBlocks.add(block);
                }
            }
        }

        // chain one block under roof, in center
        Block chainBlock = world.getBlockAt(cx, maxY - 1, cz);
        originalBlocks.putIfAbsent(chainBlock, chainBlock.getState());
        chainBlock.setType(Material.CHAIN, false);
        cageBlocks.add(chainBlock);

        // lantern one block under chain
        Block lampBlock = world.getBlockAt(cx, maxY - 2, cz);
        originalBlocks.putIfAbsent(lampBlock, lampBlock.getState());
        lampBlock.setType(Material.LANTERN, false);
        cageBlocks.add(lampBlock);

        return cageBlocks;
    }

    /**
     * Restore all blocks for this player and forget cage.
     */
    private void restoreCage(UUID uuid) {
        Set<Block> blocks = activeCages.remove(uuid);
        Map<Block, BlockState> originals = originalBlockStates.remove(uuid);

        if (blocks == null) return;

        if (originals != null) {
            for (Block b : blocks) {
                BlockState old = originals.get(b);
                if (old != null) {
                    // restore old block exactly (type, data, inventory, etc.)
                    old.update(true, false);
                } else {
                    // fallback if something went wrong
                    if (b.getType() == Material.OBSIDIAN
                            || b.getType() == Material.CRYING_OBSIDIAN
                            || b.getType() == Material.CHAIN
                            || b.getType() == Material.LANTERN) {
                        b.setType(Material.AIR, false);
                    }
                }
            }
        } else {
            // no saved states (shouldn't happen, but safe)
            for (Block b : blocks) {
                if (b.getType() == Material.OBSIDIAN
                        || b.getType() == Material.CRYING_OBSIDIAN
                        || b.getType() == Material.CHAIN
                        || b.getType() == Material.LANTERN) {
                    b.setType(Material.AIR, false);
                }
            }
        }
    }

    /**
     * Remove cage and cancel timer (used before creating a new one).
     */
    public void clearCage(UUID uuid) {
        // restore blocks
        restoreCage(uuid);

        // cancel old scheduled removal
        Integer oldTaskId = removalTasks.remove(uuid);
        if (oldTaskId != null) {
            Bukkit.getScheduler().cancelTask(oldTaskId);
        }
    }

    /**
     * Schedule auto-remove after 60 seconds.
     */
    private void scheduleRemoval(UUID uuid) {
        // cancel previous task if exists
        Integer oldTaskId = removalTasks.remove(uuid);
        if (oldTaskId != null) {
            Bukkit.getScheduler().cancelTask(oldTaskId);
        }

        int taskId = new BukkitRunnable() {
            @Override
            public void run() {
                restoreCage(uuid);
                Player p = Bukkit.getPlayer(uuid);
                if (p != null && p.isOnline()) {
                    p.sendMessage(ChatColor.GRAY + "☁ Коробка исчезла.");
                }
                removalTasks.remove(uuid);
            }
        }.runTaskLater(ObsidianCage.getInstance(), 20L * 60).getTaskId(); // 60 sec

        removalTasks.put(uuid, taskId);
    }
}
