plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "26.2.x"

// Builds every registered version's jar in one go, output lands in build/libs/<mod version>/
tasks.register("chiseledBuild") {
    group = "project"
    dependsOn(stonecutter.tasks.named("buildAndCollect").map { it.values })
}

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"${property("mod.version")}\";"
    swaps["minecraft"] = "\"${node.metadata.version}\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String

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
    }
}
