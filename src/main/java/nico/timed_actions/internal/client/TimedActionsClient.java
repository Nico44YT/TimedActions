package nico.timed_actions.internal.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import nico.timed_actions.api.v1.TimedActionHolder;
import nico.timed_actions.internal.v1.networking.SyncActionS2C;

public class TimedActionsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SyncActionS2C.PACKET_TYPE, (SyncActionS2C packet, ClientPlayerEntity clientPlayer, PacketSender packetSender) -> {
            TimedActionHolder holder = SyncActionS2C.getHolder(clientPlayer.getWorld(), packet.getResolver());
            holder.updateTimedAction(packet);
        });
    }
}
