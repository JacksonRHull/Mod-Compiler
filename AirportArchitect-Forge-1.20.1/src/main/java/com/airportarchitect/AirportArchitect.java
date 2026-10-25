package com.airportarchitect;

import com.airportarchitect.network.ModNetwork;
import com.airportarchitect.registry.ModItems;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AirportArchitect.MOD_ID)
public class AirportArchitect {
    public static final String MOD_ID = "airportarchitect";

    public AirportArchitect() {
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModNetwork.register();
    }
}
