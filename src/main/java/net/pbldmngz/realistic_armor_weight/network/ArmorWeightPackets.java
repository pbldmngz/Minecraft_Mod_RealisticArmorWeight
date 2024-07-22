package net.pbldmngz.realistic_armor_weight.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.pbldmngz.realistic_armor_weight.ArmorWeightHandler;
import net.pbldmngz.realistic_armor_weight.ArmorWeightMod;
import net.pbldmngz.realistic_armor_weight.CustomSpeedAccessor;

import java.util.UUID;

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
                    EntityAttributeInstance moveSpeedAttr = client.player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                    if (moveSpeedAttr != null) {
                        // Remove existing modifiers
                        moveSpeedAttr.removeModifier(UUID.fromString("00000000-0000-0000-0000-000000000001"));

                        // Add new modifier based on received weightFactor
                        EntityAttributeModifier newModifier = new EntityAttributeModifier(
                                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                "Armor Weight Speed Modifier",
                                weightFactor - 1,
                                EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                        );
                        moveSpeedAttr.addTemporaryModifier(newModifier);
                    }

                    // Update custom speed for client-side predictions
                    if (client.player instanceof CustomSpeedAccessor) {
                        TrackedData<Float> customSpeedKey = ((CustomSpeedAccessor) client.player).armorweight$getCustomSpeedKey();
                        client.player.getDataTracker().set(customSpeedKey, weightFactor);
                    }
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

        ServerPlayNetworking.registerGlobalReceiver(SYNC_ARMOR_WEIGHT, (server, player, handler, buf, responseSender) -> {
            float weightFactor = buf.readFloat();
            server.execute(() -> {
                if (player instanceof ArmorWeightHandler) {
                    ArmorWeightHandler armorWeightHandler = (ArmorWeightHandler) player;
                    EntityAttributeInstance moveSpeedAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
                    if (moveSpeedAttr != null) {
                        // Remove existing modifiers
                        moveSpeedAttr.removeModifier(UUID.fromString("00000000-0000-0000-0000-000000000001"));

                        // Add new modifier based on received weightFactor
                        EntityAttributeModifier newModifier = new EntityAttributeModifier(
                                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                                "Armor Weight Speed Modifier",
                                weightFactor - 1,
                                EntityAttributeModifier.Operation.MULTIPLY_TOTAL
                        );
                        moveSpeedAttr.addPersistentModifier(newModifier);
                    }

                    // Update custom speed for server-side calculations
                    if (player instanceof CustomSpeedAccessor) {
                        TrackedData<Float> customSpeedKey = ((CustomSpeedAccessor) player).armorweight$getCustomSpeedKey();
                        player.getDataTracker().set(customSpeedKey, weightFactor);
                    }
                }
            });
        });
    }
}