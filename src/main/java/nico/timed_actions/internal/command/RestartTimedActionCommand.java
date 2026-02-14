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
import nico.timed_actions.internal.v1.InternalTimedActionRegistry;

public class RestartTimedActionCommand {
    public static ArgumentBuilder<ServerCommandSource, ?> create() {
        return CommandManager.literal("restart")
                .then(CommandManager.argument("target", EntityArgumentType.entity()).executes(RestartTimedActionCommand::execute));

    }

    public static int execute(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var target = context.getArgument("target", EntitySelector.class).getEntity(context.getSource());
        if(target != null) {
            if(target.getTimedAction().isPresent()) {
                Identifier id = target.getTimedAction().get().getActionIdentifier();
                target.restartTimedAction(InternalTimedActionRegistry.getActionSupplier(id).get().get());
                context.getSource().sendFeedback(() -> Text.literal(String.format("Restarted timed action (%s)", id)), true);
                return 0;
            }
        }

        return 1;
    }

}