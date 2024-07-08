package net.pbldmngz.realistic_armor_weight.fluid;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.pbldmngz.realistic_armor_weight.RealisticArmorWeight;
import net.pbldmngz.realistic_armor_weight.item.ModItemGroup;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModFluids {
    public static FlowableFluid STILL_SOAP_WATER;
    public static FlowableFluid FLOWING_SOAP_WATER;
    public static Block SOAP_WATER_BLOCK;
    public static Item SOAP_WATER_BUCKET;

    public static void register() {
        STILL_SOAP_WATER = Registry.register(Registries.FLUID,
                new Identifier(RealisticArmorWeight.MOD_ID, "soap_water"), new SoapWaterFluid.Still());
        FLOWING_SOAP_WATER = Registry.register(Registries.FLUID,
                new Identifier(RealisticArmorWeight.MOD_ID, "flowing_soap_water"), new SoapWaterFluid.Flowing());

        SOAP_WATER_BLOCK = Registry.register(Registries.BLOCK, new Identifier(RealisticArmorWeight.MOD_ID, "soap_water_block"),
                new FluidBlock(ModFluids.STILL_SOAP_WATER, FabricBlockSettings.copyOf(Blocks.WATER)){ });
        SOAP_WATER_BUCKET = Registry.register(Registries.ITEM, new Identifier(RealisticArmorWeight.MOD_ID, "soap_water_bucket"),
                new BucketItem(ModFluids.STILL_SOAP_WATER, new FabricItemSettings().recipeRemainder(Items.BUCKET).maxCount(1)));

        ItemGroupEvents.modifyEntriesEvent(ModItemGroup.TANZANITE).register(entries -> entries.add(SOAP_WATER_BUCKET));
    }
}
