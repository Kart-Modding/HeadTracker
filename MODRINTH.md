# HeadTracker

Replaces the plain colored dots on the vanilla locator bar with the actual heads of the players they represent.

Instead of squinting at anonymous colored squares to figure out who's who, HeadTracker renders each tracked player's real skin face behind a colored square — so you get the color-coded position *and* the identity at a glance.

## Features
- Renders player skin faces on the locator bar instead of vanilla dots
- Keeps the original dot's color as a background square, so existing color-coding (team, etc.) still works
- Optional cardinal direction letters (N/E/S/W) on the locator bar, aligned with the waypoint dots
- Optional setting to keep the locator bar visible even when no waypoints are being tracked
- [Mod Menu](https://modrinth.com/mod/modmenu) integration for both settings (optional — works fine without it)
- Client-side only — safe to use on any server, vanilla or modded

## Compatibility
Built with [Stonecutter](https://stonecutter.kikugie.dev/) for multiversion support across:

`1.21.6` · `1.21.7` · `1.21.8` · `1.21.9` · `1.21.10` · `1.21.11` · `26.1` · `26.2`

Requires **Fabric API**. Mod Menu is optional, only needed for the in-game settings screen.

## Known limitations
- Only the 9x9 player-dot icon is replaced — the self-arrow and off-screen death-marker icons on the locator bar are untouched.
- Only players currently in your tab list get a head rendered; other waypoint types fall back to the vanilla dot.
