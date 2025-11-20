package yernaz.obsidianCage;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import yernaz.obsidianCage.enchant.ObsidianCageEnchant;
import yernaz.obsidianCage.listener.EnchantListener;

public final class ObsidianCage extends JavaPlugin {

    private static ObsidianCage instance;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("ObsidianCage plugin enabled!");
        // Plugin startup logic
        ObsidianCageEnchant cageEnchant = new ObsidianCageEnchant();
        EnchantListener enchantListener = new EnchantListener(cageEnchant);

        Bukkit.getPluginManager().registerEvents(enchantListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static ObsidianCage getInstance() {
        return instance;
    }
}
