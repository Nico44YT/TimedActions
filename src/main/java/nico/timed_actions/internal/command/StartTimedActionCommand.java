package nico.timed_actions.internal.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nico.timed_actions.api.v1.BlockEntityTimedAction;
import nico.timed_actions.api.v1.EntityTimedAction;
import nico.timed_actions.api.v1.TimedAction;
import nico.timed_actions.api.v1.TimedActionHolder;
import nico.timed_actions.internal.v1.InternalTimedActionRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class StartTimedActionCommand {
    public static ArgumentBuilder<ServerCommandSource, ?> create() {
        return CommandManager.literal("start")
                .then(CommandManager.literal("blockentity")
                        .then(CommandManager.argument("target_blockentity", BlockPosArgumentType.blockPos())
                                .then(CommandManager.argument("animation_identifier", IdentifierArgumentType.identifier()).suggests(StartTimedActionCommand::suggestsBlockEntity)
                                        .executes(StartTimedActionCommand::executeBlockEntity))))
                .then(CommandManager.literal("entity")
                        .then(CommandManager.argument("target_entity", EntityArgumentType.entity())
                                .then(CommandManager.argument("animation_identifier", IdentifierArgumentType.identifier()).suggests(StartTimedActionCommand::suggestsEntity)
                                        .executes(StartTimedActionCommand::executeEntity))));
    }

    public static int executeEntity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var target = context.getArgument("target_entity", EntitySelector.class).getEntity(context.getSource());
        if(target != null) {
            Optional<Supplier<TimedAction<TimedActionHolder>>> optional = InternalTimedActionRegistry.getActionSupplier(context.getArgument("animation_identifier", Identifier.class));

            if (optional.isPresent()) {
                TimedAction<TimedActionHolder> animation = optional.get().get();

                if (!animation.checkPredicate(target)) {
                    context.getSource().sendFeedback(() -> Text.translatable("timed_actions.action.fail", animation.getActionIdentifier(), animation.getClass()), false);
                    return 1;
                }

                target.startTimedAction(animation.getActionIdentifier());
                context.getSource().sendFeedback(() -> Text.literal(String.format("Started timed action (%s)", animation.getActionIdentifier())), true);
                return 0;
            }
        }

        return 1;
    }

    public static int executeBlockEntity(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        World world = context.getSource().getWorld();
        BlockPos pos = context.getArgument("target_blockentity", DefaultPosArgument.class).toAbsoluteBlockPos(context.getSource());
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity == null) {
            return 1;
        }

        Optional<Supplier<TimedAction<TimedActionHolder>>> optional = InternalTimedActionRegistry.getActionSupplier(context.getArgument("animation_identifier", Identifier.class));

        if (optional.isPresent()) {
            TimedAction<TimedActionHolder> animation = optional.get().get();

            if (!animation.checkPredicate(blockEntity)) {
                context.getSource().sendFeedback(() -> Text.translatable("timed_actions.action.fail", animation.getActionIdentifier(), animation.getClass()), false);
                return 1;
            }

            blockEntity.startTimedAction(animation.getActionIdentifier());
            context.getSource().sendFeedback(() -> Text.literal(String.format("Started timed action (%s)", animation.getActionIdentifier())), true);
            return 0;
        }

        return 1;
    }

    private static CompletableFuture<Suggestions> suggestsEntity(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestionsBuilder) {
        return suggests(context, suggestionsBuilder, (factory) -> factory instanceof EntityTimedAction.EntityFactory<?, ?>);
    }

    private static CompletableFuture<Suggestions> suggestsBlockEntity(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestionsBuilder) {
        return suggests(context, suggestionsBuilder, (factory) -> factory instanceof BlockEntityTimedAction.BlockEntityFactory<?, ?>);
    }

    private static CompletableFuture<Suggestions> suggests(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestionsBuilder, Predicate<TimedAction.Factory<?, ?>> predicate) {
        CommandSource commandSource = context.getSource();
        List<Suggestion> suggestions = new ArrayList<>();

        StringReader stringReader = new StringReader(suggestionsBuilder.getInput());
        stringReader.setCursor(suggestionsBuilder.getStart());

        InternalTimedActionRegistry.getTimedActionEntries().entrySet().stream().filter(entry -> predicate.test(entry.getValue().factory())).forEach(entry -> {
            suggestions.add(new Suggestion(StringRange.between(stringReader.getCursor(), stringReader.getCursor() + entry.getKey().toString().length()), entry.getKey().toString()));
        });

        Collections.sort(suggestions);

        return CompletableFuture.supplyAsync(() -> new Suggestions(StringRange.at(stringReader.getCursor()), suggestions));
    }
}