package net.pbldmngz.realistic_armor_weight.mixin;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.pbldmngz.realistic_armor_weight.ArmorWeightMod;
import net.pbldmngz.realistic_armor_weight.CustomSpeedAccessor;
import net.pbldmngz.realistic_armor_weight.network.ArmorWeightPackets;
import net.pbldmngz.realistic_armor_weight.JumpHandler;

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

import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements JumpHandler {

    private static final UUID ARMOR_WEIGHT_SPEED_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ELYTRA_SPEED_UUID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID ELYTRA_FALL_RESISTANCE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("3b07f6a1-ec4e-4f8b-9ed6-bda2e4e4bb6a");

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
        if (this.world.isClient) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastJumpTime < JUMP_COOLDOWN) {
                ci.cancel();
                return;
            }
            lastJumpTime = currentTime;

            PacketByteBuf buf = PacketByteBufs.create();
            ClientPlayNetworking.send(ArmorWeightPackets.JUMP_REQUEST, buf);

            ci.cancel();
        }
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
            player.setVelocity(newVelocity);

            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeDouble(newVelocity.y);
            ServerPlayNetworking.send((ServerPlayerEntity) player, ArmorWeightPackets.JUMP_SYNC, buf);
        }
    }

    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Entity target, CallbackInfo ci) {
        PlayerEntity player = (PlayerEntity)(Object)this;
        float weightFactor = calculateArmorWeightFactor(player);
        float currentSpeed = calculateCurrentSpeed(player);

        float damageMultiplier = calculateAttackMultiplier(currentSpeed, weightFactor);
        LOGGER.info("Attack damage multiplier: " + damageMultiplier);

        if (target instanceof LivingEntity) {
            LivingEntity livingTarget = (LivingEntity) target;
            float baseDamage = (float) player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
            float modifiedDamage = baseDamage * damageMultiplier;

            // Use DamageSource.player() instead of getDamageSources().playerAttack()
            livingTarget.damage(DamageSource.player(player), modifiedDamage);
            LOGGER.info("Base damage: " + baseDamage + ", Modified damage: " + modifiedDamage);

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

    private float calculateArmorWeightFactor(PlayerEntity player) {
        float totalWeight = 0f;
        for (ItemStack armorPiece : player.getArmorItems()) {
            if (armorPiece.getItem() instanceof ArmorItem) {
                ArmorItem armor = (ArmorItem) armorPiece.getItem();
                totalWeight += ArmorWeightMod.CONFIG.getArmorWeight(armor.getMaterial().getName());
            }
        }
        return 1f - totalWeight;
    }

    private float calculateJumpBoost(PlayerEntity player, float weightFactor) {
        float baseMovementSpeed = (float) player.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        float minJumpBoost = 1.2f; // Minimum jump boost
        float maxJumpBoost = 20f; // Maximum jump boost
        float scaleFactor = 10f; // Adjust this to control how quickly the boost reaches its maximum

        // Calculate the raw jump boost
        float rawJumpBoost = baseMovementSpeed * weightFactor * scaleFactor;

        // Apply a logarithmic scale to make it harder to increase at higher values
        float scaledJumpBoost = (float) (Math.log1p(rawJumpBoost) / Math.log1p(scaleFactor));

        // Clamp the value between minJumpBoost and maxJumpBoost
        float finalJumpBoost = MathHelper.clamp(
                minJumpBoost + (maxJumpBoost - minJumpBoost) * scaledJumpBoost,
                minJumpBoost,
                maxJumpBoost
        );

        // Increase jump height by 30% if the player is sprinting
        if (player.isSprinting()) {
            finalJumpBoost *= 1.3f;
        }

        return finalJumpBoost;
    }

    private void updateCurrentSpeed(PlayerEntity player) {
        long currentTime = System.currentTimeMillis();
        Vec3d currentPosition = player.getPos();

        if (lastMovementTime != 0) {
            double timeDelta = (currentTime - lastMovementTime) / 1000.0; // Convert to seconds
            Vec3d movement = currentPosition.subtract(lastPosition);
            double speed = movement.length() / timeDelta;

            TrackedData<Float> customSpeedKey = ((CustomSpeedAccessor) player).armorweight$getCustomSpeedKey();
            player.getDataTracker().set(customSpeedKey, (float) speed);
        }

        lastPosition = currentPosition;
        lastMovementTime = currentTime;
    }

    private float calculateCurrentSpeed(PlayerEntity player) {
        TrackedData<Float> customSpeedKey = ((CustomSpeedAccessor) player).armorweight$getCustomSpeedKey();
        return player.getDataTracker().get(customSpeedKey);
    }

    private float calculateAttackMultiplier(float currentSpeed, float weightFactor) {
        float speedFactor = currentSpeed / 5.612f; // Normalize speed (adjust divisor as needed)
        //speedFactor = Math.min(1, speedFactor); // Cap at 1

        float damageMultiplier = 1 + (speedFactor * ( 1 / weightFactor ) * ArmorWeightMod.CONFIG.getSpeedAttackMultiplier()) * 100;
        return damageMultiplier;
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