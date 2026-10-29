
package com.angelclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.LinkedHashMap;
import java.util.Map;

public class AngelScreen extends Screen {
    private static final String[] TABS = {
        "HOME", "CHEATS", "VISUALS",
        "COSMETICS", "CUSTOM GUI", "CONFIG"
    };

    private int selectedTab = 0;
    private int circleColor = 0xFF8B5CF6;
    private int circleSize = 80;
    private boolean circleEnabled = true;
    private boolean circleFilled = false;
    private boolean guiBlur = true;
    private String status = "Angel Client gotowy";

    private final Map<String, Boolean> toggles = new LinkedHashMap<>();

    public AngelScreen() {
        super(Text.literal("Angel Client"));

        for (String name : new String[] {
            "Sprint", "Fullbright", "Zoom", "Keystrokes",
            "FPS Counter", "Coordinates", "Armor HUD",
            "CPS Counter", "Crosshair", "Particles"
        }) {
            toggles.put(name, false);
        }
    }

    @Override
    protected void init() {
        rebuildButtons();
    }

    private void rebuildButtons() {
        clearChildren();

        int left = Math.max(10, (width - 560) / 2);
        int top = Math.max(10, (height - 350) / 2);

        for (int i = 0; i < TABS.length; i++) {
            final int tab = i;
            addDrawableChild(ButtonWidget.builder(
                Text.literal((selectedTab == i ? "> " : "") + TABS[i]),
                b -> {
                    selectedTab = tab;
                    rebuildButtons();
                }
            ).dimensions(left + 10, top + 45 + i * 39, 140, 30).build());
        }

        int x = left + 175;
        int y = top + 55;

        switch (selectedTab) {
            case 0 -> {
                addButton("CHEATS", x, y, () -> selectTab(1));
                addButton("VISUALS", x + 165, y, () -> selectTab(2));
                addButton("COSMETICS", x, y + 45, () -> selectTab(3));
                addButton("CUSTOM GUI", x + 165, y + 45, () -> selectTab(4));
                addButton("CONFIG", x, y + 90, () -> selectTab(5));
            }
            case 1 -> {
                int row = 0;
                for (String name : toggles.keySet()) {
                    final String key = name;
                    addButton(name + ": " +
                        (toggles.get(name) ? "ON" : "OFF"),
                        x + (row % 2) * 165,
                        y + (row / 2) * 38,
                        () -> {
                            toggles.put(key, !toggles.get(key));
                            status = key + " zmieniono";
                            rebuildButtons();
                        });
                    row++;
                }
            }
            case 2 -> {
                addButton("Jump Circle: " + (circleEnabled ? "ON" : "OFF"),
                    x, y, () -> {
                        circleEnabled = !circleEnabled;
                        status = "Jump Circle zmieniono";
                        rebuildButtons();
                    });

                addButton("Rozmiar: " + circleSize,
                    x, y + 42, () -> {
                        circleSize += 10;
                        if (circleSize > 150) circleSize = 30;
                        rebuildButtons();
                    });

                addButton("Kolor: " + colorName(),
                    x, y + 84, () -> {
                        circleColor = nextColor(circleColor);
                        rebuildButtons();
                    });

                addButton("Wypełnienie: " + (circleFilled ? "ON" : "OFF"),
                    x, y + 126, () -> {
                        circleFilled = !circleFilled;
                        rebuildButtons();
                    });
            }
            case 3 -> {
                addButton("Domyślny wygląd", x, y, () ->
                    status = "Wybrano wygląd domyślny");
                addButton("Fioletowy motyw", x, y + 42, () ->
                    status = "Wybrano motyw fioletowy");
                addButton("Złoty motyw", x, y + 84, () ->
                    status = "Wybrano motyw złoty");
            }
            case 4 -> {
                addButton("Rozmycie: " + (guiBlur ? "ON" : "OFF"),
                    x, y, () -> {
                        guiBlur = !guiBlur;
                        rebuildButtons();
                    });
                addButton("Reset wyglądu", x, y + 42, () ->
                    status = "Ustawienia wyglądu zresetowane");
            }
            case 5 -> {
                addButton("Zapisz konfigurację", x, y, () ->
                    status = "Ustawienia aktywne do zamknięcia gry");
                addButton("Przywróć domyślne", x, y + 42, () -> {
                    circleEnabled = true;
                    circleFilled = false;
                    circleSize = 80;
                    circleColor = 0xFF8B5CF6;
                    toggles.replaceAll((k, v) -> false);
                    status = "Przywrócono ustawienia";
                    rebuildButtons();
                });
            }
        }
    }

    private void addButton(String label, int x, int y, Runnable action) {
        addDrawableChild(ButtonWidget.builder(Text.literal(label),
            b -> action.run()
        ).dimensions(x, y, 155, 30).build());
    }

    private void selectTab(int tab) {
        selectedTab = tab;
        rebuildButtons();
    }

    private int nextColor(int color) {
        if (color == 0xFF8B5CF6) return 0xFFFFD76A;
        if (color == 0xFFFFD76A) return 0xFF45D6C5;
        return 0xFF8B5CF6;
    }

    private String colorName() {
        if (circleColor == 0xFFFFD76A) return "Złoty";
        if (circleColor == 0xFF45D6C5) return "Turkusowy";
        return "Fioletowy";
    }

    @Override
    public void render(DrawContext context, int mouseX,
                       int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int left = Math.max(10, (width - 560) / 2);
        int top = Math.max(10, (height - 350) / 2);

        context.fill(left, top, left + 560, top + 350, 0xFF171725);
        context.fill(left, top, left + 160, top + 350, 0xFF222235);
        context.fill(left, top, left + 560, top + 35, 0xFF30304A);

        context.drawText(textRenderer, "ANGEL CLIENT",
            left + 12, top + 12, 0xFFFFD76A, false);

        context.drawText(textRenderer, TABS[selectedTab],
            left + 175, top + 18, 0xFFFFFFFF, false);

        context.drawText(textRenderer, status,
            left + 175, top + 320, 0xFFFFD76A, false);

        if (selectedTab == 2) {
            context.drawText(textRenderer,
                "Jump Circle — ustawienia wyglądu",
                left + 175, top + 245, circleColor, false);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
