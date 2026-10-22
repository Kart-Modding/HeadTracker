![Head Locator](Promotional%20Images/Infographics/banner.png)

<p align="center">
  <img alt="Fabric" src="https://img.shields.io/badge/Fabric-supported-dbb585?style=for-the-badge">
  <img alt="Quilt" src="https://img.shields.io/badge/Quilt-supported-9b59ff?style=for-the-badge">
  <img alt="NeoForge" src="https://img.shields.io/badge/NeoForge-supported-f48a3c?style=for-the-badge">
  <img alt="Forge" src="https://img.shields.io/badge/Forge-1.21.10%2B-5b8cff?style=for-the-badge">
  <br>
  <img alt="Minecraft" src="https://img.shields.io/badge/Minecraft-1.21.6_→_26.3-62e021?style=for-the-badge">
  <img alt="Client-side" src="https://img.shields.io/badge/side-client-gray?style=for-the-badge">
  <img alt="License" src="https://img.shields.io/badge/license-MIT-gray?style=for-the-badge">
</p>

**Head Locator** swaps the colored dots on the vanilla locator bar for the real heads of the players they track.

![How it works](Promotional%20Images/Infographics/how-it-works.png)
![Features](Promotional%20Images/Infographics/features.png)
![Border mode](Promotional%20Images/Infographics/border-mode.png)
![Compatibility](Promotional%20Images/Infographics/compatibility.png)

| Cardinal letters on | Heads only | Settings |
|:-:|:-:|:-:|
| ![](Promotional%20Images/Compressed/Photo1.png) | ![](Promotional%20Images/Compressed/Photo2.png) | ![](Promotional%20Images/Compressed/Photo3.png) |

## Install

Drop the jar for your loader into `mods/`. Nothing else is required.

| Loader | Settings screen |
|---|---|
| Fabric / Quilt | [Mod Menu](https://modrinth.com/mod/modmenu) (optional) |
| NeoForge / Forge | Mods list → *Config* |

Optional: [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap) waypoints show up in border mode.

## Pet tracking

With *Track Pets (Last Seen)* on, the client remembers where each of your tamed wolves, cats and parrots was last seen, per singleplayer world and per multiplayer server (`config/headtracker/pets/`). Border mode shows them as plates with the pet's initial. The server only sends entities near you, so this is the last spot *you* saw them - exact for sitting pets and pets in unloaded chunks, which don't move.

## Known limitations

- Only the 9×9 player dot is replaced; the self arrow and death markers stay vanilla.
- Heads need the player in your tab list; anything else keeps the vanilla icon.
- Border mode replaces the bar, so *Always Show Locator Bar* does nothing while it's on.
- Remembered pets only show in border mode, not on the vanilla locator bar.
- Pet tracking doesn't cover horses (their owner isn't sent to the client), skips Realms, and only knows what this computer has seen.

## Development

Multi-loader, multi-version via [Stonecutter](https://stonecutter.kikugie.dev/). One node per `<version>-<loader>` (see `settings.gradle.kts`), each built by `build.<loader>.gradle.kts`; per-version/loader pins live in `stonecutter.properties.toml`.

| Task | Result |
|---|---|
| `./gradlew chiseledBuild` | every jar → `build/libs/<mod version>/` |
| `./gradlew :1.21.11-neoforge:buildAndCollect` | one jar |
| *Set active project to …* | switch the version/loader the sources are written for |

Loader code is behind `//? if fabric` / `neoforge` / `forge` comments: `Platform` (config dir, mod checks), the `HeadTracker{Fabric,NeoForge,Forge}` entrypoints, and the border-mode hook in `HudMixin` (NeoForge/Forge split the vanilla HUD into layers). Quilt runs the Fabric jar. Forge starts at 1.21.10 because older Forge doesn't ship MixinExtras.

`XaeroWaypoints` reads Xaero's Minimap purely through reflection and switches itself off if anything fails to resolve.
