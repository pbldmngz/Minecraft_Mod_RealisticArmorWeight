package net.pbldmngz.realistic_armor_weight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.pbldmngz.realistic_armor_weight.block.ModBlocks;
import net.pbldmngz.realistic_armor_weight.block.entity.ModBlockEntities;
import net.pbldmngz.realistic_armor_weight.block.entity.client.GemInfusingStationBlockEntityRenderer;
import net.pbldmngz.realistic_armor_weight.client.ThirstHudOverlay;
import net.pbldmngz.realistic_armor_weight.entity.ModEntities;
import net.pbldmngz.realistic_armor_weight.entity.client.ChomperRenderer;
import net.pbldmngz.realistic_armor_weight.event.KeyInputHandler;
import net.pbldmngz.realistic_armor_weight.fluid.ModFluids;
import net.pbldmngz.realistic_armor_weight.networking.ModMessages;
import net.pbldmngz.realistic_armor_weight.screen.GemInfusingScreen;
import net.pbldmngz.realistic_armor_weight.screen.ModScreenHandlers;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Identifier;

public class TutorialModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.EGGPLANT_CROP, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DOGWOOD_LEAVES, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DOGWOOD_SAPLING, RenderLayer.getCutout());

        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.BUTTERCUPS, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.POTTED_BUTTERCUPS, RenderLayer.getCutout());

        KeyInputHandler.register();
        ModMessages.registerS2CPackets();

        HudRenderCallback.EVENT.register(new ThirstHudOverlay());

        FluidRenderHandlerRegistry.INSTANCE.register(ModFluids.STILL_SOAP_WATER, ModFluids.FLOWING_SOAP_WATER,
                new SimpleFluidRenderHandler(
                        new Identifier("minecraft:block/water_still"),
                        new Identifier("minecraft:block/water_flow"),
                        0xA1E038D0
                ));

        BlockRenderLayerMap.INSTANCE.putFluids(RenderLayer.getTranslucent(),
                ModFluids.STILL_SOAP_WATER, ModFluids.FLOWING_SOAP_WATER);

        HandledScreens.register(ModScreenHandlers.GEM_INFUSING_SCREEN_HANDLER, GemInfusingScreen::new);

        BlockEntityRendererRegistry.register(ModBlockEntities.GEM_INFUSING_STATION, GemInfusingStationBlockEntityRenderer::new);

        EntityRendererRegistry.register(ModEntities.CHOMPER, ChomperRenderer::new);
    }
}
