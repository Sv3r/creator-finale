package be.sv3r.command;

import be.sv3r.command.annotation.SubCommand;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AnnotatedCommand extends SimpleCommand {
    private final Set<Method> methods = new HashSet<>();

    public AnnotatedCommand(String label, String description, List<String> aliases) {
        super(label, description, aliases, "creator.command." + label);
        findMethods();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void register(JavaPlugin plugin, Commands commands) {
        final LiteralArgumentBuilder<CommandSourceStack> commandBuilder = Commands.literal(this.label).executes(this::onRoot)
                .requires(source -> source.getSender().hasPermission(this.permission));

        for (Method method : methods) {
            ArgumentBuilder<CommandSourceStack, ?> value;

            try {
                value = (ArgumentBuilder<CommandSourceStack, ?>) method.invoke(this);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }

            commandBuilder.then(value);
        }

        commands.register(plugin.getPluginMeta(), commandBuilder.build(), this.description, this.aliases);
    }

    private void findMethods() {
        for (Method method : this.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(SubCommand.class)) {
                int modifiers = method.getModifiers();
                if (Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)) {
                    throw new RuntimeException("Method " + method.getName() + " in class " + this.getClass().getSimpleName() + " must be public and non-static!");
                }

                this.methods.add(method);
            }
        }
    }
}
