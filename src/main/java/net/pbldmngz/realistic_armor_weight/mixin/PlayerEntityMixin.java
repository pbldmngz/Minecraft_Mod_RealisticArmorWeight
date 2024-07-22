package net.pbldmngz.realistic_armor_weight.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pbldmngz.realistic_armor_weight.*;
import net.pbldmngz.realistic_armor_weight.network.ArmorWeightPackets;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import java.util.Queue;
import java.util.LinkedList;


@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements ArmorWeightHandler {

    private static final UUID ARMOR_WEIGHT_SPEED_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ELYTRA_SPEED_UUID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID ELYTRA_FALL_RESISTANCE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000004");

    private static final int SPEED_HISTORY_SIZE = 8; // Store last 5 speed readings
    private Queue<Float> speedHistory = new LinkedList<>();
    private float totalSpeed = 0f;

    private static final Logger LOGGER = LogManager.getLogger(ArmorWeightMod.class);
    private long lastJumpTime = 0;
    private static final long JUMP_COOLDOWN = 250; // milliseconds
    private Vec3d lastPosition = Vec3d.ZERO;
    private long lastMovementTime = 0;

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (!this.world.isClient) {
            updateArmorWeightEffects((PlayerEntity)(Object)this);
            updateCurrentSpeed((PlayerEntity)(Object)this);
        }
    }

    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    private void onJump(CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastJumpTime < JUMP_COOLDOWN) {
            ci.cancel();
            return;
        }

        lastJumpTime = currentTime;

        float weightFactor = calculateArmorWeightFactor(player);
        float jumpBoost = calculateJumpBoost(player, weightFactor);

        // Apply jump boost
        Vec3d velocity = player.getVelocity();
        player.setVelocity(velocity.x, jumpBoost, velocity.z);

        // Cancel vanilla jump
        ci.cancel();

        if (player.world.isClient) {
            sendJumpRequestToServer();
        }
    }

    @Environment(EnvType.CLIENT)
    private void sendJumpRequestToServer() {
        ClientPlayNetworking.send(ArmorWeightPackets.JUMP_REQUEST, PacketByteBufs.create());
    }

    @Override
    public void handleServerJump(PlayerEntity player) {
        if (!player.world.isClient) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastJumpTime < JUMP_COOLDOWN) {
                return;
            }
            lastJumpTime = currentTime;

            float weightFactor = calculateArmorWeightFactor(player);
            float jumpBoost = calculateJumpBoost(player, weightFactor);
            Vec3d newVelocity = player.getVelocity().add(0, jumpBoost, 0);

            // Only send correction if there's a significant difference
            if (Math.abs(newVelocity.y - player.getVelocity().y) > 0.01) {
                player.setVelocity(newVelocity);

                PacketByteBuf buf = PacketByteBufs.create();
                buf.writeDouble(newVelocity.y);
                ServerPlayNetworking.send((ServerPlayerEntity) player, ArmorWeightPackets.JUMP_SYNC, buf);
            }
        }
    }

    private boolean shouldNegateFallDamage(PlayerEntity player) {
        long currentTime = System.currentTimeMillis();
        float jumpDuration = calculateJumpDuration(player);
        return currentTime - lastJumpTime < jumpDuration;
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source == DamageSource.FALL && shouldNegateFallDamage((PlayerEntity)(Object)this)) {
            cir.setReturnValue(false);
        }
    }

    private float calculateJumpDuration(PlayerEntity player) {
        float jumpVelocity = (float) player.getVelocity().y;
        // Estimate the time it takes to reach the peak of the jump
        float timeToApex = jumpVelocity / 0.08f; // 0.08 is roughly the gravity constant in Minecraft
        // Double it to account for both ascending and descending
        return timeToApex * 2000; // Convert to milliseconds
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;

        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;

            // Calculate damage multiplier here, to ensure it's updated for each attack
            float weightFactor = calculateArmorWeightFactor(player);
            float damageMultiplier = calculateAttackMultiplier(player, weightFactor);

            float baseDamage = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            float modifiedDamage = baseDamage * damageMultiplier;

            // Apply the damage
            boolean damaged = livingTarget.damage(DamageSource.player(player), modifiedDamage);

            // Cancel the original attack
            ci.cancel();
        }
    }


    private void updateArmorWeightEffects(PlayerEntity player) {
        float weightFactor = calculateArmorWeightFactor(player);

        modifyAttribute(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, ARMOR_WEIGHT_SPEED_UUID, "Armor Weight Speed Modifier", weightFactor);

        if (player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            modifyAttribute(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, ELYTRA_SPEED_UUID, "Elytra Speed Bonus", 1 + ArmorWeightMod.CONFIG.getElytraSpeedBonus());
            modifyAttribute(player, EntityAttributes.GENERIC_MAX_HEALTH, ELYTRA_FALL_RESISTANCE_UUID, "Elytra Fall Resistance", 1 + ArmorWeightMod.CONFIG.getElytraFallResistanceBonus());
        } else {
            removeAttribute(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, ELYTRA_SPEED_UUID);
            removeAttribute(player, EntityAttributes.GENERIC_MAX_HEALTH, ELYTRA_FALL_RESISTANCE_UUID);
        }
    }

    @Override
    public float calculateArmorWeightFactor(PlayerEntity player) {
        float totalWeight = 0f;
        for (ItemStack armorPiece : player.getArmorItems()) {
            if (armorPiece.getItem() instanceof ArmorItem) {
                ArmorItem armor = (ArmorItem) armorPiece.getItem();
                totalWeight += ArmorWeightMod.CONFIG.getArmorWeight(armor.getMaterial().getName());
            }
        }
        return 1f - totalWeight;
    }

    @Override
    public float calculateJumpBoost(PlayerEntity player, float weightFactor) {
        float baseMovementSpeed = (float) player.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        float minJumpBoost = 0.42f;  // Default Minecraft jump boost
        float baseJumpBoost = 0.42f;
        float baseSpeed = 0.1f;

        float speedMultiplier = baseMovementSpeed / baseSpeed + baseSpeed;

        // Ensure no reduction at low speeds, more dramatic increase for higher speeds
        float jumpBoostIncrease = (float) (Math.max(0, Math.pow(speedMultiplier - 1, 2)) * 0.1f) - baseJumpBoost * 0.5f;

        float adjustedJumpBoost = baseJumpBoost + jumpBoostIncrease;
        float weightReducedJumpBoost = adjustedJumpBoost * (0.8f + weightFactor * 0.2f) * 4f;

        if (player.isSprinting()) {
            weightReducedJumpBoost *= (float) (Math.pow(0.8f, weightReducedJumpBoost) + 0.15f);
        }

        weightReducedJumpBoost = (float) Math.pow(weightReducedJumpBoost, 0.9f) - 0.75f * baseJumpBoost;

        return Math.max(weightReducedJumpBoost, minJumpBoost);
    }

    private float calculateAttackMultiplier(PlayerEntity player, float weightFactor) {
        float averageSpeed = calculateAverageSpeed();
        float baseSpeed = 0.1f;

        // Calculate horizontal speed
        float horizontalSpeed = averageSpeed * 100000;

        // Calculate total speed with reduced impact of vertical speed
        float totalSpeed = horizontalSpeed - 0.0784f;

        // Calculate speed factor
        float speedFactor = totalSpeed / baseSpeed + baseSpeed;

        // Calculate speed-based damage multiplier with more controlled scaling
        float speedDamageMultiplier = 1f;
        if (speedFactor > 1) {
            speedDamageMultiplier = 1f + (float) (Math.pow(speedFactor, 0.6) / 25000) * ArmorWeightMod.CONFIG.getSpeedAttackMultiplier();
        }

        // Weight-based damage multiplier (more weight = more damage)
        float weightDamageMultiplier = 1f + (1f - weightFactor) * 0.2f;

        // Combine speed and weight multipliers
        float combinedMultiplier = speedDamageMultiplier * weightDamageMultiplier;

        return Math.max(combinedMultiplier, 1f);
    }

    private void updateCurrentSpeed(PlayerEntity player) {
        long currentTime = System.currentTimeMillis();
        Vec3d currentPosition = player.getPos();

        if (lastMovementTime != 0) {
            double timeDelta = (currentTime - lastMovementTime) / 1000.0; // Convert to seconds
            Vec3d movement = currentPosition.subtract(lastPosition);
            float speed = (float) (movement.length() / timeDelta);

            // Update speed history
            if (speedHistory.size() >= SPEED_HISTORY_SIZE) {
                totalSpeed -= speedHistory.poll();
            }
            speedHistory.offer(speed);
            totalSpeed += speed;

            float averageSpeed = calculateAverageSpeed();

            TrackedData<Float> customSpeedKey = ((CustomSpeedAccessor) player).armorweight$getCustomSpeedKey();
            player.getDataTracker().set(customSpeedKey, averageSpeed);
        }

        lastPosition = currentPosition;
        lastMovementTime = currentTime;
    }

    private float calculateAverageSpeed() {
        if (speedHistory.isEmpty()) {
            return 0f;
        }
        return totalSpeed / speedHistory.size();
    }

    private float calculateCurrentSpeed(PlayerEntity player) {
        TrackedData<Float> customSpeedKey = ((CustomSpeedAccessor) player).armorweight$getCustomSpeedKey();
        return player.getDataTracker().get(customSpeedKey);
    }

    private void modifyAttribute(PlayerEntity player, EntityAttribute attribute, UUID id, String name, float modifier) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        if (instance != null) {
            EntityAttributeModifier existingModifier = instance.getModifier(id);
            if (existingModifier != null) {
                instance.removeModifier(existingModifier);
            }
            EntityAttributeModifier newModifier = new EntityAttributeModifier(id, name, modifier - 1, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
            instance.addPersistentModifier(newModifier);
        }
    }

    private void removeAttribute(PlayerEntity player, EntityAttribute attribute, UUID id) {
        EntityAttributeInstance instance = player.getAttributeInstance(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }
}