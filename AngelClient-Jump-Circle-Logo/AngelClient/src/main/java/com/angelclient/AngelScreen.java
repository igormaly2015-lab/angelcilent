
package com.angelclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public final class AngelScreen extends Screen {

    private static final int PANEL_WIDTH = 560;
    private static final int PANEL_HEIGHT = 350;

    private static final String[] TABS = {
        "HOME", "CHEATS", "VISUALS",
        "COSMETICS", "CUSTOM GUI", "CONFIG"
    };

    private static final String[] CHEATS = {
        "Kill Aura (prototyp UI)",
        "FakeLag (prototyp UI)",
        "Reach (prototyp UI)",
        "Velocity (prototyp UI)",
        "Auto Clicker (prototyp UI)",
        "Aim Assist (prototyp UI)",
        "Trigger Bot (prototyp UI)",
        "Auto Trap (prototyp UI)"
    };

    private static final String[] VISUALS = {
        "Ring Visual", "Jump Circle", "Target Ring",
        "Angel Aura", "Golden Wings", "Heaven Particles",
        "Halo Visual", "Enchanted Sword", "Target Highlight",
        "China Hat", "Glass Vapor", "Tracers", "Nametags",
        "Player Glow", "Motion Blur (preview)", "Hit Color",
        "Damage Tint", "Keystrokes HUD", "CPS Counter",
        "Armor HUD", "Potion HUD", "Crosshair Customizer",
        "Low Fire", "Item Physics (preview)"
    };

    private final Map<String, Boolean> toggles = new LinkedHashMap<>();
    private final Map<String, String> settings = new LinkedHashMap<>();

    private int selectedTab = 0;
    private int page = 0;
    private int backgroundColor = 0xFF171725;
    private String status = "Angel Client gotowy";

    public AngelScreen() {
        super(Text.literal("Angel Client"));

        for (String name : CHEATS) toggles.put(name, false);
        for (String name : VISUALS) toggles.put(name, false);
        toggles.put("Golden Wings", false);
        toggles.put("White Armor", false);
        toggles.put("Halo Cosmetic", false);
        toggles.put("Archangel Emblem", false);

        settings.put("Jump Circle.Color", "Gold");
        settings.put("Jump Circle.Size", "1.0");
        settings.put("Jump Circle.Animation", "Pulse");
        settings.put("Jump Circle.Logo", "ON");

        loadConfig();
    }

    private Path configPath() {
        return MinecraftClient.getInstance().runDirectory.toPath()
            .resolve("config")
            .resolve("angelclient.properties");
    }

    private void saveConfig() {
        Properties properties = new Properties();
        properties.setProperty("background", Integer.toString(backgroundColor));

        toggles.forEach((key, value) ->
            properties.setProperty("toggle." + key, Boolean.toString(value)));

        settings.forEach((key, value) ->
            properties.setProperty("setting." + key, value));

        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());

            try (OutputStream out = Files.newOutputStream(path)) {
                properties.store(out, "Angel Client configuration");
            }

            status = "Konfiguracja zapisana";
        } catch (IOException e) {
            status = "Błąd zapisu konfiguracji";
        }
    }

    private void loadConfig() {
        Path path = configPath();
        if (!Files.exists(path)) return;

        Properties properties = new Properties();

        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);

            backgroundColor = Integer.parseInt(
                properties.getProperty("background",
                    Integer.toString(backgroundColor)));

            for (String key : toggles.keySet()) {
                toggles.put(key, Boolean.parseBoolean(
                    properties.getProperty("toggle." + key,
                        Boolean.toString(toggles.get(key)))));
            }

            for (String key : settings.keySet()) {
                settings.put(key,
                    properties.getProperty("setting." + key, settings.get(key)));
            }
        } catch (IOException | NumberFormatException e) {
            status = "Nie udało się wczytać konfiguracji";
        }
    }

    @Override
    protected void init() {
        clearChildren();

        int left = Math.max(8, (width - PANEL_WIDTH) / 2);
        int top = Math.max(8, (height - PANEL_HEIGHT) / 2);

        for (int i = 0; i < TABS.length; i++) {
            final int tab = i;

            addDrawableChild(ButtonWidget.builder(
                Text.literal((selectedTab == i ? "> " : "") + TABS[i]),
                button -> {
                    selectedTab = tab;
                    page = 0;
                    init();
                }
            ).dimensions(left + 10, top + 42 + i * 36, 140, 29).build());
        }

        int x = left + 170;
        int y = top + 58;

        switch (selectedTab) {
            case 0 -> {
                addButton("Otwórz CHEATS", x, y, () -> selectTab(1));
                addButton("Otwórz VISUALS", x + 175, y, () -> selectTab(2));
                addButton("Otwórz COSMETICS", x, y + 42, () -> selectTab(3));
                addButton("Otwórz CUSTOM GUI", x + 175, y + 42, () -> selectTab(4));
                addButton("Otwórz CONFIG", x, y + 84, () -> selectTab(5));
            }
            case 1 -> addTogglePage(CHEATS, x, y);
            case 2 -> addTogglePage(VISUALS, x, y);
            case 3 -> addTogglePage(new String[] {
                "Golden Wings", "White Armor",
                "Halo Cosmetic", "Archangel Emblem"
            }, x, y);
            case 4 -> {
                addButton("Zmień kolor tła", x, y, () -> {
                    backgroundColor = backgroundColor == 0xFF171725
                        ? 0xFFEEEEEE : 0xFF171725;
                    saveConfig();
                    init();
                });
            }
            case 5 -> {
                addButton("Zapisz konfigurację", x, y, this::saveConfig);
                addButton("Wczytaj konfigurację", x, y + 40, () -> {
                    loadConfig();
                    status = "Konfiguracja wczytana";
                    init();
                });
                addButton("Przywróć domyślne", x, y + 80, () -> {
                    toggles.replaceAll((key, value) -> false);
                    backgroundColor = 0xFF171725;
                    settings.put("Jump Circle.Color", "Gold");
                    settings.put("Jump Circle.Size", "1.0");
                    settings.put("Jump Circle.Animation", "Pulse");
                    settings.put("Jump Circle.Logo", "ON");
                    saveConfig();
                    init();
                });
            }
        }

        addButton("Zamknij", left + 445, top + 305, this::close);
    }

    private void addTogglePage(String[] items, int x, int y) {
        int pageSize = 8;
        int pageCount = Math.max(1, (items.length + pageSize - 1) / pageSize);
        page = Math.min(page, pageCount - 1);

        int start = page * pageSize;
        int end = Math.min(items.length, start + pageSize);

        for (int i = start; i < end; i++) {
            String name = items[i];

            int col = (i - start) % 2;
            int row = (i - start) / 2;

            addButton(name + (toggles.getOrDefault(name, false) ? " : ON" : " : OFF"),
                x + col * 175, y + row * 36, () -> {
                    if (name.equals("Jump Circle")) {
                        toggleSetting("Jump Circle.Logo", "ON", "OFF");
                        status = "Jump Circle: ustawienia zapisane";
                        showJumpCircleSettings();
                    } else {
                        toggles.put(name, !toggles.getOrDefault(name, false));
                        status = name + " — " +
                            (toggles.get(name) ? "ON" : "OFF");
                        saveConfig();
                        init();
                    }
                });
        }

        if (pageCount > 1) {
            addButton("Poprzednia", x, y + 160,
                () -> { page = Math.max(0, page - 1); init(); });

            addButton("Następna", x + 175, y + 160,
                () -> { page = Math.min(pageCount - 1, page + 1); init(); });
        }
    }

    private void showJumpCircleSettings() {
        int oldTab = selectedTab;
        selectedTab = 2;
        init();
        selectedTab = oldTab;
    }

    private void toggleSetting(String key, String a, String b) {
        settings.put(key, settings.getOrDefault(key, a).equals(a) ? b : a);
        saveConfig();
    }

    private void addButton(String label, int x, int y, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label),
            button -> action.run()
        ).dimensions(x, y, 165, 29).build());
    }

    private void selectTab(int tab) {
        selectedTab = tab;
        page = 0;
        init();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int left = Math.max(8, (width - PANEL_WIDTH) / 2);
        int top = Math.max(8, (height - PANEL_HEIGHT) / 2);

        context.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT,
            backgroundColor);
        context.fill(left, top, left + 155, top + PANEL_HEIGHT, 0xFF25253A);
        context.fill(left, top, left + PANEL_WIDTH, top + 34, 0xFF30304A);

        context.drawText(textRenderer, "ANGEL CLIENT",
            left + 12, top + 12, 0xFFFFD76A, false);

        context.drawText(textRenderer, TABS[selectedTab],
            left + 170, top + 15, 0xFFFFFFFF, false);

        context.drawText(textRenderer, status,
            left + 170, top + 285, 0xFFFFD76A, false);

        if (selectedTab == 2) {
            context.drawText(textRenderer,
                "Jump Circle: " + settings.get("Jump Circle.Color")
                    + " | Size: " + settings.get("Jump Circle.Size")
                    + " | Animation: " + settings.get("Jump Circle.Animation"),
                left + 170, top + 250, 0xFFDDDDDD, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
