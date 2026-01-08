package nico.timed_actions.internal.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import nico.timed_actions.api.v1.TimedAction;
import nico.timed_actions.api.v1.TimedActionHolder;
import nico.timed_actions.internal.v1.InternalTimedActionRegistry;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ListTimedActionsCommand {

    public static ArgumentBuilder<ServerCommandSource, ?> create() {
        return CommandManager.literal("list")
                .then(CommandManager.argument("namespace", StringArgumentType.string()).executes(ListTimedActionsCommand::execute))
                .executes(ListTimedActionsCommand::execute);
    }

    public static <T extends TimedAction<U>, U extends TimedActionHolder> int execute(CommandContext<ServerCommandSource> context) {
        Map<Identifier, InternalTimedActionRegistry.TimedActionEntry<?, ?>> animationMap = InternalTimedActionRegistry.getTimedActionEntries();

        try{
            String namespace = context.getArgument("namespace", String.class);
            if(namespace != null) animationMap = InternalTimedActionRegistry.getAllFromNamespace(namespace);
        } catch (Exception ignored){}

        ServerCommandSource source = context.getSource();
        MutableText text = Text.literal(String.format("Animations (%s):" + (animationMap.isEmpty()?"":"\n"), String.valueOf(animationMap.size())));

        AtomicInteger i = new AtomicInteger();
        final int size = animationMap.size();
        animationMap.forEach((identifier, animation) -> {
            text.append(Text.literal(identifier.toString() + (i.getAndIncrement()==size-1?"":"\n")));

        });

        source.sendFeedback(() -> text, false);

        return 0;
    }
}