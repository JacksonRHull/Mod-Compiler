package com.airportarchitect.network;

import com.airportarchitect.AirportArchitect;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.SimpleChannel;

public final class ModNetwork {
    private static final String PROTOCOL = "2";
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AirportArchitect.MOD_ID, "main"), () -> PROTOCOL,
            PROTOCOL::equals, PROTOCOL::equals);
    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        CHANNEL.messageBuilder(GenerateTerminalPacket.class, 0)
                .encoder(GenerateTerminalPacket::encode)
                .decoder(GenerateTerminalPacket::decode)
                .consumerMainThread(GenerateTerminalPacket::handle)
                .add();
        registered = true;
    }
    private ModNetwork() { }
}
