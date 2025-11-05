package Thexiaoyu.thrown.init;

import Thexiaoyu.thrown.Thrown;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item KZZYC = Registry.register(
            Registries.ITEM,
            new Identifier(Thrown.MOD_ID, "kzzyc"),
            new Item(new FabricItemSettings())
    );

    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(content -> {
            content.add(KZZYC);
        });
    }
}