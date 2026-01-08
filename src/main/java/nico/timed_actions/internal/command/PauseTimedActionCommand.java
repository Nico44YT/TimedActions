package nico.timed_actions.internal.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class PauseTimedActionCommand {
    public static ArgumentBuilder<ServerCommandSource, ?> create() {
        return CommandManager.literal("pause")
                .then(CommandManager.argument("target", EntityArgumentType.entity()).executes(PauseTimedActionCommand::execute));

    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        if(context.getArgument("target", EntitySelector.class).getEntity(context.getSource()) instanceof LivingEntity target) {
            if(target.getTimedAction().isPresent()) {
                Identifier id = target.getTimedAction().get().getActionIdentifier();
                target.pauseTimedAction();
                context.getSource().sendFeedback(() -> Text.literal(String.format("Paused timed action (%s)", id)), true);
                return 0;
            }
        }

        return 1;
    }

}