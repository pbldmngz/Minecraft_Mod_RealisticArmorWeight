package net.pbldmngz.realistic_armor_weight.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.pbldmngz.realistic_armor_weight.RealisticArmorWeight;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItemGroup {
    public static ItemGroup TANZANITE;

    public static void registerItemGroup() {
        TANZANITE = FabricItemGroup.builder(new Identifier(RealisticArmorWeight.MOD_ID, "tanzanite"))
                .displayName(Text.literal("Tanzanite Item Group"))
                .icon(() -> new ItemStack(ModItems.TANZANITE)).build();
    }
}
