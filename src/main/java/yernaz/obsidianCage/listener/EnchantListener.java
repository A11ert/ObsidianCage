package yernaz.obsidianCage.listener;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import yernaz.obsidianCage.enchant.ObsidianCageEnchant;

import java.lang.constant.Constable;
import java.lang.reflect.Constructor;

public class EnchantListener implements Listener {

    private final ObsidianCageEnchant obsCageEnchant;

    public EnchantListener(ObsidianCageEnchant obsCageEnchant){
        this.obsCageEnchant=obsCageEnchant;
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player damager)) return;
        ItemStack weapon = damager.getInventory().getItemInMainHand();
        if(weapon.getType()!= Material.DIAMOND_SWORD)return;
        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity victim)) return;

        obsCageEnchant.apply(damager, victim);

    }

}
