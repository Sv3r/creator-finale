package be.sv3r.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public abstract class SimpleCommand {

    protected final String label;
    protected final String description;
    protected final List<String> aliases;
    protected final String permission;

    public SimpleCommand(String label, String description, List<String> aliases, String permission) {
        this.label = label;
        this.description = description;
        this.aliases = aliases;
        this.permission = permission;
    }

    public void register(JavaPlugin plugin, Commands commands) {
        final LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal(this.label).executes(this::onRoot)
                .requires(source -> source.getSender().hasPermission(permission));
        commands.register(plugin.getPluginMeta(), commandBuilder.build(), this.description, this.aliases);
    }

    public abstract int onRoot(final CommandContext<CommandSourceStack> context);
}
