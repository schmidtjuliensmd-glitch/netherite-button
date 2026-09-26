package de.sleepclient;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class SleepActivationScreen extends Screen {
    private EditBox keyBox;
    private Button activateButton;
    private String status = "Enter your product key to activate Sleep Client.";
    private boolean checking;

    public SleepActivationScreen() {
        super(Component.literal("Sleep Client Activation"));
    }

    @Override
    protected void init() {
        int panelW = Math.min(this.width - 40, 440);
        int x = (this.width - panelW) / 2;
        int y = Math.max(30, (this.height - 230) / 2);

        keyBox = new EditBox(
                this.font,
                x + 28,
                y + 108,
                panelW - 56,
                28,
                Component.literal("Product Key")
        );
        keyBox.setHint(Component.literal("SLEEP..."));
        keyBox.setMaxLength(512);
        addRenderableWidget(keyBox);

        activateButton = Button.builder(
                Component.literal("Activate Sleep Client"),
                button -> activate()
        ).bounds(x + 28, y + 148, panelW - 56, 28).build();
        addRenderableWidget(activateButton);

        setInitialFocus(keyBox);
    }

    private void activate() {
        if (checking) return;

        String key = keyBox.getValue().trim();
        if (key.isBlank()) {
            status = "Enter your product key.";
            return;
        }

        Minecraft client = Minecraft.getInstance();
        String mcName = client.getUser().getName();

        checking = true;
        activateButton.active = false;
        status = "Checking license...";

        LicenseManager.saveAndVerify(mcName, key).thenAccept(ok ->
                client.execute(() -> {
                    checking = false;
                    activateButton.active = true;

                    if (ok) {
                        status = "Activated as " + mcName + " (" + LicenseManager.plan() + ")";
                        UpdateManager.checkForUpdates();
                        client.setScreen(new SleepClickGuiScreen());
                    } else {
                        status = switch (LicenseManager.message()) {
                            case "invalid_key" -> "Invalid product key.";
                            case "key_not_bound_to_username" -> "This key belongs to another Minecraft account.";
                            case "license_expired" -> "This license has expired.";
                            case "license_server_unreachable" -> "Could not reach the license server.";
                            default -> "Activation failed: " + LicenseManager.message();
                        };
                    }
                })
        );
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);

        int panelW = Math.min(this.width - 40, 440);
        int panelH = 230;
        int x = (this.width - panelW) / 2;
        int y = Math.max(30, (this.height - panelH) / 2);

        g.fill(x, y, x + panelW, y + panelH, 0xF20D0911);
        g.fill(x, y, x + panelW, y + 1, 0xFFFF4FA3);
        g.fill(x, y + panelH - 1, x + panelW, y + panelH, 0xFF332238);
        g.fill(x, y, x + 1, y + panelH, 0xFF332238);
        g.fill(x + panelW - 1, y, x + panelW, y + panelH, 0xFF332238);

        g.drawCenteredString(this.font, "Sleep Client", this.width / 2, y + 24, 0xFFF4EDF6);
        g.drawCenteredString(this.font, "Activation", this.width / 2, y + 43, 0xFFFF4FA3);

        String mcName = Minecraft.getInstance().getUser().getName();
        g.drawString(this.font, "Minecraft Account", x + 28, y + 70, 0xFF9A8E9E, false);
        g.drawString(this.font, mcName, x + 28, y + 84, 0xFFF4EDF6, true);

        g.drawString(this.font, "Product Key", x + 28, y + 97, 0xFF9A8E9E, false);
        g.drawCenteredString(this.font, status, this.width / 2, y + 190, 0xFFB7A9B9);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
