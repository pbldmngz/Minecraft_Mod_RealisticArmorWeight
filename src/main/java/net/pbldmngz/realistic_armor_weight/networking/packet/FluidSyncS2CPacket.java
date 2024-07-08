package net.pbldmngz.realistic_armor_weight.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.pbldmngz.realistic_armor_weight.block.entity.GemInfusingBlockEntity;
import net.pbldmngz.realistic_armor_weight.screen.GemInfusingScreenHandler;
import net.pbldmngz.realistic_armor_weight.util.FluidStack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.BlockPos;

public class FluidSyncS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        FluidVariant variant = FluidVariant.fromPacket(buf);
        long fluidLevel = buf.readLong();
        BlockPos position = buf.readBlockPos();

        if(client.world.getBlockEntity(position) instanceof GemInfusingBlockEntity blockEntity) {
            blockEntity.setFluidLevel(variant, fluidLevel);

            if(client.player.currentScreenHandler instanceof GemInfusingScreenHandler screenHandler &&
                    screenHandler.blockEntity.getPos().equals(position)) {
                blockEntity.setFluidLevel(variant, fluidLevel);
                screenHandler.setFluid(new FluidStack(variant, fluidLevel));
            }
        }
    }
}
