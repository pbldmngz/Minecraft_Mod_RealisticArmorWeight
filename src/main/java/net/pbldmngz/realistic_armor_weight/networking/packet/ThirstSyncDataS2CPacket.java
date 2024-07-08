package net.pbldmngz.realistic_armor_weight.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.pbldmngz.realistic_armor_weight.util.IEntityDataSaver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

public class ThirstSyncDataS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        ((IEntityDataSaver) client.player).getPersistentData().putInt("thirst", buf.readInt());
    }
}
