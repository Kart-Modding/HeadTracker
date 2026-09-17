package dev.karton.headtracker;

import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads the waypoints Xaero's Minimap is currently tracking.
 *
 * <p>Xaero's Minimap is closed source and ships one build per Minecraft version, so there is
 * nothing sane to compile against. Everything here goes through reflection instead: the class
 * and method names below are stable across every Minecraft version this mod supports (checked
 * against Xaero 25.2.7 for 1.21.6 and 26.4.2 for 1.21.11/26.x), and if any of them ever stops
 * resolving the integration switches itself off instead of breaking the HUD.
 */
public final class XaeroWaypoints {

    /** A snapshot of one Xaero waypoint, free of any reflected types. */
    public record Waypoint(int x, int y, int z, String name, String initials, int color) {
    }

    // Both the normal and the "fair play" build expose the same classes.
    private static final String[] MOD_IDS = {"xaerominimap", "xaerominimapfair"};

    private static boolean resolved;
    private static boolean available;

    private static Method getCurrentSession;
    private static Method getModuleSession;
    private static Object minimapModule;
    private static Constructor<?> collectorConstructor;
    private static Method collect;
    private static Method waypointGetX;
    private static Method waypointGetY;
    private static Method waypointGetZ;
    private static Method waypointGetName;
    private static Method waypointGetInitials;
    private static Method waypointIsDisabled;
    private static Method waypointGetColor;
    private static Method waypointIsEffectivelyDeleted;
    private static Method colorGetHex;

    // The collector is bound to one minimap session, so it only needs rebuilding on world change.
    private static Object cachedSession;
    private static Object cachedCollector;

    private XaeroWaypoints() {
    }

    public static boolean isAvailable() {
        resolve();
        return available;
    }

    public static List<Waypoint> collect() {
        resolve();
        if (!available) {
            return List.of();
        }

        try {
            Object session = getCurrentSession.invoke(null);
            if (session == null) {
                cachedSession = null;
                cachedCollector = null;
                return List.of();
            }

            Object minimapSession = getModuleSession.invoke(session, minimapModule);
            if (minimapSession == null) {
                cachedSession = null;
                cachedCollector = null;
                return List.of();
            }

            if (cachedCollector == null || cachedSession != minimapSession) {
                cachedCollector = collectorConstructor.newInstance(minimapSession);
                cachedSession = minimapSession;
            }

            List<Object> raw = new ArrayList<>();
            collect.invoke(cachedCollector, raw);
            if (raw.isEmpty()) {
                return List.of();
            }

            List<Waypoint> waypoints = new ArrayList<>(raw.size());
            for (Object entry : raw) {
                if (entry == null || (boolean) waypointIsDisabled.invoke(entry)) {
                    continue;
                }
                if (waypointIsEffectivelyDeleted != null && (boolean) waypointIsEffectivelyDeleted.invoke(entry)) {
                    continue;
                }

                Object color = waypointGetColor.invoke(entry);
                int hex = color != null ? (int) colorGetHex.invoke(color) : 0xFFFFFF;
                String initials = (String) waypointGetInitials.invoke(entry);
                String name = (String) waypointGetName.invoke(entry);

                waypoints.add(new Waypoint(
                    (int) waypointGetX.invoke(entry),
                    (int) waypointGetY.invoke(entry),
                    (int) waypointGetZ.invoke(entry),
                    name == null ? "" : name,
                    initials == null ? "" : initials,
                    0xFF000000 | hex
                ));
            }

            return waypoints;
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Something moved under us. Give up for the rest of the session rather than
            // throwing once per frame from inside the HUD render.
            available = false;
            cachedSession = null;
            cachedCollector = null;
            HeadTrackerMod.LOGGER.warn("Xaero's Minimap waypoint lookup failed, disabling the integration", e);
            return List.of();
        }
    }

    private static synchronized void resolve() {
        if (resolved) {
            return;
        }
        resolved = true;

        boolean loaded = false;
        for (String modId : MOD_IDS) {
            if (FabricLoader.getInstance().isModLoaded(modId)) {
                loaded = true;
                break;
            }
        }
        if (!loaded) {
            return;
        }

        try {
            getCurrentSession = Class.forName("xaero.common.XaeroMinimapSession").getMethod("getCurrentSession");

            Class<?> hudModule = Class.forName("xaero.hud.module.HudModule");
            getModuleSession = Class.forName("xaero.hud.HudSession").getMethod("getSession", hudModule);
            minimapModule = Class.forName("xaero.hud.minimap.BuiltInHudModules").getField("MINIMAP").get(null);

            Class<?> minimapSession = Class.forName("xaero.hud.minimap.module.MinimapSession");
            Class<?> collector = Class.forName("xaero.hud.minimap.waypoint.WaypointCollector");
            collectorConstructor = collector.getConstructor(minimapSession);
            collect = collector.getMethod("collect", List.class);

            Class<?> waypoint = Class.forName("xaero.common.minimap.waypoints.Waypoint");
            waypointGetX = waypoint.getMethod("getX");
            waypointGetY = waypoint.getMethod("getY");
            waypointGetZ = waypoint.getMethod("getZ");
            waypointGetName = waypoint.getMethod("getName");
            // getInitials() is the label Xaero itself paints on the marker: the waypoint's
            // symbol when one is set, derived from the name otherwise.
            waypointGetInitials = waypoint.getMethod("getInitials");
            waypointIsDisabled = waypoint.getMethod("isDisabled");
            waypointGetColor = waypoint.getMethod("getWaypointColor");
            // Only on newer builds, so it stays optional.
            waypointIsEffectivelyDeleted = optionalMethod(waypoint, "isEffectivelyDeleted");

            colorGetHex = Class.forName("xaero.hud.minimap.waypoint.WaypointColor").getMethod("getHex");

            available = true;
            HeadTrackerMod.LOGGER.info("Xaero's Minimap detected, border mode will show its waypoints");
        } catch (ReflectiveOperationException | RuntimeException e) {
            HeadTrackerMod.LOGGER.warn("Xaero's Minimap is present but its waypoint API didn't resolve", e);
        }
    }

    private static Method optionalMethod(Class<?> owner, String name) {
        try {
            return owner.getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }
}
