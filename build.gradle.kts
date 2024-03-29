import org.eclipse.jgit.lib.Repository
import org.spongepowered.gradle.plugin.config.PluginLoaders

plugins {
    val pluginVersion = "6.1.0"
    val indraVersion = "3.1.3"
    val spongeGradleVersion = "2.2.0"

    id("net.kyori.indra.git") version indraVersion
    id("net.kyori.indra") version indraVersion
    id("ca.stellardrift.opinionated") version pluginVersion
    id("net.kyori.blossom") version "2.0.1"
    id("org.spongepowered.gradle.plugin") version spongeGradleVersion
    id("org.spongepowered.gradle.ore") version spongeGradleVersion
    id("com.modrinth.minotaur") version "2.8.3"
    id("ca.stellardrift.publish-github-release") version "0.1.0"
    id("org.jetbrains.gradle.plugin.idea-ext") version "1.1.7"
}

group = "ca.stellardrift"
version = "2.1.0-SNAPSHOT"
description = "Let Sponge servers be discovered in the LAN server discovery"

indra {
    github("zml2008", "LanDiscovery") {
        ci(true)
    }
    lgpl3OrLaterLicense()

    configurePublications {
        pom {
            developers {
                developer {
                    name.set("zml")
                    email.set("zml at stellardrift [.] ca")
                }
            }
        }
    }

    publishReleasesTo("stellardrift", "https://repo.stellardrift.ca/repository/releases/")
    publishSnapshotsTo("stellardrift", "https://repo.stellardrift.ca/repository/snapshots/")
}
opinionated {
    automaticModuleNames = true
}

sourceSets.main {
    blossom.javaSources {
        property("name", provider { project.name })
        property("version", provider { project.version.toString() })
    }
}

tasks.jar {
    arrayOf("COPYING", "COPYING.LESSER").forEach {src ->
        from(file(src)) {
            rename { "${it}_LanDiscovery" }
        }
    }
}

spotless {
    ratchetFrom("origin/trunk")
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
            homepageLink.set(indra.scm().map { uri(it.url()) })
            issuesLink.set(indra.issues().map { uri(it.url()) })
            sourceLink.set(indra.scm().map { uri(it.url()) })
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

val changelogContents = objects.property(String::class)
changelogContents.set(providers.gradleProperty("changelog").map {
    file(it).readText(Charsets.UTF_8)
}.orElse(""))
changelogContents.finalizeValueOnRead()

modrinth {
    token.set(providers.gradleProperty("modrinthToken"))
    projectId.set("landiscovery")
    gameVersions.set(listOf("1.16.5", "1.18.2", "1.19.2"))
    syncBodyFrom.set(rootProject.file("README.md").readText(Charsets.UTF_8))
    changelog.set(changelogContents)
}

githubRelease {
    apiToken = providers.gradleProperty("githubToken")
            .orElse(providers.environmentVariable("GITHUB_TOKEN"))

    repository = indra.scm().map {
        val orgSlashRepo = uri(it.url()).path.trimStart('/').split('/')
        "${orgSlashRepo[0]}/${orgSlashRepo[1]}"
    }
    tagName = project.provider {
        indraGit.headTag()?.run { Repository.shortenRefName(name) }
    }

    releaseName = "LanDiscovery v$version"
    releaseBody = changelogContents
    artifacts.from(tasks.jar.map { it.outputs})
}

oreDeployment {
    apiKey().set(
        providers.gradleProperty("oreToken")
            .orElse(providers.environmentVariable("ORE_TOKEN"))
    )

    defaultPublication {
        versionBody.set(changelogContents)
    }
}


tasks.register("publishRelease") {
    description = "Publish a release to all applicable download locations"
    dependsOn("githubRelease", "modrinth", "publishToOre", "publish")
}
