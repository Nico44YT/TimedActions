package nico.timed_actions.internal;

import nazario.liby.api.util.LibyIdentifier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import nico.timed_actions.internal.command.MainCommand;
import nico.timed_actions.internal.test.TestBlockEntityAction;
import nico.timed_actions.internal.test.TestEntityAction;
import nico.timed_actions.internal.v1.InternalTimedActionRegistry;

public class TimedActions implements ModInitializer {

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register(MainCommand::new);

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) {
            InternalTimedActionRegistry.registerEntityAction(LibyIdentifier.of("timed_actions", "entity/test"), TestEntityAction::new, holder -> holder instanceof PlayerEntity);
            InternalTimedActionRegistry.registerBlockEntityAction(LibyIdentifier.of("timed_actions", "blockentity/test"), TestBlockEntityAction::new, holder -> holder instanceof BlockEntity);
        }
    }
}
