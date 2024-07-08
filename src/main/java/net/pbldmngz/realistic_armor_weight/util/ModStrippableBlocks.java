package net.pbldmngz.realistic_armor_weight.util;

import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.pbldmngz.realistic_armor_weight.block.ModBlocks;

public class ModStrippableBlocks {
    public static void registerStrippables() {
        StrippableBlockRegistry.register(ModBlocks.DOGWOOD_LOG, ModBlocks.STRIPPED_DOGWOOD_LOG);
        StrippableBlockRegistry.register(ModBlocks.DOGWOOD_WOOD, ModBlocks.STRIPPED_DOGWOOD_WOOD);
    }
}
