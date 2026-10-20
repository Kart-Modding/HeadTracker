pluginManagement {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "FabricMC" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.minecraftforge.net/") { name = "MinecraftForge" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // Check the latest version on https://stonecutter.kikugie.dev/blog/changes/0.9
    id("dev.kikugie.stonecutter") version "0.9.8"

    // Used for cross-compat for 26.1+ and older versions (https://codeberg.org/KikuGie/loom-back-compat)
    id("dev.kikugie.loom-back-compat") version "0.4.2"

    // Sometimes it is needed to make Gradle run at all, so it doesn't hurt to have
    // (https://github.com/gradle/foojay-toolchains)
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        /**
         * Creates one node per loader, named `{project}-{loader}`, each built by `build.{loader}.gradle.kts`.
         * [version] is the Minecraft version Stonecutter compares against in `//? if` comments.
         *
         * Quilt has no node of its own - Quilt Loader runs the Fabric jar as is.
         */
        fun match(project: String, vararg loaders: String, version: String = project) {
            for (loader in loaders) version("$project-$loader", version).buildscript("build.$loader.gradle.kts")
        }

        // See https://stonecutter.kikugie.dev/wiki/start/#choosing-minecraft-versions
        // Forge only bundles MixinExtras from 1.21.10 on, so older versions are Fabric/NeoForge only.
        for (it in listOf("1.21.6", "1.21.7", "1.21.8", "1.21.9")) match(it, "fabric", "neoforge")
        for (it in listOf("1.21.10", "1.21.11")) match(it, "fabric", "neoforge", "forge")
        match("26.1.x", "fabric", "neoforge", "forge", version = "26.1")
        match("26.2.x", "fabric", "neoforge", "forge", version = "26.2")
        match("26.3.x", "fabric", "neoforge", "forge", version = "26.3")
        vcsVersion = "26.3.x-fabric"
    }
}

rootProject.name = "HeadTracker"
