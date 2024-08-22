package be.sv3r.listener;

import be.sv3r.CreatorFinale;
import be.sv3r.handler.GameHandler;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.w3c.dom.Text;

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

        Sound sound = Sound.sound(Key.key("finale.death.sound"), Sound.Source.MASTER, 1F, 1F);

        TextComponent deathMessage = Component.text("\ue002 ")
                .append(event.getPlayer().displayName()) // Use directly without converting to string
                .color(TextColor.color(0xffffff))
                .decorate(TextDecoration.BOLD)
                .append(Component.text(" is uitgeschakeld!", TextColor.color(0xf0544f), TextDecoration.BOLD));

        for(Player allPlayers : Bukkit.getOnlinePlayers()){
            allPlayers.playSound(sound, Sound.Emitter.self());

            sendLongActionBar(allPlayers, deathMessage, 15);
        }

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

    public void sendLongActionBar(Player player, TextComponent message, int durationInSeconds) {
        int displayTimeTicks = 20;
        int repeatTimes = (durationInSeconds * 20) / displayTimeTicks;

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count < repeatTimes) {
                    player.sendActionBar(message);
                    count++;
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(CreatorFinale.getPlugin(), 0, displayTimeTicks);
    }
}
