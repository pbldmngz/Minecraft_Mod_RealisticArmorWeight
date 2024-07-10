package net.pbldmngz.realistic_armor_weight.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.pbldmngz.realistic_armor_weight.CustomSpeedAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityDataMixin implements CustomSpeedAccessor {
    @Unique
    private static final TrackedData<Float> CUSTOM_SPEED = DataTracker.registerData(PlayerEntity.class, TrackedDataHandlerRegistry.FLOAT);

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initCustomData(CallbackInfo ci) {
        ((PlayerEntity)(Object)this).getDataTracker().startTracking(CUSTOM_SPEED, 0.0f);
    }

    @Override
    public TrackedData<Float> armorweight$getCustomSpeedKey() {
        return CUSTOM_SPEED;
    }
}