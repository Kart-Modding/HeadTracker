plugins {
    id("net.neoforged.moddev") version "2.0.147"
    id("neoforge-mutex")
    id("me.modmuss50.mod-publish-plugin")
}

// DO NOT set group = ...!
version = "${property("mod.version")}+${sc.current.version}"
base.archivesName = "${property("mod.id") as String}-neoforge"

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    sc.current.parsed >= "1.18" -> JavaVersion.VERSION_17
    sc.current.parsed >= "1.17" -> JavaVersion.VERSION_16
    else -> JavaVersion.VERSION_1_8
}

// Releases marked compatible on Modrinth and CurseForge
val compatibleVersions: List<String> = sc.properties.rawOrNull("mod", "mc_releases")
    ?.asList().orEmpty().map { it.toString() }

neoForge {
    version = sc.properties.get<String>("deps.neoforge")

    mods {
        register(property("mod.id") as String) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = rootProject.file("run") // Shares the run directory between versions
            client()
            jvmArguments.add("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        }
    }
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

        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("fabric.mod.json", "*.ct", "META-INF/mods.toml")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds mod jars and copies results to `build/libs/{mod version}/`"

        inputs.property("version", project.property("mod.version"))
        from(jar.flatMap { it.archiveFile }, named<Jar>("sourcesJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod.version")}"))
    }
}

// Uploads this node's jar to Modrinth and CurseForge.
// CI runs it when mod.version changes on master. Without MODRINTH_TOKEN it is a dry run, so a local
// `./gradlew publishMods` only logs what it would upload.
publishMods {
    val modrinthId = findProperty("publish.modrinth")?.toString().orEmpty()
    val curseforgeId = findProperty("publish.curseforge")?.toString().orEmpty()

    file = tasks.jar.flatMap { it.archiveFile }
    displayName = "${property("mod.name")} ${project.version} (NeoForge)"
    version = project.version.toString()
    changelog = providers.environmentVariable("PUBLISH_CHANGELOG").orElse("")
    type = STABLE
    modLoaders.addAll("neoforge")
    dryRun = providers.environmentVariable("MODRINTH_TOKEN").orNull == null

    if (modrinthId.isNotEmpty()) modrinth {
        projectId = modrinthId
        accessToken = providers.environmentVariable("MODRINTH_TOKEN")
        minecraftVersions.addAll(compatibleVersions)
        optional("xaeros-minimap")
    }

    if (curseforgeId.isNotEmpty()) curseforge {
        projectId = curseforgeId
        accessToken = providers.environmentVariable("CURSEFORGE_TOKEN")
        minecraftVersions.addAll(compatibleVersions)
        javaVersions.add(requiredJava)
        clientRequired = true
        serverRequired = false
        optional("xaeros-minimap")
    }
}
