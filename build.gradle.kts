import ca.stellardrift.build.common.agpl3
import ca.stellardrift.build.common.stellardriftReleases
import ca.stellardrift.build.common.stellardriftSnapshots
import java.net.URL
import org.spongepowered.gradle.plugin.config.PluginLoaders

plugins {
    val pluginVersion = "4.2.1"
    id("ca.stellardrift.opinionated") version pluginVersion
    id("ca.stellardrift.templating") version pluginVersion
    id("org.spongepowered.gradle.plugin") version "1.1.1"
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
    stellardriftReleases()
    stellardriftSnapshots()
}

sponge {
    apiVersion("8.0.0")
    injectRepositories(false)
    plugin("landiscovery") {
        displayName("LanDiscovery")
        loader(PluginLoaders.JAVA_PLAIN)
        description(project.description)
        mainClass("ca.stellardrift.landiscovery.LanDiscoveryPlugin")
        links {
            homepage.set(indra.scm.map { URL(it.url) })
            issues.set(indra.issues.map { URL(it.url) })
            source.set(indra.scm.map { URL(it.url) })
        }
        contributor("zml") {
            description("developer")
        }
        dependency("spongeapi") {
            optional(false)
        }
    }
}
