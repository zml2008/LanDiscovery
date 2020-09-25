import ca.stellardrift.build.transformations.ConfigFormats
import ca.stellardrift.build.transformations.convertFormat

plugins {
    val pluginVersion = "3.1"
    id("ca.stellardrift.opinionated") version pluginVersion
    id("ca.stellardrift.configurate-transformations") version pluginVersion
    id("ca.stellardrift.templating") version pluginVersion
}

group = "ca.stellardrift"
version = "1.0.3-SNAPSHOT"
description = "Let Sponge servers be discovered in the LAN server discovery"

opinionated {
    github("zml2008", "LanDiscovery")
    agpl3()

    automaticModuleNames = true
}

license {
    newLine = false
}

repositories {
    maven("https://repo-new.spongepowered.org/repository/maven-public") {
        name = "sponge"
    }
}

dependencies {
    annotationProcessor(implementation("org.spongepowered:spongeapi:7.3.0")!!)
}

tasks {
    jar {
        manifest.attributes(
                "Loader" to "java_plain"
        )
    }

    processResources {
        inputs.property("version", version)

        filesMatching("*.yml") {
            expand("project" to project)
            convertFormat(ConfigFormats.YAML, ConfigFormats.GSON)
            name = name.substringBeforeLast('.') + ".json"
        }
    }
}
