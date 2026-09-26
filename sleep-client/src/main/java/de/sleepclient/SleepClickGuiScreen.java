package de.sleepclient;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class SleepClickGuiScreen extends Screen {
    private static final int W = 604, H = 498, SIDEBAR = 190;
    private static final float COMPACT_SCALE = .88f * .95f;
    private static final int TEXT = 0xFFF7F3F8, MUTED = 0xFF9F909C, QUIET = 0xFF706372;
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");
    private static final ModuleCategory[] CATEGORIES = {
            ModuleCategory.COMBAT, ModuleCategory.MOVEMENT, ModuleCategory.DONUT_SMP,
            ModuleCategory.VISUALS, ModuleCategory.MISC
    };
    private static final List<String> FIRST_COMBAT = List.of(
            "Auto Crystal", "Anchor Macro", "Aim Assist", "Auto Totem",
            "Reach", "Auto Mace", "Trigger Bot", "Auto Refill Hotbar");

    private final long openedAt = System.nanoTime();
    private long lastFrame = openedAt;
    private final Map<Module, Float> toggles = new HashMap<>();
    private final Map<Module, Float> hovers = new HashMap<>();
    private ModuleCategory selected = ModuleCategory.COMBAT;
    private int scroll;
    private String query = "";
    private EditBox search;
    private ModuleSettingsPanel settings;
    private GuiLayout layout = GuiLayout.fit(624, 518, W, H, 1f);
    private float frameBlend = .2f;

    public SleepClickGuiScreen() {
        super(Component.literal("Sleep Client"));
    }

    @Override
    protected void init() {
        search = new EditBox(font, 0, 0, 190, 32, Component.literal("Module suchen"));
        search.setMaxLength(64);
        search.setBordered(false);
        search.setValue(query);
        search.setResponder(value -> { query = value; scroll = 0; });
        addWidget(search); // Native keyboard handling; the visible text uses our smooth font.
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        long now = System.nanoTime();
        float elapsed = Math.min(.1f, Math.max(0f, (now - lastFrame) / 1_000_000_000f));
        lastFrame = now;
        frameBlend = 1f - (float) Math.exp(-16f * elapsed * ThemeConfig.animationSpeed);
        float progress = Math.min(1f, (now - openedAt) / (220_000_000f / ThemeConfig.animationSpeed));
        float eased = 1f - (float) Math.pow(1f - progress, 3);
        layout = GuiLayout.fit(width, height, W, H, ThemeConfig.guiScale);
        // Apply after fitting so the menu also shrinks at Minecraft's larger GUI scales.
        float entranceScale = COMPACT_SCALE * (.98f + .02f * eased);
        layout = new GuiLayout(layout.x() + W * layout.scale() * (1f - entranceScale) / 2f,
                layout.y() + H * layout.scale() * (1f - entranceScale) / 2f,
                layout.scale() * entranceScale, W, H);
        double mx = layout.localX(mouseX), my = layout.localY(mouseY);

        search.setX(Math.round(layout.screenX(408)));
        search.setY(Math.round(layout.screenY(17)));
        search.setWidth(Math.max(1, Math.round(190 * layout.scale())));
        search.setHeight(Math.max(1, Math.round(32 * layout.scale())));
        search.active = settings == null;

        g.pose().pushMatrix();
        g.pose().translate(layout.x(), layout.y());
        g.pose().scale(layout.scale(), layout.scale());
        renderShell(g);
        renderSidebar(g, mx, my);
        if (settings == null) {
            renderHeader(g);
            renderModules(g, mx, my);
        } else {
            settings.render(g, layout, mx, my);
        }
        g.pose().popMatrix();
    }

    private void renderShell(GuiGraphics g) {
        SmoothShapeRenderer.roundedGradient(g, 0, 0, W, H, 22, 0xFF1B101E, 0xFF09070D, false);
        SmoothShapeRenderer.roundedOutline(g, 0, 0, W, H, 22, 0xFF352B3A);
        SmoothShapeRenderer.roundedRect(g, 1, 1, SIDEBAR - 1, H - 2, 21, 0xFF0C080F);
        g.fill(166, 1, SIDEBAR, H - 1, 0xFF0C080F);
        g.fill(SIDEBAR - 1, 0, SIDEBAR, H - 1, 0xFF25182A);

        SmoothShapeRenderer.roundedGradient(g, 19, 21, 32, 32, 11,
                ThemeConfig.accent, ThemeConfig.accentSecondary, true);
        GuiIconRenderer.draw(g, GuiIconArtwork.Icon.MOON, 25, 27, 20, 0xFFFFFFFF);
        SmoothTextRenderer.draw(g, "Sleep Client", 59, 22, 13f, TEXT, true);
        SmoothTextRenderer.draw(g, "v" + UpdateManager.currentVersion(), 59, 43, 9f, QUIET, false);
    }

    private void renderSidebar(GuiGraphics g, double mx, double my) {
        SmoothTextRenderer.draw(g, "MODULE", 20, 88, 9f, QUIET, true);
        for (int i = 0; i < CATEGORIES.length; i++) {
            ModuleCategory category = CATEGORIES[i];
            GuiIconArtwork.Icon icon = switch (category) {
                case COMBAT -> GuiIconArtwork.Icon.COMBAT;
                case MOVEMENT -> GuiIconArtwork.Icon.MOVEMENT;
                case DONUT_SMP -> GuiIconArtwork.Icon.DONUT;
                case VISUALS -> GuiIconArtwork.Icon.VISUALS;
                case MISC -> GuiIconArtwork.Icon.MISC;
                case GUI -> GuiIconArtwork.Icon.GUI;
            };
            renderNavigation(g, icon, category.displayName(), 109 + i * 42,
                    selected == category, mx, my);
        }
        SmoothTextRenderer.draw(g, "ALLGEMEIN", 20, 342, 9f, QUIET, true);
        renderNavigation(g, GuiIconArtwork.Icon.GUI, "GUI", 363, selected == ModuleCategory.GUI, mx, my);
        renderNavigation(g, GuiIconArtwork.Icon.SETTINGS, "Settings", 405, false, mx, my);
        renderProfile(g);
    }

    private void renderProfile(GuiGraphics g) {
        int x = 12;
        int y = 452;
        int w = 165;
        int h = 34;

        SmoothShapeRenderer.roundedRect(g, x, y, w, h, 9, 0xFF151019);
        SmoothShapeRenderer.roundedOutline(g, x, y, w, h, 9, 0xFF2E2433);

        if (LicenseManager.verified()) {
            if (minecraft != null && minecraft.player != null) {
                PlayerFaceRenderer.draw(g, minecraft.player.getSkin(), x + 7, y + 5, 24, 0xFFFFFFFF);
            } else {
                SmoothShapeRenderer.roundedGradient(g, x + 7, y + 5, 24, 24, 7,
                        ThemeConfig.accent, ThemeConfig.accentSecondary, true);
                GuiIconRenderer.draw(g, GuiIconArtwork.Icon.MOON, x + 11, y + 9, 16, 0xFFFFFFFF);
            }

            String username = SmoothTextRenderer.fit(LicenseManager.username(), 10.5f, true, 112);
            SmoothTextRenderer.draw(g, username, x + 39, y + 6, 10.5f, TEXT, true);

            String plan = switch (LicenseManager.plan().toLowerCase(Locale.ROOT)) {
                case "monthly" -> "Monthly";
                case "lifetime" -> "Lifetime";
                default -> "Angemeldet";
            };
            SmoothTextRenderer.draw(g, plan, x + 39, y + 20, 8f, MUTED, false);
        } else {
            SmoothShapeRenderer.roundedGradient(g, x + 7, y + 5, 24, 24, 7,
                    0xFF302638, 0xFF211925, true);
            GuiIconRenderer.draw(g, GuiIconArtwork.Icon.MOON, x + 11, y + 9, 16, MUTED);
            SmoothTextRenderer.draw(g, "Nicht angemeldet", x + 39, y + 11, 9.5f, MUTED, true);
        }
    }

    private void renderNavigation(GuiGraphics g, GuiIconArtwork.Icon icon, String label, int y,
                                  boolean active, double mx, double my) {
        boolean hover = GuiLayout.inside(mx, my, 12, y, 165, 38);
        if (active) {
            SmoothShapeRenderer.roundedGradient(g, 12, y, 165, 38, 9, 0xFF29132B, 0xFF1B1228, true);
            SmoothShapeRenderer.roundedRect(g, 12, y + 6, 2, 26, 1, ThemeConfig.accent);
        } else if (hover) {
            SmoothShapeRenderer.roundedRect(g, 12, y, 165, 38, 9, 0xFF19111E);
        }
        int color = active ? TEXT : MUTED;
        GuiIconRenderer.draw(g, icon, 21, y + 11, 15, active ? ThemeConfig.accent : color);
        SmoothTextRenderer.draw(g, label, 42, y + 11, 12f, color, true);
    }

    private void renderHeader(GuiGraphics g) {
        String title = query.isBlank() ? switch (selected) {
            case COMBAT -> "Kampf";
            case MOVEMENT -> "Movement";
            case DONUT_SMP -> "Base Finding Tools";
            case VISUALS -> "Visuals";
            case MISC -> "Misc";
            case GUI -> "GUI";
        } : "Suche";
        SmoothTextRenderer.draw(g, title, 205, 24, 13f, TEXT, true);
        SmoothTextRenderer.draw(g, LocalTime.now().format(TIME), 361, 24, 12f, ThemeConfig.accent, true);
        SmoothShapeRenderer.roundedRect(g, 408, 17, 190, 32, 9, 0xFF1A111D);
        SmoothShapeRenderer.roundedOutline(g, 408, 17, 190, 32, 9,
                search.isFocused() ? alpha(ThemeConfig.accent, 150) : 0xFF33263A);
        String text = query.isEmpty() ? "Module suchen..." : query;
        // Keep the caret visible without painting Minecraft's pixel font or its background.
        while (!query.isEmpty() && SmoothTextRenderer.width(text, 10.5f, false) > 162) {
            text = text.substring(text.offsetByCodePoints(0, 1));
        }
        SmoothTextRenderer.draw(g, text, 417, 26, 10.5f, query.isEmpty() ? QUIET : TEXT, false);
        if (search.isFocused() && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cursor = Math.min(586, 417 + (query.isEmpty() ? 0 : SmoothTextRenderer.width(text, 10.5f, false)));
            g.fill(cursor, 27, cursor + 1, 39, TEXT);
        }
        g.fill(205, 54, 598, 55, alpha(ThemeConfig.accent, 46));
    }

    private List<Module> modules() {
        List<Module> modules = new ArrayList<>(query.isBlank() ? ModuleRegistry.byCategory(selected) : ModuleRegistry.all());
        if (!query.isBlank()) {
            String q = query.trim().toLowerCase(Locale.ROOT);
            modules.removeIf(module -> !module.name().toLowerCase(Locale.ROOT).contains(q));
        } else if (selected == ModuleCategory.COMBAT) {
            modules.sort(Comparator.comparingInt(module -> {
                int index = FIRST_COMBAT.indexOf(module.name());
                return index < 0 ? FIRST_COMBAT.size() : index;
            }));
        }
        return modules;
    }

    private void renderModules(GuiGraphics g, double mx, double my) {
        List<Module> modules = modules();
        scroll = Math.min(scroll, GuiLayout.maxGridScroll(modules.size()));
        clip(g, layout, 205, 69, 598, 482);
        int hovered = GuiLayout.cardAt(mx, my, scroll, modules.size());
        for (int i = 0; i < modules.size(); i++) {
            int cy = 69 + (i / 2) * 63 - scroll;
            if (cy + 54 <= 69 || cy >= 482) continue;
            renderModule(g, modules.get(i), 205 + (i % 2) * 201, cy, i == hovered);
        }
        if (modules.isEmpty()) SmoothTextRenderer.draw(g, "Keine Module gefunden.", 217, 92, 12f, MUTED, false);
        g.disableScissor();
        if (GuiLayout.maxGridScroll(modules.size()) > 0 && GuiLayout.inside(mx, my, 190, 59, 414, 424)) {
            int thumb = Math.max(25, Math.round(413f * 413 / (GuiLayout.maxGridScroll(modules.size()) + 413)));
            int top = 69 + Math.round((413f - thumb) * scroll / GuiLayout.maxGridScroll(modules.size()));
            SmoothShapeRenderer.roundedRect(g, 600, top, 2, thumb, 1, alpha(ThemeConfig.accent, 130));
        }
    }

    private void renderModule(GuiGraphics g, Module module, int x, int y, boolean hovered) {
        float hover = hovers.getOrDefault(module, 0f);
        hover += ((hovered ? 1f : 0f) - hover) * frameBlend;
        hovers.put(module, hover);
        float toggle = toggles.getOrDefault(module, module.enabled() ? 1f : 0f);
        toggle += ((module.enabled() ? 1f : 0f) - toggle) * frameBlend;
        toggles.put(module, toggle);
        int bg = mix(0xFF1B121E, 0xFF241726, hover);
        SmoothShapeRenderer.roundedRect(g, x, y, 192, 54, 11, bg);
        int border = module.enabled() ? alpha(ThemeConfig.accent, 118) : mix(0xFF35293A, 0xFF70415F, hover);
        SmoothShapeRenderer.roundedOutline(g, x, y, 192, 54, 11, border);
        String label = SmoothTextRenderer.fit(module.name(), 12f, true, 132);
        SmoothTextRenderer.draw(g, label, x + 12, y + 18, 12f, 0xFFCDBFCA, true);
        drawToggle(g, x + 152, y + 19, toggle);
    }

    static void drawToggle(GuiGraphics g, int x, int y, float amount) {
        SmoothShapeRenderer.roundedRect(g, x, y, 28, 15, 7, 0xFF29303A);
        if (amount > .002f) {
            SmoothShapeRenderer.roundedGradient(g, x, y, 28, 15, 7,
                    alpha(ThemeConfig.accent, Math.round(255 * amount)),
                    alpha(ThemeConfig.accentSecondary, Math.round(255 * amount)), true);
        }
        SmoothShapeRenderer.roundedRect(g, x + 2 + Math.round(13 * amount), y + 2, 11, 11, 5,
                mix(0xFF76808A, 0xFFFFFFFF, amount));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mx = layout.localX(click.x()), my = layout.localY(click.y());
        int button = click.button();
        if (button != 0 && button != 1) return super.mouseClicked(click, doubled);
        if (settings == null && button == 0 && GuiLayout.inside(mx, my, 408, 17, 190, 32)) {
            setFocused(search);
            search.setFocused(true);
            return true;
        }
        search.setFocused(false);
        setFocused(null);

        if (button == 0) {
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (GuiLayout.inside(mx, my, 12, 109 + i * 42, 165, 38)) {
                    selectCategory(CATEGORIES[i]); return true;
                }
            }
            if (GuiLayout.inside(mx, my, 12, 363, 165, 38)) {
                selectCategory(ModuleCategory.GUI); return true;
            }
            if (GuiLayout.inside(mx, my, 12, 405, 165, 38)) {
                closeSettings();
                minecraft.setScreen(new SleepConfigScreen(this));
                return true;
            }
        }
        if (settings != null) {
            if (button == 0 && GuiLayout.inside(mx, my, 205, 17, 32, 32)) {
                closeSettings(); return true;
            }
            return settings.mouseClicked(mx, my, button);
        }
        List<Module> modules = modules();
        int index = GuiLayout.cardAt(mx, my, scroll, modules.size());
        if (index >= 0) {
            Module module = modules.get(index);
            if (button == 1) {
                settings = new ModuleSettingsPanel(module);
            } else {
                module.toggle();
                ConfigManager.save();
            }
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    private void selectCategory(ModuleCategory category) {
        closeSettings();
        selected = category;
        scroll = 0;
        search.setValue("");
    }

    private void closeSettings() {
        if (settings != null) settings.close();
        settings = null;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (settings != null && settings.mouseDragged(layout.localX(event.x()), layout.localY(event.y()))) return true;
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (settings != null) settings.release();
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        double mx = layout.localX(mouseX), my = layout.localY(mouseY);
        if (!GuiLayout.inside(mx, my, 190, 59, 414, 424)) return false;
        if (settings != null) return settings.mouseScrolled(verticalAmount);
        scroll = Math.max(0, Math.min(GuiLayout.maxGridScroll(modules().size()), scroll - (int) (verticalAmount * 32)));
        return true;
    }

    @Override
    public void onClose() {
        if (settings != null) { closeSettings(); return; }
        if (search != null && search.isFocused()) {
            search.setFocused(false); setFocused(null); return;
        }
        ConfigManager.save();
        super.onClose();
    }

    @Override
    public void removed() {
        if (settings != null) settings.release();
        ConfigManager.save();
        super.removed();
    }

    static void clip(GuiGraphics g, GuiLayout layout, int x1, int y1, int x2, int y2) {
        // Supply screen coordinates under an identity pose, then restore the drawing pose.
        g.pose().pushMatrix();
        g.pose().identity();
        g.enableScissor((int) Math.floor(layout.screenX(x1)), (int) Math.floor(layout.screenY(y1)),
                (int) Math.ceil(layout.screenX(x2)), (int) Math.ceil(layout.screenY(y2)));
        g.pose().popMatrix();
    }

    static int alpha(int color, int alpha) { return (Math.max(0, Math.min(255, alpha)) << 24) | (color & 0xFFFFFF); }
    static int mix(int a, int b, float amount) {
        float t = Math.max(0, Math.min(1, amount));
        int result = 0;
        for (int shift : new int[]{24, 16, 8, 0}) {
            int value = Math.round(((a >>> shift) & 255) * (1 - t) + ((b >>> shift) & 255) * t);
            result |= value << shift;
        }
        return result;
    }
}
