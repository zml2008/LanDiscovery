import ca.stellardrift.build.common.agpl3
import ca.stellardrift.build.configurate.ConfigFormats
import ca.stellardrift.build.configurate.transformations.convertFormat

plugins {
    val pluginVersion = "4.1"
    id("ca.stellardrift.opinionated") version pluginVersion
    id("ca.stellardrift.configurate-transformations") version pluginVersion
    id("ca.stellardrift.templating") version pluginVersion
}

group = "ca.stellardrift"
version = "2.0-SNAPSHOT"
description = "Let Sponge servers be discovered in the LAN server discovery"

indra {
    github("zml2008", "LanDiscovery") {
        ci = true
    }
    agpl3()
}
opinionated {
    automaticModuleNames = true
}

repositories {
    maven("https://repo.stellardrift.ca/repository/stable/") {
        name = "stellardriftReleases"
        mavenContent { releasesOnly() }
    }

    maven("https://repo.stellardrift.ca/repository/snapshots/") {
        name = "stellardriftSnapshots"
        mavenContent { snapshotsOnly() }
    }
}

dependencies {
    annotationProcessor(implementation("org.spongepowered:spongeapi:8.0.0-SNAPSHOT")!!)
}

tasks {
    processResources {
        inputs.property("version", version)

        filesMatching("**/*.yml") {
            expand("project" to project)
            convertFormat(ConfigFormats.YAML, ConfigFormats.JSON)
            name = name.substringBeforeLast('.') + ".json"
        }
    }
}
