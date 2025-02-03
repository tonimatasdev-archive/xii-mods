pluginManagement {
    repositories {
        gradlePluginPortal()
        maven(url = "https://maven.minecraftforge.net/")
        maven(url = "https://maven.architectury.dev/")
        maven(url = "https://maven.fabricmc.net/")
    }
}

rootProject.name = "xii-mods"

// Example Mod
include("example:common", "example:fabric", "example:neoforge")
