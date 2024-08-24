package be.sv3r.command;

import be.sv3r.CreatorFinale;
import be.sv3r.command.annotation.SubCommand;
import be.sv3r.handler.ConfigHandler;
import be.sv3r.handler.GameHandler;
import be.sv3r.task.CountdownTask;
import be.sv3r.util.EntityUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Stray;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.*;

public class CreatorFinalCommand extends AnnotatedCommand {

    public static boolean worldBorder = false;
    public static boolean canMove = true;

    private final HashMap<UUID, Integer> playerStrikes = new HashMap<>();

    public CreatorFinalCommand() {
        super("creatorfinal", "Creator final command.", List.of("cf"));
    }

    @Override
    public int onRoot(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    @SubCommand
    public ArgumentBuilder<CommandSourceStack, ?> OnFinaleStart() {
        return Commands.literal("start")
                .then(Commands.argument("countdownTitle", ArgumentTypes.component())
                        .then(Commands.argument("stopTitle", ArgumentTypes.component())
                                .then(Commands.argument("color", ArgumentTypes.namedColor())
                                        .then(Commands.argument("duration", IntegerArgumentType.integer())
                                                .then(Commands.argument("worldborderSize", IntegerArgumentType.integer(1))
                                                    .executes(context -> {
                                                        CommandSourceStack sourceStack = context.getSource();

                                                        if (!GameHandler.started){
                                                            Component countdownTitle = context.getArgument("countdownTitle", Component.class);
                                                            Component stopTitle = context.getArgument("stopTitle", Component.class);
                                                            int duration = context.getArgument("duration", Integer.class);
                                                            NamedTextColor color = context.getArgument("color", NamedTextColor.class);
                                                            int worldborderSize = IntegerArgumentType.getInteger(context, "worldborderSize");

                                                            GameHandler.setupGame();

                                                            World world = Bukkit.getWorlds().getFirst();
                                                            world.getWorldBorder().setSize(worldborderSize);

                                                            CountdownTask countdownRunnable = new CountdownTask(
                                                                    CreatorFinale.getPlugin(),
                                                                    duration,
                                                                    () -> {
                                                                    },
                                                                    () -> stopCountdownCountdown(stopTitle, color),
                                                                    (task) -> duringCountdownCountdown(task, countdownTitle, color)
                                                            );
                                                            countdownRunnable.scheduleTask();

                                                        } else {
                                                            sourceStack.getSender().sendMessage("You need to reset the game to do it again /cf stopall");
                                                        }
                                                        return 0;
                                                    })
                                                )
                                        )
                                )
                        )
                );
    }
    @SubCommand
    public ArgumentBuilder<CommandSourceStack, ?> onSafeCommand() {
        return Commands.literal("stopall").executes((source) -> {
            CommandSourceStack sourceStack = source.getSource();

            GameHandler.started = false;
            canMove = true;

            sourceStack.getSender().sendMessage("Reseted the game!");

            return 0;
        });
    }
    @SubCommand
    public ArgumentBuilder<CommandSourceStack, ?> onSpawnPointSet() {
        return Commands.literal("spawnpointset")
                .then(Commands.argument("player", ArgumentTypes.player())
                        .executes((source) -> {
                            CommandSourceStack sourceStack = source.getSource();
                            Player player = source.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(sourceStack).getFirst();

                            Location location = player.getLocation();
                            ConfigHandler.getInstance().addSpawnpointLocation(location);
                            player.sendMessage(location.toString());

                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
    @SubCommand
    public ArgumentBuilder<CommandSourceStack, ?> onReload() {
        return Commands.literal("reload").executes((source) -> {
            CommandSourceStack sourceStack = source.getSource();

            ConfigHandler.getInstance().load();

            sourceStack.getSender().sendMessage("Reloaded the config!");

            return 0;
        });
    }

    @SubCommand
    public ArgumentBuilder<CommandSourceStack, ?> onStrike() {
        return Commands.literal("strike")
                .then(Commands.argument("player", ArgumentTypes.player())
                        .executes((source) -> {
                            CommandSourceStack sourceStack = source.getSource();
                            if (sourceStack.getSender().hasPermission("creator.finale.strike")) {
                                Player player = source.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(sourceStack).getFirst();

                                UUID playerUUID = player.getUniqueId();
                                int strikes = playerStrikes.getOrDefault(playerUUID, 0) + 1;

                                playerStrikes.put(playerUUID, strikes);

                                showStrikeTitle(player, Component.empty(), strikes);

                                if (strikes >= 3) {
                                    player.setHealth(0);
                                }

                            }
                            return Command.SINGLE_SUCCESS;
                        })
                );
    }
    @SubCommand
    public ArgumentBuilder<CommandSourceStack, ?> onWarn() {
        return Commands.literal("warn")
                .then(Commands.argument("player", ArgumentTypes.player())
                        .then(Commands.argument("warnText", ArgumentTypes.component())
                                .executes((source) -> {
                                    CommandSourceStack sourceStack = source.getSource();
                                    if (sourceStack.getSender().hasPermission("creator.finale.warn")){
                                        Player player = source.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(sourceStack).getFirst();
                                        Component warnText = source.getArgument("warnText", Component.class);

                                        showWWarnTitle(player, warnText);
                                    }
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                );
    }

    private static void showStrikeTitle(final Audience target, final Component strikeWarning, int strikes) {
        final Component mainTitle = Component.text("STRIKE WARNING: " + strikes, TextColor.color(0xf0544f), TextDecoration.BOLD);

        final Title title = Title.title(mainTitle, strikeWarning.style(Style.style(TextColor.color(0xffffff), TextDecoration.BOLD)));
        target.showTitle(title);
    }
    private static void showWWarnTitle(final Audience target, final Component strikeWarning) {
        final Component mainTitle = Component.text("WARNING", TextColor.color(0xf0544f), TextDecoration.BOLD);

        final Title title = Title.title(mainTitle, strikeWarning.style(Style.style(TextColor.color(0xffffff), TextDecoration.BOLD)));
        target.showTitle(title);
    }

    private static void stopCountdownCountdown(Component stopTitle, NamedTextColor color) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            final Title.Times times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1000), Duration.ofMillis(1000));

            final Title title = Title.title(stopTitle.style(Style.style(color, TextDecoration.BOLD)), Component.empty(), times);
            player.showTitle(title);

            GameHandler.started = true;
            canMove = true;

            Sound sound = Sound.sound(Key.key("item.goat_horn.sound.0"), Sound.Source.MASTER, 1F, 1F);
            player.playSound(sound, Sound.Emitter.self());
        });

        startBorderTimer();
    }

    private static void duringCountdownCountdown(CountdownTask task, Component duringTitle, NamedTextColor color) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            final Component subtitle = Component.text(">" + task.getSecondsLeft() + "<").style(Style.style(TextDecoration.BOLD, NamedTextColor.WHITE));

            final Title.Times times = Title.Times.times(Duration.ofMillis(0), Duration.ofMillis(1000), Duration.ofMillis(1000));
            final Title title = Title.title(duringTitle.style(Style.style(color, TextDecoration.BOLD)), subtitle, times);
            player.showTitle(title);

            Sound sound = Sound.sound(Key.key("entity.experience_orb.pickup"), Sound.Source.MASTER, 1F, 1F);
            player.playSound(sound, Sound.Emitter.self());
        });
    }
    private static void startBorderTimer() {
        int[] stageTimes = ConfigHandler.getInstance().getStageTimes(); // in seconds
        int[] borderSizes = ConfigHandler.getInstance().getBorderSizes();
        int pauseTime = ConfigHandler.getInstance().getPauseTime() * 60; // Convert minutes to seconds

        new BukkitRunnable() {
            @Override
            public void run() {
                if (GameHandler.stage < stageTimes.length) {
                    int time = stageTimes[GameHandler.stage]; // Stage time in seconds
                    int borderSize = borderSizes[GameHandler.stage]; // Border size for this stage
                    long delay = (time + pauseTime) * 20L; // Convert total time to ticks

                    CreatorFinale.LOGGER.info("Stage: {}\nTime: {}\nBorder Size: {}\n Next Delay: {}",
                            GameHandler.stage,
                            time,
                            borderSize,
                            delay
                    );

                    // Resize the world border and notify players
                    World world = Bukkit.getWorlds().getFirst();
                    world.getWorldBorder().setSize(borderSize, time);

                    TextComponent borderClosing = Component.text("\ue002 ")
                            .append(Component.text("Border is aan het krimpen!", TextColor.color(0xf0544f), TextDecoration.BOLD));

                    Bukkit.getOnlinePlayers().forEach(player -> {
                        Sound alarm = Sound.sound(Key.key("finale.border.alarm"), Sound.Source.MASTER, 1F, 1F);
                        player.playSound(alarm, Sound.Emitter.self());
                        sendLongActionBar(player, borderClosing, 15);
                    });

                    // Schedule the next stage after the current stage time plus pause time
                    GameHandler.stage++;
                    this.runTaskLater(CreatorFinale.getPlugin(), delay);
                } else {
                    // Cancel the task when all stages are complete
                    this.cancel();
                }
            }
        }.runTask(CreatorFinale.getPlugin()); // Start immediately
    }
    public static void sendLongActionBar(Player player, TextComponent message, int durationInSeconds) {
        int displayTimeTicks = 20;
        int repeatTimes = (durationInSeconds * 20) / displayTimeTicks;

        new BukkitRunnable() {
            int count = 0;

            @Override
            public void run() {
                if (count < repeatTimes) {
                    player.sendActionBar(message);
                    count++;

                    worldBorder = true;
                } else {
                    worldBorder = false;
                    player.stopAllSounds();
                    this.cancel();
                }
            }
        }.runTaskTimer(CreatorFinale.getPlugin(), 0, displayTimeTicks);
    }
}
