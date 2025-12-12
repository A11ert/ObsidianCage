package yernaz.obsidianCage;

import org.bukkit.plugin.java.JavaPlugin;
import yernaz.obsidianCage.comands.TrapCommand;
import yernaz.obsidianCage.enchant.ObsidianCageEnchant;
import yernaz.obsidianCage.listener.EnchantListener;
import yernaz.obsidianCage.listener.TrapUserListener;

import static org.bukkit.Bukkit.getServer;

public final class ObsidianCage extends JavaPlugin {

    private static ObsidianCage instance;

    private ObsidianCageEnchant cageEnchant;

    @Override
    public void onEnable() {
        instance = this;
        getCommand("trap").setExecutor(new TrapCommand(this));
        cageEnchant = new ObsidianCageEnchant();

        getServer().getPluginManager().registerEvents(
                new EnchantListener(cageEnchant),
                this
        );

        getServer().getPluginManager().registerEvents(
                new TrapUserListener(this, cageEnchant),
                this
        );
    }



    public static ObsidianCage getInstance() {
        return instance;
    }
}
