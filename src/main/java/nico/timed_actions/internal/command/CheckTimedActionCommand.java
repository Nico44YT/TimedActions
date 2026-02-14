package nico.timed_actions.internal.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import nico.timed_actions.api.v1.TimedAction;
import nico.timed_actions.api.v1.TimedActionHolder;
import org.jetbrains.annotations.NotNull;

public class CheckTimedActionCommand {
    public static ArgumentBuilder<ServerCommandSource, ?> create() {
        return CommandManager.literal("check")
                .then(CommandManager.argument("target", EntityArgumentType.entity())
                        .executes(CheckTimedActionCommand::execute));
    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var target = context.getArgument("target", EntitySelector.class).getEntity(context.getSource());
        if(target != null) {
            if(target.getTimedAction().isEmpty()) {
                MutableText text = Text.literal(String.format("There is no action playing for %s (%s)", target.getName().getString(), target.getUuid().toString()));
                context.getSource().sendFeedback(() -> text, false);
                return 0;
            }

            MutableText text = getMutableText(target);

            context.getSource().sendFeedback(() -> text, false);

            return 0;
        }

        return 1;
    }

    private static @NotNull MutableText getMutableText(Entity target) {
        TimedAction<TimedActionHolder> animation = target.getTimedAction().get();

        return Text.literal
                (String.format(
                        "Animation Info:\n" +
                                "Animation: %s\n" +
                                "Target: §7%s (%s)§r\n" +
                                "Ticks-Left: §7%s§r\n" +
                                "Play-State: §7%s§r",
                        animation.getActionIdentifier(),
                        target.getName().getString(),
                        target.getUuid().toString(),
                        String.format("%dt, %ds, %smin", animation.getTicksLeft(), animation.getTicksLeft()/20, animation.getTicksLeft()/20/60), animation.getPlayState().name()
                ));
    }
}