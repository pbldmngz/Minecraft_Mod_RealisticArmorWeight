package net.pbldmngz.realistic_armor_weight.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.pbldmngz.realistic_armor_weight.ArmorWeightMod;
import net.pbldmngz.realistic_armor_weight.JumpHandler;
import net.pbldmngz.realistic_armor_weight.mixin.PlayerEntityMixin;

public class ArmorWeightPackets {
    public static final Identifier SYNC_ARMOR_WEIGHT = new Identifier(ArmorWeightMod.MOD_ID, "sync_armor_weight");
    public static final Identifier JUMP_REQUEST = new Identifier(ArmorWeightMod.MOD_ID, "jump_request");
    public static final Identifier JUMP_SYNC = new Identifier(ArmorWeightMod.MOD_ID, "jump_sync");


    public static void registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_ARMOR_WEIGHT, (client, handler, buf, responseSender) -> {
            float weightFactor = buf.readFloat();
            client.execute(() -> {
                // Apply the received weight factor to the client-side player
                if (client.player != null) {
                    // Apply the weightFactor to client-side movement prediction
                    // This might involve updating the player's attributes or storing the value for use in movement calculations
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(JUMP_SYNC, (client, handler, buf, responseSender) -> {
            double velocityY = buf.readDouble();
            client.execute(() -> {
                if (client.player != null) {
                    Vec3d currentVelocity = client.player.getVelocity();
                    client.player.setVelocity(currentVelocity.x, velocityY, currentVelocity.z);
                }
            });
        });
    }

    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(JUMP_REQUEST, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (player instanceof JumpHandler) {
                    ((JumpHandler) player).handleServerJump(player);
                }
            });
        });
    }

}