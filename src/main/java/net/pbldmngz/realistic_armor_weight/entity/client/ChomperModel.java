package net.pbldmngz.realistic_armor_weight.entity.client;

import net.pbldmngz.realistic_armor_weight.RealisticArmorWeight;
import net.pbldmngz.realistic_armor_weight.entity.custom.ChomperEntity;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public class ChomperModel extends GeoModel<ChomperEntity> {
    @Override
    public Identifier getModelResource(ChomperEntity object) {
        return new Identifier(RealisticArmorWeight.MOD_ID, "geo/chomper.geo.json");
    }

    @Override
    public Identifier getTextureResource(ChomperEntity object) {
        return new Identifier(RealisticArmorWeight.MOD_ID, "textures/entity/chomper_texture.png");
    }

    @Override
    public Identifier getAnimationResource(ChomperEntity animatable) {
        return new Identifier(RealisticArmorWeight.MOD_ID, "animations/chomper.animation.json");
    }
}
