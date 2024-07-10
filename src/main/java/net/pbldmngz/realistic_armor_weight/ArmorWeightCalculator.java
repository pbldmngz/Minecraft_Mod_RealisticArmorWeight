package net.pbldmngz.realistic_armor_weight;

import net.minecraft.entity.player.PlayerEntity;

public interface ArmorWeightCalculator {
    float calculateArmorWeightFactor(PlayerEntity player);
    float calculateJumpBoost(PlayerEntity player, float weightFactor);
}