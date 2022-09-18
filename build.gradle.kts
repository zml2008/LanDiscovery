import ca.stellardrift.build.common.stellardriftReleases
import ca.stellardrift.build.common.stellardriftSnapshots
import java.net.URL
import org.spongepowered.gradle.plugin.config.PluginLoaders

plugins {
    val pluginVersion = "5.0.1"
    id("net.kyori.indra") version "2.1.1"
    id("ca.stellardrift.opinionated") version pluginVersion
    id("ca.stellardrift.templating") version pluginVersion
    id("org.spongepowered.gradle.plugin") version "2.0.2"
}

group = "ca.stellardrift"
version = "2.0.0"
description = "Let Sponge servers be discovered in the LAN server discovery"

indra {
    github("zml2008", "LanDiscovery") {
        ci(true)
    }
    lgpl3OrLaterLicense()
}
opinionated {
    automaticModuleNames = true
}

repositories {
    stellardriftReleases()
    stellardriftSnapshots()
}

tasks.jar {
    arrayOf("COPYING", "COPYING.LESSER").forEach {src ->
        from(file(src)) {
            rename { "${it}_LanDiscovery" }
        }
    }
}

sponge {
    apiVersion("8.1.0")
    injectRepositories(false)
    plugin("landiscovery") {
        displayName("LanDiscovery")
        loader {
            name(PluginLoaders.JAVA_PLAIN)
            version("1.0.0")
        }
        description(project.description)
        entrypoint("ca.stellardrift.landiscovery.LanDiscoveryPlugin")
        links {
            homepage.set(indra.scm().map { URL(it.url()) })
            issues.set(indra.issues().map { URL(it.url()) })
            source.set(indra.scm().map { URL(it.url()) })
        }
        license.set(indra.license().map { it.name() })
        contributor("zml") {
            description("developer")
        }
        dependency("spongeapi") {
            optional(false)
        }
    }
}