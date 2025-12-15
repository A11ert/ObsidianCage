package yernaz.obsidianCage.comands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import yernaz.obsidianCage.ObsidianCage;
import yernaz.obsidianCage.items.TrapItem;

public final class TrapCommand implements CommandExecutor {

    private final ObsidianCage plugin;

    public TrapCommand(ObsidianCage plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (!(sender instanceof Player p)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        int amount = 1;
        if (args.length >= 1) {
            try {
                amount = Math.max(1, Math.min(64, Integer.parseInt(args[0])));
            } catch (NumberFormatException ignored) {}
        }

        p.getInventory().addItem(TrapItem.create(plugin)).forEach((slot, item) -> {
            // if inventory is full, drop it
            p.getWorld().dropItemNaturally(p.getLocation(), item);
        });

        // if you want amount > 1:
        // for (int i = 0; i < amount; i++) p.getInventory().addItem(TrapItem.create(plugin));

        p.sendMessage(ChatColor.GREEN + "You got Trap x" + amount);
        return true;
    }
}
