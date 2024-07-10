package net.pbldmngz.realistic_armor_weight;

import net.fabricmc.api.ClientModInitializer;
import net.pbldmngz.realistic_armor_weight.network.ArmorWeightPackets;

public class ArmorWeightModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ArmorWeightPackets.registerClientPackets();
    }
}