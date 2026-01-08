package nico.timed_actions.internal.v1.networking;

import nazario.liby.api.nbt.LibyNbtCompound;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import nico.timed_actions.api.v1.TimedActionHolder;

public record SyncActionS2C(NbtCompound animationData,
                            NbtCompound animatableResolver) implements FabricPacket {

    public static final Identifier ID = Identifier.of("timed_actions", "sync_action_s2c");
    public static final PacketType<SyncActionS2C> PACKET_TYPE = PacketType.create(ID, SyncActionS2C::fromPacketByteBuf);

    public static SyncActionS2C fromPacketByteBuf(PacketByteBuf packetByteBuf) {
        return new SyncActionS2C(packetByteBuf.readNbt(), packetByteBuf.readNbt());
    }

    public static SyncActionS2C create(TimedActionHolder holder) {
        LibyNbtCompound animationData = new LibyNbtCompound();
        LibyNbtCompound animatableResolver = new LibyNbtCompound();

        holder.getTimedAction().get().writeNbt(animationData);

        if (holder instanceof Entity entity) {
            animatableResolver.putEnum("type", TimedActionHolder.AnimatableType.ENTITY);
            animatableResolver.putInt("entity_id", entity.getId());
        }

        if (holder instanceof BlockEntity blockEntity) {
            animatableResolver.putEnum("type", TimedActionHolder.AnimatableType.BLOCK_ENTITY);
            animatableResolver.putString("world", blockEntity.getWorld().getRegistryKey().toString());
            animatableResolver.putString("block_entity_type", blockEntity.getType().toString());
            animatableResolver.putBlockPos("block_pos", blockEntity.getPos());
        }

        return new SyncActionS2C(animationData, animatableResolver);
    }

    @Override
    public void write(PacketByteBuf packetByteBuf) {
        packetByteBuf.writeNbt(animationData);
        packetByteBuf.writeNbt(animatableResolver);
    }

    @Override
    public PacketType<?> getType() {
        return PACKET_TYPE;
    }

    public static TimedActionHolder getHolder(World world, NbtCompound animatableResolver) {
        LibyNbtCompound libyNbt = new LibyNbtCompound(animatableResolver);

        TimedActionHolder.AnimatableType type = libyNbt.getEnum("type", TimedActionHolder.AnimatableType.class);

        return switch (type) {
            case ENTITY -> {
                int entityId = libyNbt.getInt("entity_id");

                yield world.getEntityById(entityId);
            }
            case BLOCK_ENTITY -> {
                BlockPos blockPos = libyNbt.getBlockPos("block_pos");
                String worldKey = libyNbt.getString("world");
                String blockEntityType = libyNbt.getString("block_entity_type");

                if (world.getRegistryKey().toString().equals(worldKey)) {
                    BlockEntity blockEntity = world.getBlockEntity(blockPos);

                    if (!blockEntity.getType().toString().equals(blockEntityType)) yield null;

                    yield blockEntity;
                }

                yield null;
            }
        };
    }

    public NbtElement getData() {
        return animationData;
    }

    public NbtCompound getResolver() {
        return animatableResolver;
    }
}
