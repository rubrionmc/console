import java.net.URL
import java.nio.file.Files

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://rubrionmc.github.io/repository/")
    }

    versionCatalogs {
        create("libs") {
            val remoteUrl = "https://rubrionmc.github.io/repository/resources/libs.versions.toml"
            val localFile = file("$rootDir/.gradle/tmp-libs.versions.toml")

            if (localFile.exists()) {
                localFile.delete()
            }

            println("Load global libs.versions.toml...")
            localFile.parentFile.mkdirs()
            URL(remoteUrl).openStream().use { input ->
                Files.copy(input, localFile.toPath())
            }

            from(files(localFile))
        }
    }
}

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins { kotlin("jvm") version "2.2.10" }
}

rootProject.name = "rub-console"

rootProject.name = "rub-console"

include("common")
include("paper", "spigot")
include("velocity", "bungee")

project(":common").projectDir = file("rub-common")
project(":paper").projectDir = file("rum-paper")
project(":spigot").projectDir = file("rum-spigot")
project(":velocity").projectDir = file("rup-velocity")
project(":bungee").projectDir = file("rup-bungee")
