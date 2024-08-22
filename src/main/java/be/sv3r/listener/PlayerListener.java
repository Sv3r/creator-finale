package be.sv3r.listener;

import be.sv3r.CreatorFinale;
import be.sv3r.handler.GameHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.UUID;

public class PlayerListener implements Listener {
    public final HashMap<UUID, Location> deathLocations = new HashMap<>();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (!GameHandler.COMPETING_PLAYERS.contains(player)) return;

        UUID playerUuid = player.getUniqueId();

        deathLocations.put(playerUuid, player.getLocation());
        player.setGameMode(GameMode.SPECTATOR);
        player.getInventory().clear();



        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playsound records.test player @a");

        event.getDrops().clear();
        event.setKeepLevel(true);
        event.setShouldDropExperience(false);

        CreatorFinale.getPlugin().getServer().getScheduler().runTaskLater(CreatorFinale.getPlugin(), () -> player.spigot().respawn(), 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event){
        if (!event.getPlayer().hasPermission("creator.finale.*")){
            event.setCancelled(GameHandler.started);
        }
    }
}
