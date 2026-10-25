package com.airportarchitect.network;

import com.airportarchitect.world.TerminalGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.Arrays;
import java.util.function.Supplier;

public class GenerateTerminalPacket {
    public static final int COLS = 12;
    public static final int ROWS = 8;
    private final byte[] plan;

    public GenerateTerminalPacket(byte[] plan) {
        this.plan = new byte[COLS * ROWS];
        if (plan != null) System.arraycopy(plan, 0, this.plan, 0, Math.min(plan.length, this.plan.length));
        for (int i = 0; i < this.plan.length; i++) if (this.plan[i] < 0 || this.plan[i] > 8) this.plan[i] = 0;
    }
    public static void encode(GenerateTerminalPacket packet, FriendlyByteBuf buffer) { buffer.writeByteArray(packet.plan); }
    public static GenerateTerminalPacket decode(FriendlyByteBuf buffer) {
        byte[] incoming = buffer.readByteArray(COLS * ROWS);
        return new GenerateTerminalPacket(incoming);
    }
    public static void handle(GenerateTerminalPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.getAbilities().instabuild) return;
            if (Arrays.stream(toInts(packet.plan)).noneMatch(v -> v != 0)) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Airport Architect: paint at least one zone before building."));
                return;
            }
            BlockPos start = player.blockPosition().relative(player.getDirection(), 5).above();
            int zones = TerminalGenerator.generatePlan(player.serverLevel(), start, packet.plan, COLS, ROWS);
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Airport Architect: built a planned terminal from " + zones + " zones."));
        });
        context.setPacketHandled(true);
    }
    private static int[] toInts(byte[] values) { int[] out = new int[values.length]; for (int i=0;i<values.length;i++) out[i] = values[i]; return out; }
}
