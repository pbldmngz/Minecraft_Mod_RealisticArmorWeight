package net.pbldmngz.realistic_armor_weight.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.pbldmngz.realistic_armor_weight.ArmorWeightCalculator;
import net.pbldmngz.realistic_armor_weight.ArmorWeightHandler;
import net.pbldmngz.realistic_armor_weight.ArmorWeightMod;
import net.pbldmngz.realistic_armor_weight.mixin.PlayerEntityMixin;

public class ArmorWeightPackets {
    public static final Identifier SYNC_ARMOR_WEIGHT = new Identifier(ArmorWeightMod.MOD_ID, "sync_armor_weight");
    public static final Identifier JUMP_REQUEST = new Identifier(ArmorWeightMod.MOD_ID, "jump_request");
    public static final Identifier JUMP_SYNC = new Identifier(ArmorWeightMod.MOD_ID, "jump_sync");

    public static void registerClientPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SYNC_ARMOR_WEIGHT, (client, handler, buf, responseSender) -> {
            float weightFactor = buf.readFloat();
            client.execute(() -> {
                if (client.player != null) {
                    // Apply the weightFactor to client-side movement prediction
                    // This might involve updating the player's attributes or storing the value for use in movement calculations
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(JUMP_SYNC, (client, handler, buf, responseSender) -> {
            double correctedJumpVelocity = buf.readDouble();
            client.execute(() -> {
                if (client.player != null) {
                    Vec3d velocity = client.player.getVelocity();
                    client.player.setVelocity(velocity.x, correctedJumpVelocity, velocity.z);
                }
            });
        });
    }

    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(JUMP_REQUEST, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (player instanceof ArmorWeightHandler) {
                    ArmorWeightHandler armorWeightHandler = (ArmorWeightHandler) player;
                    float weightFactor = armorWeightHandler.calculateArmorWeightFactor(player);
                    float jumpBoost = armorWeightHandler.calculateJumpBoost(player, weightFactor);
                    Vec3d currentVelocity = player.getVelocity();

                    // Check if the player has already jumped (client-side prediction)
                    if (currentVelocity.y <= 0.01) {
                        // Player hasn't jumped yet on the server, apply the jump
                        player.setVelocity(currentVelocity.x, jumpBoost, currentVelocity.z);
                    } else {
                        // Player has already jumped, check for significant difference
                        if (Math.abs(currentVelocity.y - jumpBoost) > 0.01) {
                            // Only correct if there's a significant difference
                            player.setVelocity(currentVelocity.x, jumpBoost, currentVelocity.z);
                            // Send correction to client
                            PacketByteBuf correctionBuf = PacketByteBufs.create();
                            correctionBuf.writeDouble(jumpBoost);
                            ServerPlayNetworking.send((ServerPlayerEntity) player, JUMP_SYNC, correctionBuf);
                        }
                    }
                }
            });
        });
    }
}