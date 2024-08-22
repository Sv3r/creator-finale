package be.sv3r;

import be.sv3r.command.CreatorFinalCommand;
import be.sv3r.handler.ConfigHandler;
import be.sv3r.listener.PlayerListener;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class CreatorFinale extends JavaPlugin {
    public static final Logger LOGGER = LoggerFactory.getLogger("creator-finale");

    private static CreatorFinale instance;
    private ConfigHandler configHandler;

    public static CreatorFinale getPlugin() {
        return getPlugin(CreatorFinale.class);
    }

    @Override
    public void onEnable() {
        ConfigHandler.getInstance().load();
        registerCommands();
        registerListeners();
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
    }

    private void registerCommands(){
        final LifecycleEventManager<Plugin> lifecycleManager = this.getLifecycleManager();

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            new CreatorFinalCommand().register(this, commands);
        });
    }
}
