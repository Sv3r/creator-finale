package be.sv3r.util;

import be.sv3r.CreatorFinale;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

public class EntityUtil {
    public static void spawnFirework(Location location, Color color, long detonateTime) {
        ItemStack fireworkItem = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta meta = (FireworkMeta) fireworkItem.getItemMeta();
        meta.addEffect(FireworkEffect.builder().withColor(color).with(FireworkEffect.Type.BALL).withFlicker().withTrail().build());
        meta.setPower(3);
        fireworkItem.setItemMeta(meta);

        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK_ROCKET);
        firework.setItem(fireworkItem);

        Bukkit.getScheduler().runTaskLater(CreatorFinale.getPlugin(), firework::detonate, detonateTime);
    }
}
