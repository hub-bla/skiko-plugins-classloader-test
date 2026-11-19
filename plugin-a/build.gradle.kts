plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

val skikoVersion = providers.gradleProperty("skikoA").get()

sourceSets.main { kotlin.srcDir("../common/src/main/kotlin") }

dependencies {
    intellijPlatform {
        local(providers.gradleProperty("ideaPath").get())
    }
    // The IDE provides Kotlin and coroutines; the plugin bundles only its own Skiko.
    // skikoMode=mono: a released, not-yet-split Skiko (a single native library, no skiko-platform).
    val skikoArtifact = if (providers.gradleProperty("skikoMode").orNull == "mono") "skiko-awt-runtime-macos-arm64" else "skiko-platform-awt-runtime-macos-arm64"
    implementation("org.jetbrains.skiko:$skikoArtifact:$skikoVersion") {
        exclude(group = "org.jetbrains.kotlin")
        exclude(group = "org.jetbrains.kotlinx")
    }
}

kotlin { jvmToolchain(21) }

dependencies {
    intellijPlatform {
        // The other plugin is installed next to this one in the sandbox.
        localPlugin(project(":plugin-b"))
    }
}

intellijPlatformTesting {
    runIde {
        register("runCheck") {
            task {
                val order = providers.gradleProperty("order").orElse("ab")
                val out = layout.buildDirectory.file("check-${order.get()}.txt").get().asFile
                args("skikoCheck", order.get())
                providers.gradleProperty("ideJvmArgs").orNull?.let { jvmArgs(it.split(" ")) }
                jvmArgs("-Dskiko.check.out=${out.absolutePath}")
            }
        }
    }
}

// A normal IDE with both plugins installed: use Tools | Skiko Check (Plugin A / Plugin B), in any order.
intellijPlatformTesting {
    runIde {
        register("runBoth") {
            // No extra tasks: the IDE just starts with plugin A and plugin B (see localPlugin above).
        }
    }
}
