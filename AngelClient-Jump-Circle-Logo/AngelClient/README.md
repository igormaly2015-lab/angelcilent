# Angel Client (Fabric)

Client-side Minecraft Java mod with a black/white/gold menu, right-shift keybind, visual/cosmetic option toggles, and local configuration storage.

## Current implementation status

- Menu and six tabs: implemented.
- Configuration save/load/reset: implemented. File: `.minecraft/config/angelclient.properties`.
- Copy configuration to clipboard: implemented.
- Visual/cosmetic options: currently UI toggles; they do not all render effects in-world yet.
- Combat automation options: deliberately UI-only prototypes; they do not automate combat or movement.
- Spotify controls: UI placeholders only; Spotify OAuth/API integration is not configured.

## Requirements

- Java 21
- Minecraft Java Edition version specified in `gradle.properties`
- Fabric Loader and Fabric API matching that Minecraft version
- Gradle 8.x compatible with the selected Fabric Loom version

## Build on Windows

1. Install Java 21 and Gradle (or open this project in IntelliJ IDEA with the Gradle plugin).
2. Open a terminal in this folder.
3. Run `gradle build`.
4. The mod JAR, if the build succeeds, will be in `build/libs/`.
5. Copy the non-`sources` JAR into `.minecraft/mods` and launch the matching Fabric profile.

This repository does not include a prebuilt JAR. Build and test it against the target Minecraft/Fabric versions before distributing.
