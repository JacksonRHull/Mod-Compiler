package com.airportarchitect.network;

import com.airportarchitect.world.TerminalGenerator;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class GenerateTerminalPacket {
    private final int preset;
    public GenerateTerminalPacket(int preset) { this.preset = Math.max(0, Math.min(2, preset)); }

    public static void encode(GenerateTerminalPacket packet, FriendlyByteBuf buffer) { buffer.writeVarInt(packet.preset); }
    public static GenerateTerminalPacket decode(FriendlyByteBuf buffer) { return new GenerateTerminalPacket(buffer.readVarInt()); }

    public static void handle(GenerateTerminalPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.getAbilities().instabuild) return;
            int width = switch (packet.preset) { case 1 -> 30; case 2 -> 40; default -> 20; };
            int length = switch (packet.preset) { case 1 -> 42; case 2 -> 54; default -> 30; };
            int height = switch (packet.preset) { case 1 -> 10; case 2 -> 12; default -> 8; };
            BlockPos start = player.blockPosition().relative(player.getDirection(), 5).above();
            TerminalGenerator.generate(player.serverLevel(), start, width, length, height);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "Airport Architect: generated a " + width + " x " + length + " terminal."));
        });
        context.setPacketHandled(true);
    }
}
