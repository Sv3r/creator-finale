package be.sv3r.listener;

import be.sv3r.CreatorFinale;
import be.sv3r.command.CreatorFinalCommand;
import be.sv3r.handler.GameHandler;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
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
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;


public class PlayerListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (!GameHandler.COMPETING_PLAYERS.contains(player)) return;

        Sound sound = Sound.sound(Key.key("finale.death.sound"), Sound.Source.MASTER, 1F, 1F);

        TextComponent deathMessage = Component.text("\ue002 ")
                .append(event.getPlayer().displayName())
                .color(TextColor.color(0xffffff))
                .decorate(TextDecoration.BOLD)
                .append(Component.text(" is uitgeschakeld!", TextColor.color(0xf0544f)));

        for(Player allPlayers : Bukkit.getOnlinePlayers()){
            allPlayers.playSound(sound, Sound.Emitter.self());
            sendLongActionBar(allPlayers, deathMessage, 15);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event){
        if (event.getPlayer().hasPermission("group.crude") && !CreatorFinalCommand.canMove){
            event.setCancelled(true);
        }
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChat(PlayerChatEvent event){
        if (!event.getPlayer().hasPermission("creator.finale.talk") && GameHandler.started){
            event.setCancelled(true);
        }
    }

    public void sendLongActionBar(Player player, TextComponent message, int durationInSeconds) {
        int displayTimeTicks = 20;
        int repeatTimes = (durationInSeconds * 20) / displayTimeTicks;

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count < repeatTimes && !CreatorFinalCommand.worldBorder) {
                    player.sendActionBar(message);
                    count++;
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(CreatorFinale.getPlugin(), 0, displayTimeTicks);
    }
}
