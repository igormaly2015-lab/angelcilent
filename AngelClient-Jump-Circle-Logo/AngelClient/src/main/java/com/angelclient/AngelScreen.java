package com.angelclient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class AngelScreen extends Screen {
    private final List<String> tabs = List.of("HOME", "CHEATS", "VISUALS", "COSMETICS", "CUSTOM GUI", "CONFIG");
    private int selectedTab = 0;
    private int togglePage = 0;
    private int background = 0xFF111111;
    private final Map<String, Boolean> enabled = new HashMap<>();
    private String spotifyStatus = "Spotify: niepołączono";
    private String currentTrack = "Połącz Spotify, aby wyświetlić utwór";
    private String statusMessage = "Gotowy";
    private String selectedCheat = null;
    private String selectedVisual = null;
    private final Map<String, Map<String, String>> cheatSettings = new HashMap<>();

    public AngelScreen() {
        super(Text.literal("Angel Client"));
        loadConfig(false);
    }

    private Path configPath() {
        return MinecraftClient.getInstance().runDirectory.toPath().resolve("config").resolve("angelclient.properties");
    }

    private void saveConfig(boolean showMessage) {
        Properties properties = new Properties();
        properties.setProperty("background", Integer.toString(background));
        for (Map.Entry<String, Map<String, String>> cheat : cheatSettings.entrySet()) {
            for (Map.Entry<String, String> setting : cheat.getValue().entrySet()) {
                String key = Base64.getUrlEncoder().withoutPadding().encodeToString((cheat.getKey() + "::" + setting.getKey()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                properties.setProperty("setting." + key, setting.getValue());
            }
        }
        for (Map.Entry<String, Boolean> entry : enabled.entrySet()) {
            String key = Base64.getUrlEncoder().withoutPadding().encodeToString(entry.getKey().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            properties.setProperty("toggle." + key, Boolean.toString(entry.getValue()));
        }
        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) {
                properties.store(out, "Angel Client settings");
            }
            if (showMessage) statusMessage = "Zapisano: config/angelclient.properties";
        } catch (IOException e) {
            if (showMessage) statusMessage = "Błąd zapisu konfiguracji";
        }
    }

    private void loadConfig(boolean showMessage) {
        Path path = configPath();
        if (!Files.exists(path)) return;
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
            background = Integer.parseInt(properties.getProperty("background", Integer.toString(background)));
            enabled.clear();
            cheatSettings.clear();
            for (String key : properties.stringPropertyNames()) {
                if (key.startsWith("setting.")) {
                    try {
                        String decoded = new String(Base64.getUrlDecoder().decode(key.substring("setting.".length())), java.nio.charset.StandardCharsets.UTF_8);
                        int split = decoded.indexOf("::");
                        if (split > 0) cheatSettings.computeIfAbsent(decoded.substring(0, split), k -> new HashMap<>()).put(decoded.substring(split + 2), properties.getProperty(key));
                    } catch (IllegalArgumentException ignored) { }
                    continue;
                }
                if (!key.startsWith("toggle.")) continue;
                try {
                    String label = new String(Base64.getUrlDecoder().decode(key.substring("toggle.".length())), java.nio.charset.StandardCharsets.UTF_8);
                    enabled.put(label, Boolean.parseBoolean(properties.getProperty(key)));
                } catch (IllegalArgumentException ignored) { }
            }
            if (showMessage) statusMessage = "Konfiguracja wczytana";
        } catch (IOException | NumberFormatException e) {
            if (showMessage) statusMessage = "Nie udało się wczytać konfiguracji";
        }
    }

    @Override
    protected void init() {
        clearChildren();
        int left = Math.max(8, (width - 560) / 2);
        int top = Math.max(8, (height - 330) / 2);
        for (int i = 0; i < tabs.size(); i++) {
            final int tab = i;
            addDrawableChild(ButtonWidget.builder(Text.literal(tabs.get(i)), b -> {
                selectedTab = tab;
                togglePage = 0;
                init();
            }).dimensions(left + 10, top + 38 + i * 34, 135, 27).build());
        }

        if (selectedCheat != null && selectedTab == 1) {
            addCheatSettings(left, top);
        } else if (selectedTab == 0) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Połącz Spotify (wymaga integracji)"), b -> {
                spotifyStatus = "Spotify: integracja nie skonfigurowana";
                currentTrack = "Wymagane logowanie/API Spotify";
            }).dimensions(left + 170, top + 62, 350, 27).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("⏮ Poprzedni"), b -> setSpotifyMessage("Sterowanie Spotify wymaga połączenia API")).dimensions(left + 170, top + 101, 108, 26).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("▶ / ⏸ Odtwórz / pauza"), b -> setSpotifyMessage("Sterowanie Spotify wymaga połączenia API")).dimensions(left + 284, top + 101, 145, 26).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Następny ⏭"), b -> setSpotifyMessage("Sterowanie Spotify wymaga połączenia API")).dimensions(left + 435, top + 101, 85, 26).build());
        } else if (selectedTab == 1) {
            addToggles(left, top, new String[]{"Kill Aura (prototyp UI)", "FakeLag (prototyp UI)", "Reach (prototyp UI)", "Velocity (prototyp UI)", "Auto Clicker (prototyp UI)", "Aim Assist (prototyp UI)", "Trigger Bot (prototyp UI)", "Auto Trap (prototyp UI)"});
        } else if (selectedTab == 2 && selectedVisual != null) {
            addJumpCircleSettings(left, top);
        } else if (selectedTab == 2) {
            addToggles(left, top, new String[]{"Ring Visual", "Jump Circle", "Target Ring", "Angel Aura", "Golden Wings", "Heaven Particles", "Halo Visual", "Enchanted Sword", "Target Highlight", "China Hat", "Glass Vapor", "Tracers", "Nametags", "Player Glow", "Motion Blur (preview)", "Hit Color", "Damage Tint", "Keystrokes HUD", "CPS Counter", "Armor HUD", "Potion HUD", "Crosshair Customizer", "Low Fire", "Item Physics (preview)"});
        } else if (selectedTab == 3) {
            addToggles(left, top, new String[]{"Golden Wings", "White Armor", "Halo Cosmetic", "Archangel Emblem"});
        } else if (selectedTab == 4) {
            addDrawableChild(ButtonWidget.builder(Text.literal("Zmień kolor tła"), b -> {
                background = background == 0xFF111111 ? 0xFFEEEEEE : 0xFF111111;
                saveConfig(false);
                statusMessage = "Zmieniono kolor tła";
            }).dimensions(left + 170, top + 62, 300, 27).build());
        } else {
            addDrawableChild(ButtonWidget.builder(Text.literal("Zapisz konfigurację"), b -> saveConfig(true)).dimensions(left + 170, top + 62, 300, 27).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Wczytaj konfigurację"), b -> { loadConfig(true); init(); }).dimensions(left + 170, top + 100, 300, 27).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Kopiuj konfigurację"), b -> copyConfig()).dimensions(left + 170, top + 138, 300, 27).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("Przywróć domyślne"), b -> {
                enabled.clear();
                background = 0xFF111111;
                saveConfig(true);
                statusMessage = "Przywrócono ustawienia domyślne";
                init();
            }).dimensions(left + 170, top + 176, 300, 27).build());
        }
        addDrawableChild(ButtonWidget.builder(Text.literal("Zamknij"), b -> close()).dimensions(left + 445, top + 290, 75, 24).build());
    }

    private void addCheatSettings(int left, int top) {
        Map<String, String> settings = cheatSettings.computeIfAbsent(selectedCheat, k -> {
            Map<String, String> defaults = new HashMap<>();
            defaults.put("Range", "3.0");
            defaults.put("Keybind", "NONE");
            defaults.put("Mode", "Default");
            defaults.put("Speed", "10");
            defaults.put("Target", "Players");
            return defaults;
        });
        addDrawableChild(ButtonWidget.builder(Text.literal("Range: " + settings.get("Range") + "  [-]"), b -> {
            double v = Math.max(1.0, Double.parseDouble(settings.get("Range")) - 0.5);
            settings.put("Range", String.format(java.util.Locale.ROOT, "%.1f", v)); saveConfig(false); init();
        }).dimensions(left + 170, top + 60, 165, 27).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Range: " + settings.get("Range") + "  [+]"), b -> {
            double v = Math.min(10.0, Double.parseDouble(settings.get("Range")) + 0.5);
            settings.put("Range", String.format(java.util.Locale.ROOT, "%.1f", v)); saveConfig(false); init();
        }).dimensions(left + 342, top + 60, 165, 27).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Keybind: " + settings.get("Keybind")), b -> {
            String[] keys = {"NONE", "R", "G", "H", "J", "K", "V", "B"};
            int i = 0; for (int n = 0; n < keys.length; n++) if (keys[n].equals(settings.get("Keybind"))) i = n;
            settings.put("Keybind", keys[(i + 1) % keys.length]); saveConfig(false); init();
        }).dimensions(left + 170, top + 95, 165, 27).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Mode: " + settings.get("Mode")), b -> {
            String[] modes = {"Default", "Switch", "Legit", "Adaptive"};
            int i = 0; for (int n = 0; n < modes.length; n++) if (modes[n].equals(settings.get("Mode"))) i = n;
            settings.put("Mode", modes[(i + 1) % modes.length]); saveConfig(false); init();
        }).dimensions(left + 342, top + 95, 165, 27).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Speed / APS: " + settings.get("Speed")), b -> {
            int v = Integer.parseInt(settings.get("Speed")); settings.put("Speed", Integer.toString(v >= 20 ? 1 : v + 1)); saveConfig(false); init();
        }).dimensions(left + 170, top + 130, 165, 27).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Target: " + settings.get("Target")), b -> {
            String[] targets = {"Players", "Mobs", "All", "Friends off"};
            int i = 0; for (int n = 0; n < targets.length; n++) if (targets[n].equals(settings.get("Target"))) i = n;
            settings.put("Target", targets[(i + 1) % targets.length]); saveConfig(false); init();
        }).dimensions(left + 342, top + 130, 165, 27).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("← Wróć do CHEATS"), b -> { selectedCheat = null; init(); })
                .dimensions(left + 170, top + 180, 190, 27).build());
    }


    private void addJumpCircleSettings(int left, int top) {
        Map<String, String> settings = cheatSettings.computeIfAbsent("Jump Circle", k -> {
            Map<String, String> defaults = new HashMap<>();
            defaults.put("Logo in circle", "ON");
            defaults.put("Color", "Gold");
            defaults.put("Size", "1.0");
            defaults.put("Animation", "Pulse");
            return defaults;
        });
        addDrawableChild(ButtonWidget.builder(Text.literal("Logo in circle: " + settings.get("Logo in circle")), b -> {
            settings.put("Logo in circle", settings.get("Logo in circle").equals("ON") ? "OFF" : "ON"); saveConfig(false); init();
        }).dimensions(left + 170, top + 60, 340, 27).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Color: " + settings.get("Color")), b -> {
            String[] values = {"Gold", "White", "Angel blue"}; int i = 0;
            for (int n = 0; n < values.length; n++) if (values[n].equals(settings.get("Color"))) i = n;
            settings.put("Color", values[(i + 1) % values.length]); saveConfig(false); init();
        }).dimensions(left + 170, top + 95, 165, 27).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Size: " + settings.get("Size")), b -> {
            double v = Double.parseDouble(settings.get("Size")); v = v >= 2.0 ? 0.5 : v + 0.5;
            settings.put("Size", String.format(java.util.Locale.ROOT, "%.1f", v)); saveConfig(false); init();
        }).dimensions(left + 342, top + 95, 165, 27).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("Animation: " + settings.get("Animation")), b -> {
            String[] values = {"Pulse", "Rotate", "Fade"}; int i = 0;
            for (int n = 0; n < values.length; n++) if (values[n].equals(settings.get("Animation"))) i = n;
            settings.put("Animation", values[(i + 1) % values.length]); saveConfig(false); init();
        }).dimensions(left + 170, top + 130, 337, 27).build());
        addDrawableChild(ButtonWidget.builder(Text.literal("← Wróć do VISUALS"), b -> { selectedVisual = null; init(); })
                .dimensions(left + 170, top + 180, 190, 27).build());
    }

    private void addToggles(int left, int top, String[] labels) {
        int columns = 2;
        int pageSize = 8;
        int pageCount = Math.max(1, (labels.length + pageSize - 1) / pageSize);
        togglePage = Math.min(togglePage, pageCount - 1);
        int start = togglePage * pageSize;
        int end = Math.min(labels.length, start + pageSize);
        for (int index = start; index < end; index++) {
            final String label = labels[index];
            boolean on = enabled.getOrDefault(label, false);
            int local = index - start;
            int col = local % columns;
            int row = local / columns;
            addDrawableChild(ButtonWidget.builder(Text.literal(label + (on ? " : ON" : " : OFF")), b -> {
                boolean next = !enabled.getOrDefault(label, false);
                enabled.put(label, next);
                b.setMessage(Text.literal(label + (next ? " : ON" : " : OFF")));
                saveConfig(false);
            }).dimensions(left + 170 + col * 178, top + 58 + row * 34, 170, 27).build());
        }
        if (pageCount > 1) {
            addDrawableChild(ButtonWidget.builder(Text.literal("◀"), b -> { togglePage = Math.max(0, togglePage - 1); init(); })
                    .dimensions(left + 170, top + 230, 48, 25).build());
            addDrawableChild(ButtonWidget.builder(Text.literal((togglePage + 1) + " / " + pageCount), b -> {})
                    .dimensions(left + 222, top + 230, 100, 25).build());
            addDrawableChild(ButtonWidget.builder(Text.literal("▶"), b -> { togglePage = Math.min(pageCount - 1, togglePage + 1); init(); })
                    .dimensions(left + 326, top + 230, 48, 25).build());
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (selectedTab == 1 && selectedCheat == null && click.button() == 1) {
            int left = Math.max(8, (width - 560) / 2);
            int top = Math.max(8, (height - 330) / 2);
            int pageSize = 8;
            int start = togglePage * pageSize;
            String[] labels = {"Kill Aura (prototyp UI)", "FakeLag (prototyp UI)", "Reach (prototyp UI)", "Velocity (prototyp UI)", "Auto Clicker (prototyp UI)", "Aim Assist (prototyp UI)", "Trigger Bot (prototyp UI)", "Auto Trap (prototyp UI)"};
            for (int index = start; index < Math.min(labels.length, start + pageSize); index++) {
                int local = index - start;
                int x = left + 170 + (local % 2) * 178;
                int y = top + 58 + (local / 2) * 34;
                if (click.x() >= x && click.x() <= x + 170 && click.y() >= y && click.y() <= y + 27) {
                    selectedCheat = labels[index]; init(); return true;
                }
            }
        }
        if (selectedTab == 2 && selectedVisual == null && click.button() == 1) {
            int left = Math.max(8, (width - 560) / 2);
            int top = Math.max(8, (height - 330) / 2);
            int pageSize = 8;
            int start = togglePage * pageSize;
            String[] labels = {"Ring Visual", "Jump Circle", "Target Ring", "Angel Aura", "Golden Wings", "Heaven Particles", "Halo Visual", "Enchanted Sword", "Target Highlight", "China Hat", "Glass Vapor", "Tracers", "Nametags", "Player Glow", "Motion Blur (preview)", "Hit Color", "Damage Tint", "Keystrokes HUD", "CPS Counter", "Armor HUD", "Potion HUD", "Crosshair Customizer", "Low Fire", "Item Physics (preview)"};
            for (int index = start; index < Math.min(labels.length, start + pageSize); index++) {
                int local = index - start;
                int x = left + 170 + (local % 2) * 178;
                int y = top + 58 + (local / 2) * 34;
                if (click.x() >= x && click.x() <= x + 170 && click.y() >= y && click.y() <= y + 27 && labels[index].equals("Jump Circle")) {
                    selectedVisual = "Jump Circle"; init(); return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    private void setSpotifyMessage(String message) {
        spotifyStatus = message;
        currentTrack = "Brak połączenia Spotify";
        statusMessage = message;
    }

    private void copyConfig() {
        Properties properties = new Properties();
        properties.setProperty("background", Integer.toString(background));
        for (Map.Entry<String, Map<String, String>> cheat : cheatSettings.entrySet()) {
            for (Map.Entry<String, String> setting : cheat.getValue().entrySet()) {
                String key = Base64.getUrlEncoder().withoutPadding().encodeToString((cheat.getKey() + "::" + setting.getKey()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
                properties.setProperty("setting." + key, setting.getValue());
            }
        }
        for (Map.Entry<String, Boolean> entry : enabled.entrySet()) {
            String key = Base64.getUrlEncoder().withoutPadding().encodeToString(entry.getKey().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            properties.setProperty("toggle." + key, Boolean.toString(entry.getValue()));
        }
        try {
            StringWriter writer = new StringWriter();
            properties.store(writer, "Angel Client settings");
            MinecraftClient.getInstance().keyboard.setClipboard(writer.toString());
            statusMessage = "Konfigurację skopiowano do schowka";
        } catch (IOException e) {
            statusMessage = "Nie udało się skopiować konfiguracji";
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
         super.render(context, mouseX, mouseY, delta);
        int left = Math.max(8, (width - 560) / 2);
        int top = Math.max(8, (height - 330) / 2);
        context.fill(left, top, left + 560, top + 330, background);
        context.fill(left, top, left + 155, top + 330, 0xFFFFFFFF);
        context.drawText(textRenderer, "ANGEL CLIENT", left + 12, top + 14, 0xFF111111, false);
        context.drawText(textRenderer, selectedCheat != null ? selectedCheat + " — SETTINGS" : selectedVisual != null ? selectedVisual + " — SETTINGS" : tabs.get(selectedTab), left + 170, top + 20, 0xFFFFD76A, false);
        if (selectedTab == 1 && selectedCheat == null) context.drawText(textRenderer, "Lewy klik: ON/OFF  |  Prawy klik: ustawienia", left + 170, top + 282, 0xFFDDDDDD, false);
        if (selectedCheat != null && selectedTab == 1) context.drawText(textRenderer, "Panel ustawień UI — nie aktywuje automatyzacji", left + 170, top + 220, 0xFFAAAAAA, false);
        if (selectedVisual != null && selectedTab == 2) context.drawText(textRenderer, "Prawy klik na Jump Circle otwiera ustawienia. To konfiguracja UI.", left + 170, top + 220, 0xFFAAAAAA, false);
        context.drawText(textRenderer, statusMessage, left + 170, top + 310, 0xFFFFD76A, false);
        if (selectedTab == 0) {
            context.drawText(textRenderer, spotifyStatus, left + 170, top + 43, 0xFFFFFFFF, false);
            context.drawText(textRenderer, currentTrack, left + 170, top + 146, 0xFFDDDDDD, false);
            context.drawText(textRenderer, "Sterowanie działa po dodaniu autoryzacji Spotify API.", left + 170, top + 170, 0xFFAAAAAA, false);
        }
       
    }

    @Override
    public boolean shouldPause() { return false; }
}
