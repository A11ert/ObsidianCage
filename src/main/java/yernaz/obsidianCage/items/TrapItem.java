package yernaz.obsidianCage.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import yernaz.obsidianCage.ObsidianCage;

import java.util.List;

public final class TrapItem {

    private TrapItem() {}

    public static NamespacedKey KEY(ObsidianCage plugin) {
        return new NamespacedKey(plugin, "trap_item");
    }

    public static ItemStack create(ObsidianCage plugin) {
        ItemStack item = new ItemStack(Material.IRON_INGOT);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.DARK_RED + "Trap");
        meta.setLore(List.of(
                ChatColor.GRAY + "Right-click to spawn an",
                ChatColor.GRAY + "Obsidian Cage around you."
        ));

        meta.getPersistentDataContainer().set(KEY(plugin), PersistentDataType.BYTE, (byte) 1);

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isTrap(ObsidianCage plugin, ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        Byte v = meta.getPersistentDataContainer().get(KEY(plugin), PersistentDataType.BYTE);
        return v != null && v == (byte) 1;
    }
}
