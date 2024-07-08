package net.pbldmngz.realistic_armor_weight.world.gen;

public class ModWorldGen {
    public static void generateWorldGen() {
        ModOreGeneration.generateOres();

        ModTreeGeneration.generateTrees();
        ModFlowerGeneration.generateFlowers();

        ModEntitySpawn.addEntitySpawn();
    }
}
