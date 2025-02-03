import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.task.RemapJarTask

plugins {
    java
    `maven-publish`
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.9-SNAPSHOT" apply false
    id("com.gradleup.shadow") version "9.+"
}

group = "dev.tonimatas"
version = "null"

val fabricLoaderVersion: String by extra
val minecraftVersion: String by extra
val neoforgeVersion: String by extra

repositories {
    mavenCentral()
}

dependencies {
}

subprojects {
    if (name == "neoforge") {
        extra["loom.platform"] = "neoforge"
    }
    
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")

    if (toString().split(":").size == 3) {
        apply(plugin = "dev.architectury.loom")
        apply(plugin = "com.gradleup.shadow")

        val modName = toString().split(":")[1]

        version = "${minecraftVersion}-${properties[modName]}"
        group = "dev.tonimatas.$modName"
        base.archivesName.set("$modName-${name}")
        
        configure<LoomGradleExtensionAPI> {
            silentMojangMappingsLicense()
        }

        dependencies {
            "minecraft"("com.mojang:minecraft:$minecraftVersion")
            "mappings"(project.the<LoomGradleExtensionAPI>().officialMojangMappings())
        }
        
        when (name) {
            "fabric" -> {
                architectury {
                    platformSetupLoomIde()
                    fabric()
                }

                dependencies {
                    "modImplementation"("net.fabricmc:fabric-loader:$fabricLoaderVersion")
                }
            }
            "neoforge" -> {
                architectury {
                    platformSetupLoomIde()
                    neoForge()
                }

                repositories {
                    maven(url = "https://maven.neoforged.net/")
                }

                dependencies {
                    "neoForge"("net.neoforged:neoforge:$neoforgeVersion")
                }
            }
            "common" -> {
                architectury {
                    common("fabric", "neoforge")
                }

                dependencies {
                    "modImplementation"("net.fabricmc:fabric-loader:$fabricLoaderVersion")
                }
            }
        }

        if (name == "fabric" || name == "neoforge") {
            val projectLoader = if (name == "neoforge") "NeoForge" else "Fabric"
            
            val common: Configuration by configurations.creating
            val shadowCommon: Configuration by configurations.creating

            configurations["compileClasspath"].extendsFrom(common)
            configurations["runtimeClasspath"].extendsFrom(common)
            configurations["development$projectLoader"].extendsFrom(common)
            
            dependencies {
                common(project(":$modName:common", "namedElements")) { isTransitive = false }
                shadowCommon(project(":$modName:common", "transformProduction$projectLoader")) { isTransitive = false }
            }
            
            val processFile = if (name == "neoforge") "META-INF/neoforge.mods.toml" else "fabric.mod.json"

            tasks.withType<ProcessResources> {
                val replaceProperties = mapOf("minecraftVersion" to minecraftVersion, "modVersion" to properties[modName])
                inputs.properties(replaceProperties)
                
                filesMatching(processFile) {
                    expand(replaceProperties)
                }
            }

            tasks.withType<ShadowJar> {
                configurations = listOf(shadowCommon)
                archiveClassifier.set("dev-shadow")
            }

            tasks.withType<RemapJarTask> {
                val shadowTask = project.tasks.shadowJar.get()
                inputFile.set(shadowTask.archiveFile)
            }
        }
    } else {
        architectury {
            minecraft = minecraftVersion
        }
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }

    java {
        withSourcesJar()

        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}