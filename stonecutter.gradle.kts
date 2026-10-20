plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.3.x-fabric"

// Builds every registered version's jar in one go, output lands in build/libs/<mod version>/
tasks.register("chiseledBuild") {
    group = "project"
    dependsOn(stonecutter.tasks.named("buildAndCollect").map { it.values })
}

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    // Makes version- and loader-specific sections of `stonecutter.properties.toml` apply
    properties {
        tags(version, loader)
    }

    // Adds loader constants to Stonecutter comments (i.e. `//? if fabric {...`)
    constants {
        match(loader, "fabric", "neoforge", "forge")
    }

    swaps["mod_version"] = "\"${properties.get<String>("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    constants["release"] = properties.get<String>("mod.id") != "template"
    dependencies["fapi"] = properties.getOrNull<String>("deps.fabric_api") ?: "0"

    replacements {
        string(current.parsed >= "1.21.11") {
            replace("ResourceLocation", "Identifier")
        }

        string(current.parsed >= "26.1") {
            replace("classTweaker v2 named", "classTweaker v2 official")
            // 26.1 split GuiGraphics into an immediate-draw and a render-state-extracting variant
            replace("GuiGraphics", "GuiGraphicsExtractor")
            replace("PlayerFaceRenderer", "PlayerFaceExtractor")
        }

        string(current.parsed >= "26.3") {
            // 26.3 moved RenderPipeline out of blaze3d into the renderpearl package
            replace("com.mojang.blaze3d.pipeline.RenderPipeline", "com.mojang.renderpearl.api.pipeline.RenderPipeline")
            replace("Lcom/mojang/blaze3d/pipeline/RenderPipeline;", "Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;")
        }
    }
}
