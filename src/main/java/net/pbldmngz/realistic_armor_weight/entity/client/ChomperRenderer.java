package net.pbldmngz.realistic_armor_weight.entity.client;

import net.pbldmngz.realistic_armor_weight.RealisticArmorWeight;
import net.pbldmngz.realistic_armor_weight.entity.custom.ChomperEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class ChomperRenderer extends GeoEntityRenderer<ChomperEntity> {
    public ChomperRenderer(EntityRendererFactory.Context ctx) {
        super(ctx, new ChomperModel());
        this.shadowRadius = 0.4f;
    }

    @Override
    public Identifier getTextureLocation(ChomperEntity instance) {
        return new Identifier(RealisticArmorWeight.MOD_ID, "textures/entity/chomper_texture.png");
    }


    @Override
    public RenderLayer getRenderType(ChomperEntity animatable, Identifier texture, @Nullable VertexConsumerProvider bufferSource, float partialTick) {
        return super.getRenderType(animatable, texture, bufferSource, partialTick);
    }
}
