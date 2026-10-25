package com.airportarchitect.client;

import com.airportarchitect.network.GenerateTerminalPacket;
import com.airportarchitect.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class TerminalDesignerScreen extends Screen {
    public TerminalDesignerScreen() { super(Component.literal("Airport Architect")); }

    @Override
    protected void init() {
        int center = this.width / 2;
        int top = this.height / 2 - 38;
        addRenderableWidget(Button.builder(Component.literal("Small terminal · 20 x 30"), b -> generate(0))
                .bounds(center - 105, top, 210, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Medium terminal · 30 x 42"), b -> generate(1))
                .bounds(center - 105, top + 26, 210, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Large terminal · 40 x 54"), b -> generate(2))
                .bounds(center - 105, top + 52, 210, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"), b -> onClose())
                .bounds(center - 50, top + 82, 100, 20).build());
    }

    private void generate(int preset) {
        ModNetwork.CHANNEL.sendToServer(new GenerateTerminalPacket(preset));
        onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, height / 2 - 68, 0xFFFFFF);
        graphics.drawCenteredString(font, "Instant generation · Creative mode", width / 2, height / 2 + 72, 0xB8DDF5);
        graphics.drawCenteredString(font, "Builds in front of you; clear the area first.", width / 2, height / 2 + 84, 0xCCCCCC);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
