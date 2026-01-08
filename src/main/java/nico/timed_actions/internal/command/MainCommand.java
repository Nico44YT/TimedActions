package nico.timed_actions.internal.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;

public class MainCommand {
    protected static List<ArgumentBuilder> subCommands = new ArrayList<>();

    public MainCommand(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        LiteralArgumentBuilder<ServerCommandSource> builder = CommandManager.literal("timed_actions").requires((source) -> source.hasPermissionLevel(3));

        subCommands.add(CheckTimedActionCommand.create());
        subCommands.add(ListTimedActionsCommand.create());
        subCommands.add(StartTimedActionCommand.create());
        subCommands.add(StopTimedActionCommand.create());
        subCommands.add(PauseTimedActionCommand.create());
        subCommands.add(ResumeTimedActionCommand.create());
        subCommands.add(RestartTimedActionCommand.create());
        subCommands.add(RemoveTimedActionCommand.create());

        subCommands.forEach(builder::then);

        dispatcher.register(builder);
    }
}
