plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.intellij.platform")
}

val skikoVersion = providers.gradleProperty("skikoB").get()

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
