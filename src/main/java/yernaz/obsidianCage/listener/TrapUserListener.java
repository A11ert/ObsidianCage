package yernaz.obsidianCage.listener;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import yernaz.obsidianCage.ObsidianCage;
import yernaz.obsidianCage.enchant.ObsidianCageEnchant;
import yernaz.obsidianCage.items.TrapItem;

public final class TrapUserListener implements Listener {

    private final ObsidianCage plugin;
    private final ObsidianCageEnchant cage;

    public TrapUserListener(ObsidianCage plugin, ObsidianCageEnchant cage) {
        this.plugin = plugin;
        this.cage = cage;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        // prevent double trigger (main hand + offhand)
        if (e.getHand() != EquipmentSlot.HAND) return;

        Action a = e.getAction();
        if (a != Action.RIGHT_CLICK_AIR && a != Action.RIGHT_CLICK_BLOCK) return;

        Player p = e.getPlayer();
        ItemStack item = p.getInventory().getItemInMainHand();

        if (!TrapItem.isTrap(plugin, item)) return;

        e.setCancelled(true); // stop vanilla behavior

        cage.applyTrap(p);

        // consume 1 item in survival/adventure
        if (p.getGameMode() != GameMode.CREATIVE) {
            int amount = item.getAmount();
            if (amount <= 1) p.getInventory().setItemInMainHand(null);
            else item.setAmount(amount - 1);
        }
    }
}
