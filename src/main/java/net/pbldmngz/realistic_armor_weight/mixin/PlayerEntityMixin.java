package net.pbldmngz.realistic_armor_weight.mixin;

import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;
import net.pbldmngz.realistic_armor_weight.ArmorWeightMod;

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
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    private static final UUID ARMOR_WEIGHT_SPEED_UUID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ELYTRA_SPEED_UUID = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final UUID ELYTRA_FALL_RESISTANCE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000004");
    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("3b07f6a1-ec4e-4f8b-9ed6-bda2e4e4bb6a");

    private static final float DEFAULT_JUMP_HEIGHT = 1.25f; // Default jump height in blocks
    private static final float MAX_JUMP_HEIGHT = 25.0f; // Maximum jump height in blocks
    private static final float DEFAULT_ATTACK_DAMAGE = 1.0f;
    private static final float MAX_ATTACK_DAMAGE = 20.0f;
    private static final float DEFAULT_MOVEMENT_SPEED = 0.1f;
    private static final float MAX_MOVEMENT_SPEED = 0.5638577f; // Maximum speed factor observed in logs

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        updateArmorWeightEffects((PlayerEntity)(Object)this);
    }

    @Inject(method = "jump", at = @At("TAIL"))
    private void onJump(CallbackInfo ci) {
        Logger logger = LogManager.getLogger(ArmorWeightMod.class);

        if (ArmorWeightMod.CONFIG.isEnableSpeedJump()) {
            logger.info("onJump method executed!");
            PlayerEntity player = (PlayerEntity)(Object)this;
            float weightFactor = calculateArmorWeightFactor(player);
            logger.info("Weight factor: " + weightFactor);

            float baseMovementSpeed = getBaseMovementSpeed(player);
            logger.info("Base movement speed: " + baseMovementSpeed);

            // Calculate jump boost
            float jumpBoost = calculateJumpBoost(player, weightFactor);
            logger.info("Jump boost: " + jumpBoost);

            // Apply the jump boost
            Vec3d newVelocity = player.getVelocity().add(0, jumpBoost, 0);
            player.setVelocity(newVelocity);

            logger.info("Player velocity after jump: " + player.getVelocity());
        }
    }

    @Inject(method = "attack", at = @At("HEAD"))
    private void onAttack(Entity target, CallbackInfo ci) {
        Logger logger = LogManager.getLogger(ArmorWeightMod.class);
        PlayerEntity player = (PlayerEntity)(Object)this;
        float weightFactor = calculateArmorWeightFactor(player);

        float baseMovementSpeed = getBaseMovementSpeed(player);
        logger.info("Base movement speed: " + baseMovementSpeed);

        float damageMultiplier = calculateAttackMultiplier(player, weightFactor);
        logger.info("ATTACK damage multiplier: " + damageMultiplier);

        player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).removeModifier(ATTACK_SPEED_UUID);

        EntityAttributeModifier modifier = new EntityAttributeModifier(ATTACK_SPEED_UUID, "Speed Attack Modifier", damageMultiplier - 1, EntityAttributeModifier.Operation.ADDITION);
        player.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).addTemporaryModifier(modifier);

        double finalDamage = player.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        logger.info("Final attack damage: " + finalDamage);
    }

    private float getBaseMovementSpeed(PlayerEntity player) {
        return (float) player.getAttributeValue(EntityAttributes.GENERIC_MOVEMENT_SPEED);
    }

    private float calculateJumpBoost(PlayerEntity player, float weightFactor) {
        float baseMovementSpeed = getBaseMovementSpeed(player);
        float speedFactor = (baseMovementSpeed - DEFAULT_MOVEMENT_SPEED) / (MAX_MOVEMENT_SPEED - DEFAULT_MOVEMENT_SPEED);
        speedFactor = Math.max(0, Math.min(1, speedFactor)); // Clamp between 0 and 1

        // Apply a 30% boost if the player is sprinting
        if (player.isSprinting()) {
            speedFactor = Math.min(1, speedFactor * 1.3f);
        }

        float baseJumpVelocity = 0.42f; // Minecraft's base jump velocity
        float defaultJumpVelocity = (float) Math.sqrt(2 * DEFAULT_JUMP_HEIGHT * 0.08);
        float maxJumpVelocity = (float) Math.sqrt(2 * MAX_JUMP_HEIGHT * 0.08);

        float jumpBoost = defaultJumpVelocity + (maxJumpVelocity - defaultJumpVelocity) * speedFactor * weightFactor;
        return jumpBoost - baseJumpVelocity; // Return the additional velocity needed
    }

    private float calculateAttackMultiplier(PlayerEntity player, float weightFactor) {
        float baseMovementSpeed = getBaseMovementSpeed(player);
        float speedFactor = (baseMovementSpeed - DEFAULT_MOVEMENT_SPEED) / (MAX_MOVEMENT_SPEED - DEFAULT_MOVEMENT_SPEED);
        speedFactor = Math.max(0, Math.min(1, speedFactor)); // Clamp between 0 and 1

        // Apply a 30% boost if the player is sprinting
        if (player.isSprinting()) {
            speedFactor = Math.min(1, speedFactor * 1.3f);
        }

        float damageRange = MAX_ATTACK_DAMAGE - DEFAULT_ATTACK_DAMAGE;
        return DEFAULT_ATTACK_DAMAGE + (damageRange * speedFactor * weightFactor);
    }

    private void updateArmorWeightEffects(PlayerEntity player) {
        float weightFactor = calculateArmorWeightFactor(player);

        modifyAttribute(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, ARMOR_WEIGHT_SPEED_UUID, "Armor Weight Speed Modifier", weightFactor);

        if (player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
            applyElytraBonuses(player);
        } else {
            removeElytraBonuses(player);
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

    private void applyElytraBonuses(PlayerEntity player) {
        modifyAttribute(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, ELYTRA_SPEED_UUID, "Elytra Speed Bonus", 1 + ArmorWeightMod.CONFIG.getElytraSpeedBonus());
        modifyAttribute(player, EntityAttributes.GENERIC_MAX_HEALTH, ELYTRA_FALL_RESISTANCE_UUID, "Elytra Fall Resistance", 1 + ArmorWeightMod.CONFIG.getElytraFallResistanceBonus());
    }

    private void removeElytraBonuses(PlayerEntity player) {
        removeAttribute(player, EntityAttributes.GENERIC_MOVEMENT_SPEED, ELYTRA_SPEED_UUID);
        removeAttribute(player, EntityAttributes.GENERIC_MAX_HEALTH, ELYTRA_FALL_RESISTANCE_UUID);
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