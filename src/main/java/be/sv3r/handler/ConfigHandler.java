package be.sv3r.handler;

import be.sv3r.CreatorFinale;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConfigHandler {
    private static final ConfigHandler instance = new ConfigHandler();

    private File file;
    private YamlConfiguration config;

    private final List<Location> spawnpointLocations = new ArrayList<>();
    private int[] stageTimes;
    private int[] borderSizes;
    private int pauseTime;

    private ConfigHandler() {
    }

    public void load() {
        file = new File(CreatorFinale.getPlugin().getDataFolder(), "config.yml");

        if (!file.exists()) {
            CreatorFinale.getPlugin().saveResource("config.yml", false);
        }

        config = new YamlConfiguration();
        config.options().parseComments(true);

        try {
            config.load(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        ConfigurationSection configurationSection = config.getConfigurationSection("spawnpoint-locations");
        if (configurationSection != null) {
            for (String key : configurationSection.getKeys(true)) {
                String path = "spawnpoint-locations.".concat(key);
                Location location = (Location) config.get(path);
                spawnpointLocations.add(location);
            }
        }
        List<Integer> stageTimesList = config.getIntegerList("stage-times");
        stageTimes = stageTimesList.stream().mapToInt(i -> i).toArray();

        List<Integer> borderSizesList = config.getIntegerList("border-sizes");
        borderSizes = borderSizesList.stream().mapToInt(i -> i).toArray();

        pauseTime = config.getInt("pause-time", 5);
    }

    public void save() {
        try {
            config.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void set(String path, Object value) {
        config.set(path, value);
        save();
    }

    public List<Location> getSpawnpointLocations() {
        return spawnpointLocations;
    }

    public void addSpawnpointLocation(Location location) {
        spawnpointLocations.add(location);
        set("spawnpoint-locations." + (spawnpointLocations.size() - 1), location);
    }

    public int[] getStageTimes() {
        return stageTimes;
    }

    public int[] getBorderSizes() {
        return borderSizes;
    }

    public int getPauseTime() {
        return pauseTime;
    }

    public static ConfigHandler getInstance() {
        return instance;
    }
}
