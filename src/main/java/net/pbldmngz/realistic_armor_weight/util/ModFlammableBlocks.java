package net.pbldmngz.realistic_armor_weight.util;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.pbldmngz.realistic_armor_weight.block.ModBlocks;

public class ModFlammableBlocks {
    public static void registerFlammableBlocks() {
        FlammableBlockRegistry registry = FlammableBlockRegistry.getDefaultInstance();

        registry.add(ModBlocks.DOGWOOD_LOG, 5, 5);
        registry.add(ModBlocks.DOGWOOD_WOOD, 5, 5);
        registry.add(ModBlocks.STRIPPED_DOGWOOD_LOG, 5, 5);
        registry.add(ModBlocks.STRIPPED_DOGWOOD_WOOD, 5, 5);

        registry.add(ModBlocks.DOGWOOD_PLANKS, 5, 20);
        registry.add(ModBlocks.DOGWOOD_LEAVES, 30, 60);
    }
}
