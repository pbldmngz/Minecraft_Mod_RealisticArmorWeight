package net.pbldmngz.realistic_armor_weight.block.entity.client;

import net.pbldmngz.realistic_armor_weight.block.custom.GemInfusingStationBlock;
import net.pbldmngz.realistic_armor_weight.block.entity.GemInfusingBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

public class GemInfusingStationBlockEntityRenderer implements BlockEntityRenderer<GemInfusingBlockEntity> {
    public GemInfusingStationBlockEntityRenderer(BlockEntityRendererFactory.Context context) {

    }

    @Override
    public void render(GemInfusingBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();

        ItemStack itemStack = entity.getRenderStack();
        matrices.push();
        matrices.translate(0.5f, 0.645f, 0.5f);
        matrices.scale(0.2f, 0.2f, 0.2f);
        matrices.multiply(RotationAxis.POSITIVE_X.rotation(-90));

        switch (entity.getCachedState().get(GemInfusingStationBlock.FACING)) {
            case NORTH -> matrices.multiply(RotationAxis.POSITIVE_Z.rotation(180));
            case EAST -> matrices.multiply(RotationAxis.POSITIVE_Z.rotation(270));
            case SOUTH -> matrices.multiply(RotationAxis.POSITIVE_Z.rotation(0));
            case WEST -> matrices.multiply(RotationAxis.POSITIVE_Z.rotation(90));
        }

        itemRenderer.renderItem(itemStack, ModelTransformation.Mode.GUI, getLightLevel(entity.getWorld(), entity.getPos()),
                OverlayTexture.DEFAULT_UV, matrices, vertexConsumers, 1);
        matrices.pop();
    }

    private int getLightLevel(World world, BlockPos pos) {
        int bLight = world.getLightLevel(LightType.BLOCK, pos);
        int sLight = world.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(bLight, sLight);
    }
}
