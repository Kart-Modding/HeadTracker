plugins {
    java
    id("net.minecraftforge.gradle") version "7.0.40"
}

// DO NOT set group = ...!
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-forge"

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    else -> JavaVersion.VERSION_21
}

// This can be used for publishing on Modrinth and Curseforge
val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

// `<minecraft>-<forge>`, see stonecutter.properties.toml
val forgeVersion: String = sc.properties["deps.forge"]
val mixinConfig = "${property("mod.id")}.mixins.json"

minecraft {
    // 26.1+ ships unobfuscated, older versions need Mojang's mappings applied
    if (sc.current.parsed < "26.1") mappings("official", forgeVersion.substringBefore('-'))

    runs {
        register("client") {
            workingDir.convention(rootProject.layout.projectDirectory.dir("run")) // Shares the run directory between versions
            // Mixin picks its config up from the jar manifest in production, but not in dev
            args("--mixin.config=$mixinConfig")
            systemProperty("mixin.debug.export", "true") // Exports transformed classes for debugging
        }
    }
}

repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    mavenCentral()
}

dependencies {
    implementation(minecraft.dependency("net.minecraftforge:forge:$forgeVersion"))
    // Forge bundles MixinExtras at runtime since 1.21.10, this is only needed to compile against it
    compileOnly("io.github.llamalad7:mixinextras-common:0.5.3")
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    processResources {
        fun MutableMap<String, String>.register(key: String, property: String) {
            val value: String = sc.properties[property]
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", "mod.id")
            register("name", "mod.name")
            register("version", "mod.version")
            register("minecraft", "mod.mc_compat")
        }

        filesMatching("META-INF/mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("fabric.mod.json", "*.ct", "META-INF/neoforge.mods.toml")
    }

    named<Jar>("jar") {
        // Forge's Mixin finds configs through the manifest rather than mods.toml
        manifest.attributes["MixinConfigs"] = mixinConfig
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(jar.flatMap { it.archiveFile }, named<Jar>("sourcesJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}
