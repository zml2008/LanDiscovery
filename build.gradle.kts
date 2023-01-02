import org.eclipse.jgit.lib.Repository
import java.net.URL
import org.spongepowered.gradle.plugin.config.PluginLoaders

plugins {
    val pluginVersion = "6.0.1"
    val indraVersion = "3.0.1"
    val spongeGradleVersion = "2.1.1"

    id("net.kyori.indra.git") version indraVersion
    id("net.kyori.indra") version indraVersion
    id("ca.stellardrift.opinionated") version pluginVersion
    id("ca.stellardrift.templating") version pluginVersion
    id("org.spongepowered.gradle.plugin") version spongeGradleVersion
    id("org.spongepowered.gradle.ore") version spongeGradleVersion
    id("com.modrinth.minotaur") version "2.5.0"
    id("com.github.breadmoirai.github-release") version "2.4.1"
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

repositories {
    stellardrift.releases()
    stellardrift.snapshots()
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
    token(providers.gradleProperty("githubToken")
        .orElse(providers.environmentVariable("GITHUB_TOKEN")))
    val orgSlashRepo = indra.scm().map {
        val path = it.url()
        uri(path).path.trimStart('/').split('/')
    }
    owner(orgSlashRepo.map { it[0] })
    repo(orgSlashRepo.map { it[1] })

    tagName(indraGit.headTag()?.name?.let(Repository::shortenRefName))
    releaseAssets.from(tasks.jar.map { it.outputs })
    body.set(changelogContents)
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
