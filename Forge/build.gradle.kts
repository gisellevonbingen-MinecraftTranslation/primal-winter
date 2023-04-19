plugins {
    java
    idea
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("net.minecraftforge.gradle") version "5.1.+"
    id("org.spongepowered.mixin") version "0.7-SNAPSHOT"
    id("org.parchmentmc.librarian.forgegradle") version "1.+"
}


// From gradle.properties
val modId: String by extra
val modGroup: String by extra

val minecraftVersion: String by extra
val forgeVersion: String by extra
val parchmentVersionForge: String by extra
val epsilonVersion: String by extra

val shadowLibrary = configurations.create("shadowLibrary")

configurations {
    implementation.get().extendsFrom(shadowLibrary)
}

base {
    archivesName.set("${modId}-forge-${minecraftVersion}")
}

repositories {
    maven(url = "https://alcatrazescapee.jfrog.io/artifactory/mods")
}

dependencies {
    "minecraft"(group = "net.minecraftforge", name = "forge", version = "${minecraftVersion}-${forgeVersion}")

    shadowLibrary(group = "com.alcatrazescapee", name = "epsilon", version = epsilonVersion)

    implementation(project(":Common"))

    if (System.getProperty("idea.sync.active") != "true") {
        annotationProcessor(group = "org.spongepowered", name = "mixin", version = "0.8.5", classifier = "processor")
    }
}

minecraft {
    mappings("parchment", parchmentVersionForge)

    runs {
        all {
            args("-mixin.config=$modId.mixins.json")
            property("forge.logging.console.level", "debug")
            ideaModule("${project.name}.test")
            workingDirectory("run")

            mods.create(modId) {
                source(sourceSets.main.get())
                source(project(":Common").sourceSets.main.get())
            }
        }

        register("client") {}
        register("server") {
            arg("--nogui")
        }
    }
}

mixin {
    add(sourceSets.main.get(), "${modId}.refmap.json")

    config("${modId}.mixins.json")
    config("${modId}.common.mixins.json")
}

// Workaround for a bug in Forge / Mixin gradle where the refmap won't be added to the jar unless Forge java compile is done
// From https://github.com/gamma-delta/HexMod/blob/main/Forge/build.gradle#L161
tasks.register("invalidateJavaForRefmap") {
    doFirst {
        tasks.compileJava {
            if (!didWork) {
                outputs.upToDateWhen { false }
            }
        }
    }
}

tasks {
    jar {
        classifier = "slim"
        finalizedBy("reobfJar")
    }

    shadowJar {
        classifier = ""
        configurations = listOf(shadowLibrary)
        dependencies {
            exclude(dependency(closureOf<ResolvedDependency> {
                moduleGroup != modGroup
            }))
        }
        relocate("com.alcatrazescapee.epsilon", "${modGroup}.${modId}.epsilon")
        finalizedBy("reobfShadowJar")
    }

    reobf {
        shadowJar {}
    }

    assemble {
        dependsOn(shadowJar)
    }
}

idea {
    module {
        for (fileName in listOf("run", "out", "logs")) {
            excludeDirs.add(file(fileName))
        }
    }
}
