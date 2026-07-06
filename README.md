# ArcPath

ArcPath is a **client-side** Fabric mod for Minecraft 26.1 that displays a trajectory arc when holding a throwable item. The arc shows the predicted flight path of the projectile and marks the landing point with a marker.

## Supported Throwables

- Ender Pearls
- Snowballs
- Eggs
- Tridents
- Arrows (via Bow & Crossbow)

## Requirements

| Dependency | Version |
|---|---|
| Fabric Loader | 0.19.2 or newer |
| Fabric API | 0.149.1+26.1.2 or newer |
| Cloth Config | 26.1.154+fabric |
| Mod Menu | 18.0.0-beta.1 or newer |

Cloth Config and Mod Menu are required at runtime; without them the game will crash on startup. All dependencies can be downloaded from [Modrinth](https://modrinth.com).

## Configuration

Open the in-game Mod Menu (press `Escape` > `Mods`) and select ArcPath. Click the **Configure** button to open the settings screen. Each throwable has configurable arc and target visuals and can be enabled or disabled individually.

Settings are saved to `.minecraft/config/arcpath.json` automatically.

## Building from Source

If you would like to build the code locally, please note this project requires a dependency that is not yet available on Maven for Minecraft 26.1 and must be downloaded manually:

- [Cloth Config 26.1.154+fabric](https://modrinth.com/mod/cloth-config/version/26.1.154+fabric)

Download the jar and place it in a `libs/` folder in the project root before building.

(Note that this will no longer be required once Cloth Config 26.1 is avaliable on Maven)

Build with `.\gradlew build`