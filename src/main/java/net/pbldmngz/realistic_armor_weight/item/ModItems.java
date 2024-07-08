package net.pbldmngz.realistic_armor_weight.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.pbldmngz.realistic_armor_weight.RealisticArmorWeight;
import net.pbldmngz.realistic_armor_weight.block.ModBlocks;
import net.pbldmngz.realistic_armor_weight.entity.ModEntities;
import net.pbldmngz.realistic_armor_weight.item.custom.EightBallItem;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item RAW_TANZANITE = registerItem("raw_tanzanite",
            new Item(new FabricItemSettings()));
    public static final Item TANZANITE = registerItem("tanzanite",
            new Item(new FabricItemSettings()));

    public static final Item EIGHT_BALL = registerItem("eight_ball",
            new EightBallItem(new FabricItemSettings().maxCount(1)));
    public static final Item EGGPLANT_SEEDS = registerItem("eggplant_seeds",
            new AliasedBlockItem(ModBlocks.EGGPLANT_CROP,
                    new FabricItemSettings()));
    public static final Item EGGPLANT = registerItem("eggplant",
            new Item(new FabricItemSettings()
                    .food(new FoodComponent.Builder().hunger(4).saturationModifier(4f).build())));

    public static final Item KAUPENSWORD = registerItem("kaupensword",
            new SwordItem(ToolMaterials.DIAMOND, 10, 5f,
                    new FabricItemSettings().maxCount(1)));

    public static final Item CHOMPER_SPAWN_EGG = registerItem("chomper_spawn_egg",
            new SpawnEggItem(ModEntities.CHOMPER,0x22b341, 0x19732e,
                    new FabricItemSettings()));

    public static final Item TANZANITE_PICKAXE = registerItem("tanzanite_pickaxe",
            new PickaxeItem(ModToolMaterial.TANZANITE, 4, 2f,
                    new FabricItemSettings().maxCount(1)));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(RealisticArmorWeight.MOD_ID, name), item);
    }

    public static void addItemsToItemGroups() {
        addToItemGroup(ItemGroups.INGREDIENTS, RAW_TANZANITE);
        addToItemGroup(ItemGroups.INGREDIENTS, TANZANITE);

        addToItemGroup(ModItemGroup.TANZANITE, RAW_TANZANITE);
        addToItemGroup(ModItemGroup.TANZANITE, TANZANITE);
        addToItemGroup(ModItemGroup.TANZANITE, EIGHT_BALL);
        addToItemGroup(ModItemGroup.TANZANITE, KAUPENSWORD);
        addToItemGroup(ModItemGroup.TANZANITE, EGGPLANT_SEEDS);
        addToItemGroup(ModItemGroup.TANZANITE, EGGPLANT);
        addToItemGroup(ModItemGroup.TANZANITE, TANZANITE_PICKAXE);
        addToItemGroup(ModItemGroup.TANZANITE, CHOMPER_SPAWN_EGG);
    }

    public static void addToItemGroup(ItemGroup group, Item item) {
        ItemGroupEvents.modifyEntriesEvent(group).register(entries -> entries.add(item));
    }

    public static void registerModItems() {
        RealisticArmorWeight.LOGGER.debug("Registering Mod Items for " + RealisticArmorWeight.MOD_ID);

        addItemsToItemGroups();
    }
}
