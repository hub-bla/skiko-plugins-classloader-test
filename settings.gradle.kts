import org.jetbrains.intellij.platform.gradle.extensions.intellijPlatform

rootProject.name = "skiko-plugins-classloader-test"

pluginManagement {
    plugins {
        id("org.jetbrains.kotlin.jvm") version "2.4.0"
        id("org.jetbrains.changelog") version "2.5.0"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("org.jetbrains.intellij.platform.settings") version "2.20.0"
}

dependencyResolutionManagement {
    // Configure all projects' repositories
    repositories {
        // Skiko builds of this experiment come from a local publication (see README-SKIKO.md);
        // released versions (e.g. 0.156.1) come from Maven Central.
        mavenLocal()
        mavenCentral()

        // IntelliJ Platform Gradle Plugin Repositories Extension - read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-repositories-extension.html
        intellijPlatform {
            defaultRepositories()
        }
    }
}

// Two plugins, each bundling its own Skiko.
include(":plugin-a", ":plugin-b")
