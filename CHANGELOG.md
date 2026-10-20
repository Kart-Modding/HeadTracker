# Changelog

## 0.4.0

- Added NeoForge support (1.21.6 - 26.3) and Forge support (1.21.10 - 26.3). The settings screen opens from the mods list on both.
- Quilt is supported through the Fabric build.
- Jars are now named per loader: `headtracker-<loader>-<version>+<minecraft>.jar`.

## 0.3.0 - 2026-08-16

- Added a border mode option: waypoints are projected onto the edges of the screen instead of the locator bar, which frees up the experience bar and tracks players through a full 360°. Modelled on [Locator Border](https://github.com/liqwtf/locator-border).
- Added options to label border-mode heads with the player's name and distance.
- Added sliders for the border inset and the border head size.
- The border rectangle uses the same inset on all four edges, so the bottom sits as far from the screen edge as the top and its heads are drawn over the hotbar.
- Cardinal direction letters follow the border when border mode is on, and sit flush against the bottom of the screen rather than above the hotbar.
- Border mode draws in its own stratum, so its heads and letters render on top of the hotbar and the rest of the HUD.
- Border mode marks waypoints behind the camera with a down arrow when there is no pitch arrow to show, and the name/distance labels now step around whichever side the arrow took.
- Border mode can show [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap) waypoints too, but only while they are off screen - once one is inside your field of view it is left to Xaero's own in-world marker. They are drawn the way Xaero draws them, as a solid plate in the waypoint's color with its initials in white on top. Read through reflection, so Xaero stays a purely optional soft dependency.

## 0.2.0 - 2026-07-26

- Added an option to show cardinal direction letters (N/E/S/W) on the locator bar.
- Added an option to force the locator bar to stay visible even with no tracked waypoints.
- Added a Mod Menu config screen to toggle the new options (requires Mod Menu; optional dependency).

## 0.1.0

- Initial release: replaces the colored dots on the locator bar with the tracked players' heads.
