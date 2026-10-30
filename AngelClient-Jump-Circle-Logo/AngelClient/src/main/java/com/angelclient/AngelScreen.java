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
        "Kill Aura (UI)",
        "FakeLag (UI)",
        "Reach (UI)",
        "Velocity (UI)",
        "Auto Clicker (UI)",
        "Aim Assist (UI)",
        "Trigger Bot (UI)",
        "Auto Trap (UI)"
    };

    private static final String[] VISUALS = {
        "Ring Visual", "Jump Circle", "Target Ring",
        "Angel Aura", "Golden Wings", "Heaven Particles",
        "Halo Visual", "Enchanted Sword", "Target Highlight",
        "China Hat", "Glass Vapor", "Tracers", "Nametags",
        "Player Glow", "Motion Blur", "Hit Color",
        "Damage Tint", "Keystrokes HUD", "CPS Counter",
        "Armor HUD", "Potion HUD", "Custom Crosshair",
        "Low Fire", "Item Physics"
    };

    private final Map<String, Boolean> toggles = new LinkedHashMap<>();
    private final Map<String, String> settings = new LinkedHashMap<>();

    private int selectedTab = 0;
    private int page = 0;
    private int backgroundColor = 0xFF171725;

    private String status = "Angel Client gotowy";
    private boolean jumpCircleSettingsOpen = false;

    public AngelScreen() {
        super(Text.literal("Angel Client"));

        for (String name : CHEATS) {
            toggles.put(name, false);
        }

        for (String name : VISUALS) {
            toggles.put(name, false);
        }

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

        properties.setProperty(
            "background",
            Integer.toString(backgroundColor)
        );

        toggles.forEach((key, value) ->
            properties.setProperty(
                "toggle." + key,
                Boolean.toString(value)
            )
        );

        settings.forEach((key, value) ->
            properties.setProperty("setting." + key, value)
        );

        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());

            try (OutputStream out = Files.newOutputStream(path)) {
                properties.store(out, "Angel Client configuration");
            }

            status = "Konfiguracja zapisana";

        } catch (IOException e) {
            status = "Blad zapisu konfiguracji";
        }
    }

    private void loadConfig() {
        Path path = configPath();

        if (!Files.exists(path)) {
            return;
        }

        Properties properties = new Properties();

        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);

            backgroundColor = Integer.parseInt(
                properties.getProperty(
                    "background",
                    Integer.toString(backgroundColor)
                )
            );

            for (String key : toggles.keySet()) {
                toggles.put(
                    key,
                    Boolean.parseBoolean(
                        properties.getProperty(
                            "toggle." + key,
                            Boolean.toString(toggles.get(key))
                        )
                    )
                );
            }

            for (String key : settings.keySet()) {
                settings.put(
                    key,
                    properties.getProperty(
                        "setting." + key,
                        settings.get(key)
                    )
                );
            }

        } catch (IOException | NumberFormatException e) {
            status = "Blad konfiguracji";
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
                Text.literal(
                    (selectedTab == i ? "> " : "") + TABS[i]
                ),
                button -> selectTab(tab)
            ).dimensions(
                left + 10,
                top + 42 + i * 36,
                140,
                29
            ).build());
        }

        int x = left + 170;
        int y = top + 58;

        if (jumpCircleSettingsOpen) {
            addJumpCircleSettings(x, y);
        } else {
            switch (selectedTab) {

                case 0 -> {
                    addButton("Otworz CHEATS", x, y,
                        () -> selectTab(1));

                    addButton("Otworz VISUALS", x + 175, y,
                        () -> selectTab(2));

                    addButton("Otworz COSMETICS", x, y + 42,
                        () -> selectTab(3));

                    addButton("Otworz CUSTOM GUI", x + 175, y + 42,
                        () -> selectTab(4));

                    addButton("Otworz CONFIG", x, y + 84,
                        () -> selectTab(5));
                }

                case 1 -> addTogglePage(CHEATS, x, y);
                case 2 -> addTogglePage(VISUALS, x, y);

                case 3 -> addTogglePage(new String[] {
                    "Golden Wings",
                    "White Armor",
                    "Halo Cosmetic",
                    "Archangel Emblem"
                }, x, y);

                case 4 -> addButton(
                    "Zmien kolor tla",
                    x,
                    y,
                    () -> {
                        backgroundColor =
                            backgroundColor == 0xFF171725
                                ? 0xFFEEEEEE
                                : 0xFF171725;

                        saveConfig();
                        init();
                    }
                );

                case 5 -> {
                    addButton("Zapisz konfiguracje", x, y,
                        this::saveConfig);

                    addButton("Wczytaj konfiguracje", x, y + 40, () -> {
                        loadConfig();
                        status = "Konfiguracja wczytana";
                        init();
                    });

                    addButton("Przywroc domyslne", x, y + 80, () -> {
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
        }

        addButton(
            jumpCircleSettingsOpen ? "Powrot" : "Zamknij",
            left + 445,
            top + 305,
            () -> {
                if (jumpCircleSettingsOpen) {
                    jumpCircleSettingsOpen = false;
                    init();
                } else {
                    close();
                }
            }
        );
    }

    private void addTogglePage(String[] items, int x, int y) {
        int pageSize = 8;

        int pageCount = Math.max(
            1,
            (items.length + pageSize - 1) / pageSize
        );

        page = Math.min(page, pageCount - 1);

        int start = page * pageSize;
        int end = Math.min(items.length, start + pageSize);

        for (int i = start; i < end; i++) {
            String name = items[i];

            int col = (i - start) % 2;
            int row = (i - start) / 2;

            addButton(
                name + (
                    toggles.getOrDefault(name, false)
                        ? " : ON"
                        : " : OFF"
                ),
                x + col * 175,
                y + row * 36,
                () -> {
                    toggles.put(
                        name,
                        !toggles.getOrDefault(name, false)
                    );

                    if ("Jump Circle".equals(name)) {
                        jumpCircleSettingsOpen = true;
                        status = "Ustawienia Jump Circle";
                    } else {
                        status = name + " - "
                            + (toggles.get(name) ? "ON" : "OFF");
                    }

                    saveConfig();
                    init();
                }
            );
        }

        if (pageCount > 1) {
            addButton("Poprzednia", x, y + 160, () -> {
                page = Math.max(0, page - 1);
                init();
            });

            addButton("Nastepna", x + 175, y + 160, () -> {
                page = Math.min(pageCount - 1, page + 1);
                init();
            });
        }
    }

    private void addJumpCircleSettings(int x, int y) {
        addButton(
            "Kolor: " + settings.get("Jump Circle.Color"),
            x,
            y,
            () -> {
                String current = settings.get("Jump Circle.Color");

                settings.put(
                    "Jump Circle.Color",
                    "Gold".equals(current) ? "Blue"
                        : "Blue".equals(current) ? "White"
                        : "Gold"
                );

                saveConfig();
                init();
            }
        );

        addButton(
            "Rozmiar: " + settings.get("Jump Circle.Size"),
            x,
            y + 40,
            () -> {
                String current = settings.get("Jump Circle.Size");

                settings.put(
                    "Jump Circle.Size",
                    "1.0".equals(current) ? "1.5"
                        : "1.5".equals(current) ? "2.0"
                        : "1.0"
                );

                saveConfig();
                init();
            }
        );

        addButton(
            "Animacja: " + settings.get("Jump Circle.Animation"),
            x,
            y + 80,
            () -> {
                String current = settings.get("Jump Circle.Animation");

                settings.put(
                    "Jump Circle.Animation",
                    "Pulse".equals(current) ? "Rotate"
                        : "Rotate".equals(current) ? "None"
                        : "Pulse"
                );

                saveConfig();
                init();
            }
        );

        addButton(
            "Logo: " + settings.get("Jump Circle.Logo"),
            x,
            y + 120,
            () -> {
                toggleSetting("Jump Circle.Logo", "ON", "OFF");
                init();
            }
        );
    }

    private void toggleSetting(String key, String a, String b) {
        settings.put(
            key,
            settings.getOrDefault(key, a).equals(a) ? b : a
        );

        saveConfig();
    }

    private void addButton(
        String label,
        int x,
        int y,
        Runnable action
    ) {
        addDrawableChild(ButtonWidget.builder(
            Text.literal(label),
            button -> action.run()
        ).dimensions(x, y, 165, 29).build());
    }

    private void selectTab(int tab) {
        selectedTab = tab;
        page = 0;
        jumpCircleSettingsOpen = false;
        init();
    }

    @Override
    public void render(
        DrawContext context,
        int mouseX,
        int mouseY,
        float delta
    ) {
        renderBackground(context, mouseX, mouseY, delta);

        int left = Math.max(8, (width - PANEL_WIDTH) / 2);
        int top = Math.max(8, (height - PANEL_HEIGHT) / 2);

        context.fill(
            left, top,
            left + PANEL_WIDTH, top + PANEL_HEIGHT,
            backgroundColor
        );

        context.fill(
            left, top,
            left + 155, top + PANEL_HEIGHT,
            0xFF25253A
        );

        context.fill(
            left, top,
            left + PANEL_WIDTH, top + 34,
            0xFF30304A
        );

        super.render(context, mouseX, mouseY, delta);

        context.drawText(
            textRenderer,
            "ANGEL CLIENT",
            left + 12,
            top + 12,
            0xFFFFD76A,
            false
        );

        String heading = jumpCircleSettingsOpen
            ? "JUMP CIRCLE SETTINGS"
            : TABS[selectedTab];

        context.drawText(
            textRenderer,
            heading,
            left + 170,
            top + 15,
            0xFFFFFFFF,
            false
        );

        context.drawText(
            textRenderer,
            status,
            left + 170,
            top + 285,
            0xFFFFD76A,
            false
        );

        if (jumpCircleSettingsOpen) {
            context.drawText(
                textRenderer,
                "Kolor, rozmiar, animacja i logo",
                left + 170,
                top + 230,
                0xFFDDDDDD,
                false
            );
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
