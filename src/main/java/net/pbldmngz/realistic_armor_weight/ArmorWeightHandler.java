package net.pbldmngz.realistic_armor_weight;

import net.minecraft.entity.player.PlayerEntity;

public interface ArmorWeightHandler extends JumpHandler, ArmorWeightCalculator {
    // This interface now combines both JumpHandler and ArmorWeightCalculator
}