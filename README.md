# HeadTracker

Fabric mod. Replaces the colored player dots on the vanilla locator bar with the tracked players' actual heads (skin face + a colored square behind it, using the same color the vanilla dot would've had).

Built with [Stonecutter](https://stonecutter.kikugie.dev/) for multiversion source across Minecraft `1.21.6` -> `26.3`.

Client-side only. `dev/karton/headtracker/mixin/LocatorBarRendererMixin.java` wraps the locator bar's 9x9 dot sprite draw call and, when it can resolve the waypoint's player UUID to a connected `PlayerInfo`, draws that player's face texture instead.

`dev/karton/headtracker/XaeroWaypoints.java` is an optional bridge to [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap). It is closed source and ships one build per Minecraft version, so there is nothing to compile against - the whole chain (`XaeroMinimapSession` -> `BuiltInHudModules.MINIMAP` -> `WaypointCollector` -> `Waypoint`) goes through reflection, gated behind a `FabricLoader.isModLoaded` check and disabled for the session if anything fails to resolve. Those names were verified against Xaero 25.2.7 (1.21.6) and 26.4.2 (1.21.11 / 26.x).

Border mode (`dev/karton/headtracker/BorderRenderer.java`) is the alternative layout, modelled on [Locator Border](https://github.com/liqwtf/locator-border): the locator bar is kept out of the contextual bar slot entirely (so the experience bar stays visible) and the waypoints are projected onto a rectangle inset from the screen edges, giving a full 360° of tracking instead of the bar's ±60°. `HudMixin` drives it from the tail of the hotbar/decorations pass.

## Supported versions

`1.21.6`, `1.21.7`, `1.21.8`, `1.21.9`, `1.21.10`, `1.21.11`, `26.1`, `26.2`, `26.3`

Add more in `settings.gradle.kts` + matching section in `stonecutter.properties.toml` (needs `mod.mc_compat`, `mod.mc_releases`, `deps.fabric_api`).

## Usage

- Switch active dev version: Gradle task `"Set active project to ..."`, or `./gradlew :1.21.11:build` style per-subproject tasks.
- Build every version and collect all jars: `./gradlew chiseledBuild` -> `build/libs/<mod version>/` (one jar per Minecraft version).
- Build + collect just the active version's jar: `./gradlew buildAndCollect` -> `build/libs/<mod version>/`.
- Preprocessor comments (`//? if <condition> {` ... `//?}`) select code per Minecraft version when the active version changes. Read the [beginner's guide](https://stonecutter.kikugie.dev/wiki/start/) before touching `src/`.

## Known limitations

- Only replaces the 9x9 player-dot sprite. The self-arrow and off-screen death-marker icons on the locator bar are untouched.
- Only players currently in your tab list (`PlayerInfo` known to the client) get a head; other waypoint types (non-player) fall back to the vanilla dot (border mode draws their waypoint style sprite instead).
- Border mode replaces the locator bar rather than adding to it, so `forceLocatorBar` is ignored while it is on.
- Border mode insets the waypoint icons equally on all four edges, so heads on the bottom edge are drawn over the hotbar. The cardinal letters use their own, shallower bottom edge (`BorderRenderer.CARDINAL_BOTTOM_INSET`) that sits flush against the bottom of the screen.

## TODO

1. Replace `src/main/resources/assets/headtracker/icon.png` with a real icon.
2. Pick a license (currently CC0-1.0 placeholder from the template).
3. `deps.fabric_api` pins in `stonecutter.properties.toml` were resolved 2026-07-25, re-check if builds start failing on dependency resolution.

## Useful links

- [Stonecutter beginner's guide](https://stonecutter.kikugie.dev/wiki/start/)
- [Fabric Discord](https://discord.gg/v6v4pMv)
- [Stonecutter Discord](https://discord.kikugie.dev/)
