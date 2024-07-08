package net.pbldmngz.realistic_armor_weight;

import net.fabricmc.api.ModInitializer;
import net.pbldmngz.realistic_armor_weight.configs.ArmorWeightConfig;

public class ArmorWeightMod implements ModInitializer {
    public static final ArmorWeightConfig CONFIG = new ArmorWeightConfig();

    @Override
    public void onInitialize() {
        CONFIG.loadConfig();
    }
}