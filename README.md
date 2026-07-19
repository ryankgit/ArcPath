# ArcPath

ArcPath is a client-side Fabric mod for Minecraft 26.1 that displays a trajectory arc when holding a throwable item. The arc shows the predicted flight path of the projectile and marks the landing point with a marker.

![Example ArcPath screenshot](docs/images/2026-07-05_21.33.42.png)

## Features
- Trajectory arc with configurable style per throwable item
- Gradient color support - blend from a start color to a second color along the arc
- Landing target marker with configurable shape per throwable
- Calculated trajectory accounts for player velocity
- Entity hit detection - arc stops at living entities and the target marker appears on them
- Arc toggle keybind (default: Y) to show/hide all arcs without opening the config screen
- Debug overlay (default: U) showing previously calculated trajectories

## Supported Throwables
- Ender Pearls
- Snowballs
- Eggs
- Tridents
- Arrows (via Bow & Crossbow)

## Configuration
Open Options → Mods → ArcPath → Configure to access the config screen. Each throwable has its own sub-category with independent Arc and Target settings.

### Arc Settings (per throwable)
- Enable/disable
- Color
- Line width
- Style (Dashed / Solid)
- Dash length and gap length (Dashed only)
- Transparency
- Gradient color

### Target Settings (per throwable)
- Enable/disable
- Color
- Line width
- Shape (Circle / Diamond / Square)
- Radius
- Transparency

## Keybinds
| Keybind | Default | Description |
|---|---|---|
| Toggle Arc | Y | Show/hide all arc/marker rendering |
| Toggle Debug | U | Show/hide the debug trajectory overlay |

## Requirements

| Dependency | Version |
|---|---|
| Fabric Loader | 0.19.2 or newer |
| Fabric API | 0.149.1+26.1.2 or newer |
| Cloth Config | 26.1.154+fabric |
| Mod Menu | 18.0.0-beta.1 or newer |

Cloth Config and Mod Menu are required at runtime; without them the game will crash on startup. All dependencies can be downloaded from [Modrinth](https://modrinth.com).

## Building from Source

If you would like to build the code locally, please note this project requires a dependency that is not yet available on Maven for Minecraft 26.1 and must be downloaded manually:

- [Cloth Config 26.1.154+fabric](https://modrinth.com/mod/cloth-config/version/26.1.154+fabric)

Download the jar and place it in a `libs/` folder in the project root before building.

(Note that this will no longer be required once Cloth Config 26.1 is avaliable on Maven)

Build with `.\gradlew build`
