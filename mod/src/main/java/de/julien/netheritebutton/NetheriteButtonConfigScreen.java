package de.julien.netheritebutton;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class NetheriteButtonConfigScreen extends Screen {
    private final Screen parent;

    private EditBox replacementBox;
    private Button enabledButton;
    private Button colorButton;

    private boolean enabled;
    private String color;

    private static final String[] COLORS = {"GRAY", "GREEN", "YELLOW", "AQUA", "RED", "WHITE"};
    private static final String[] COLOR_NAMES = {"Grau", "Grün", "Gelb", "Aqua", "Rot", "Weiß"};

    public NetheriteButtonConfigScreen(Screen parent) {
        super(Component.literal("Netherite Button"));
        this.parent = parent;
        this.enabled = NetheriteButtonClient.CONFIG.enabled;
        this.color = NetheriteButtonClient.CONFIG.color;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int startY = this.height / 2 - 70;

        this.enabledButton = this.addRenderableWidget(
                Button.builder(enabledText(), button -> {
                    this.enabled = !this.enabled;
                    button.setMessage(enabledText());
                }).bounds(centerX - 120, startY, 240, 20).build()
        );

        this.replacementBox = new EditBox(
                this.font,
                centerX - 120,
                startY + 48,
                240,
                20,
                Component.literal("Text statt Preis")
        );
        this.replacementBox.setMaxLength(64);
        this.replacementBox.setValue(NetheriteButtonClient.CONFIG.replacementText);
        this.addRenderableWidget(this.replacementBox);

        this.colorButton = this.addRenderableWidget(
                Button.builder(colorText(), button -> {
                    int index = colorIndex();
                    this.color = COLORS[(index + 1) % COLORS.length];
                    button.setMessage(colorText());
                }).bounds(centerX - 120, startY + 82, 240, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Zurücksetzen"), button -> resetValues())
                        .bounds(centerX - 120, startY + 122, 115, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Speichern"), button -> saveAndClose())
                        .bounds(centerX + 5, startY + 122, 115, 20).build()
        );

        this.addRenderableWidget(
                Button.builder(Component.literal("Abbrechen"), button -> this.minecraft.setScreen(this.parent))
                        .bounds(centerX - 120, startY + 148, 240, 20).build()
        );
    }

    private Component enabledText() {
        return Component.literal("Preistext ersetzen: " + (this.enabled ? "AN" : "AUS"));
    }

    private int colorIndex() {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i].equals(this.color)) return i;
        }
        return 0;
    }

    private Component colorText() {
        return Component.literal("Farbe: " + COLOR_NAMES[colorIndex()]);
    }

    private void resetValues() {
        this.enabled = true;
        this.color = "GRAY";
        this.replacementBox.setValue("Custom");
        this.enabledButton.setMessage(enabledText());
        this.colorButton.setMessage(colorText());
    }

    private void saveAndClose() {
        String text = this.replacementBox.getValue().trim();
        if (text.isEmpty()) text = "Custom";

        NetheriteButtonClient.CONFIG.enabled = this.enabled;
        NetheriteButtonClient.CONFIG.replacementText = text;
        NetheriteButtonClient.CONFIG.color = this.color;
        NetheriteButtonClient.CONFIG.save();

        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xD0101216);
        super.render(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int startY = this.height / 2 - 70;

        graphics.drawCenteredString(this.font, this.title, centerX, startY - 36, 0xFFFFFF);
        graphics.drawCenteredString(
                this.font,
                Component.literal("Die Menü-Taste legst du selbst unter Optionen → Steuerung fest."),
                centerX,
                startY - 20,
                0xA0A0A0
        );

        graphics.drawString(
                this.font,
                Component.literal("Text statt $-Preis:"),
                centerX - 120,
                startY + 34,
                0xD0D0D0
        );

        String preview = this.replacementBox == null ? "Custom" : this.replacementBox.getValue();
        if (preview.isBlank()) preview = "Custom";

        ChatFormatting formatting;
        try {
            formatting = ChatFormatting.valueOf(this.color);
        } catch (IllegalArgumentException ex) {
            formatting = ChatFormatting.GRAY;
        }

        graphics.drawCenteredString(
                this.font,
                Component.literal("Vorschau: ").append(Component.literal(preview).withStyle(formatting)),
                centerX,
                startY + 108,
                0xFFFFFF
        );
    }
}
