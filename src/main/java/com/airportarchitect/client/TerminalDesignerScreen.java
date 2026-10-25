package com.airportarchitect.client;

import com.airportarchitect.network.GenerateTerminalPacket;
import com.airportarchitect.network.ModNetwork;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

/** Grid-first terminal planner with room painting and local undo/redo history. */
public class TerminalDesignerScreen extends Screen {
    private static final int COLS = 12;
    private static final int ROWS = 8;
    private static final int CELL = 22;
    private static final int[] COLORS = {0xFF30343A, 0xFF648A9B, 0xFFB9A16C, 0xFFB87953, 0xFF7D719F,
            0xFF4C8C85, 0xFF719A68, 0xFF9C9C9C, 0xFF8D7771};
    private static final String[] ROOMS = {"Erase", "Public hall", "Check-in", "Security", "Shops", "Gate lounge", "Baggage", "Restrooms", "Staff"};
    private byte[] plan = new byte[COLS * ROWS];
    private final Deque<byte[]> undo = new ArrayDeque<>();
    private final Deque<byte[]> redo = new ArrayDeque<>();
    private int selected = 1;
    private int gridX, gridY;

    public TerminalDesignerScreen() { super(Component.literal("Airport Architect · Floor Planner")); }

    @Override
    protected void init() {
        clearWidgets();
        int left = (width - COLS * CELL) / 2;
        int top = Math.max(42, (height - ROWS * CELL) / 2 - 10);
        gridX = left;
        gridY = top;
        int toolbarY = Math.min(height - 72, gridY + ROWS * CELL + 12);
        int totalW = Math.min(width - 16, 540);
        int bx = (width - totalW) / 2;
        int bw = Math.max(54, (totalW - 8 * 4) / 9);
        for (int i = 0; i < ROOMS.length; i++) {
            final int room = i;
            int row = i / 5;
            int col = i % 5;
            int buttonW = Math.min(100, (width - 24) / 5 - 4);
            addRenderableWidget(Button.builder(Component.literal(ROOMS[i]), b -> selected = room)
                    .bounds(12 + col * (buttonW + 4), toolbarY + row * 23, buttonW, 20).build());
        }
        int controlsY = height - 27;
        addRenderableWidget(Button.builder(Component.literal("Undo"), b -> undo()).bounds(bx, controlsY, 68, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Redo"), b -> redo()).bounds(bx + 72, controlsY, 68, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Clear plan"), b -> clearPlan()).bounds(bx + 144, controlsY, 82, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Build terminal"), b -> build()).bounds(width - bx - 112, controlsY, 112, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(width - bx - 54, 8, 54, 20).build());
    }

    private void remember() {
        undo.push(Arrays.copyOf(plan, plan.length));
        while (undo.size() > 100) undo.removeLast();
        redo.clear();
    }
    private void undo() {
        if (!undo.isEmpty()) { redo.push(Arrays.copyOf(plan, plan.length)); plan = undo.pop(); }
    }
    private void redo() {
        if (!redo.isEmpty()) { undo.push(Arrays.copyOf(plan, plan.length)); plan = redo.pop(); }
    }
    private void clearPlan() { remember(); Arrays.fill(plan, (byte) 0); }
    private void build() {
        ModNetwork.CHANNEL.sendToServer(new GenerateTerminalPacket(Arrays.copyOf(plan, plan.length)));
        onClose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            int col = (int) ((mouseX - gridX) / CELL);
            int row = (int) ((mouseY - gridY) / CELL);
            if (mouseX >= gridX && mouseY >= gridY && col >= 0 && col < COLS && row >= 0 && row < ROWS) {
                int idx = row * COLS + col;
                byte value = (byte) (button == 1 ? 0 : selected);
                if (plan[idx] != value) { remember(); plan[idx] = value; }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g);
        int panelW = Math.min(width - 8, 560);
        int panelL = (width - panelW) / 2;
        g.fill(panelL, 3, panelL + panelW, height - 2, 0xEE171B20);
        g.fill(panelL, 3, panelL + panelW, 28, 0xFF292F36);
        g.drawCenteredString(font, title, width / 2, 10, 0xFFF1D7A0);
        g.drawCenteredString(font, "PAINT A FLOOR PLAN  ·  Left-click: place   Right-click: erase", width / 2, gridY - 20, 0xFFE2E7EC);
        g.drawCenteredString(font, "Selected: " + ROOMS[selected] + "   |   " + filledCells() + " planned zones", width / 2, gridY + ROWS * CELL + 3, 0xFFB9C7D4);
        for (int r = 0; r < ROWS; r++) for (int c = 0; c < COLS; c++) {
            int x = gridX + c * CELL, y = gridY + r * CELL;
            int type = plan[r * COLS + c];
            g.fill(x, y, x + CELL - 1, y + CELL - 1, COLORS[type]);
            if (type != 0) g.drawCenteredString(font, roomGlyph(type), x + CELL / 2, y + 7, 0xFFFFFFFF);
        }
        g.drawCenteredString(font, "Rooms become full-height spaces; leave cells empty for open circulation/landscape.", width / 2, Math.min(height - 82, gridY + ROWS * CELL + 34), 0xFF9DAAB7);
        super.render(g, mouseX, mouseY, partialTick);
    }

    private int filledCells() { int n = 0; for (byte b : plan) if (b != 0) n++; return n; }
    private String roomGlyph(int type) { return switch (type) { case 1 -> "H"; case 2 -> "C"; case 3 -> "S"; case 4 -> "$"; case 5 -> "G"; case 6 -> "B"; case 7 -> "R"; default -> "A"; }; }
    @Override public boolean isPauseScreen() { return false; }
}
