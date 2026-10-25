package com.airportarchitect.registry;

import com.airportarchitect.AirportArchitect;
import com.airportarchitect.item.BlueprintToolItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AirportArchitect.MOD_ID);
    public static final RegistryObject<Item> BLUEPRINT_TOOL = ITEMS.register("blueprint_tool",
            () -> new BlueprintToolItem(new Item.Properties().stacksTo(1)));
    private ModItems() { }
}
