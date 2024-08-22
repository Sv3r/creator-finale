package be.sv3r.handler;

import be.sv3r.CreatorFinale;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class GameHandler {

    public static final Set<Player> COMPETING_PLAYERS = new HashSet<>();
    public static boolean started = false;


    public static void setupGame() {
        CreatorFinale.getPlugin().getServer().getScheduler().runTask(CreatorFinale.getPlugin(CreatorFinale.class), () -> {
            started = true;
            List<Location> locations = ConfigHandler.getInstance().getSpawnpointLocations();
            Collections.shuffle(locations, new Random());
            List<Player> PLAYER_LIST = (List<Player>) Bukkit.getOnlinePlayers().stream().toList();
            for (int i = 0; i < PLAYER_LIST.size(); i++) {
                Player player = PLAYER_LIST.get(i);
                if (!player.hasPermission("creator.finale.*")){
                    player.clearActivePotionEffects();
                    player.setHealth(20.0);
                    player.setFoodLevel(20);
                    player.setSaturation(0.0F);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.teleport(locations.get(i));
                    COMPETING_PLAYERS.add(player);
                }
            }
        });
    }

}
