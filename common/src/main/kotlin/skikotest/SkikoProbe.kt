package skikotest

import org.jetbrains.skia.ImageInfo
import org.jetbrains.skiko.SkiaLayer

/**
 * Compiled into every plugin, so each plugin has its own copy loaded by its own plugin class loader.
 * Creating a [SkiaLayer] loads skiko-platform (and core, Ganesh) the way a real plugin would.
 */
class SkikoProbe {
    /** Where Skiko classes come from, without initializing (and so loading native code of) any of them. */
    fun describe(): String = buildString {
        val loader = SkikoProbe::class.java.classLoader
        appendLine("probe loader      : $loader")
        appendLine("skiko.library.path: ${System.getProperty("skiko.library.path")}")
        for (name in listOf(
            "org.jetbrains.skia.ImageInfo",
            "org.jetbrains.skia.impl.Library",
            "org.jetbrains.skiko.Library",
            "org.jetbrains.skiko.SkiaLayer",
        )) {
            val c = Class.forName(name, false, loader)
            appendLine("$name\n    loader: ${c.classLoader}\n    from  : ${c.classLoader.getResource(name.replace('.', '/') + ".class")}")
        }
    }

    fun run(): String {
        // The IDE points skiko.library.path at its own (different) Skiko build; a plugin bundling its own Skiko
        // must not load that one. Set -Dskiko.check.keepLibraryPath=true to see what happens if it does.
        if (System.getProperty("skiko.check.keepLibraryPath") != "true") System.clearProperty("skiko.library.path")
        val layer = SkiaLayer()
        fun origin(c: Class<*>) = c.classLoader.getResource(c.name.replace('.', '/') + ".class")
        val probeLoader = SkikoProbe::class.java.classLoader
        return buildString {
            appendLine("probe loader      : $probeLoader")
            appendLine("SkiaLayer loader  : ${layer.javaClass.classLoader}")
            appendLine("SkiaLayer from    : ${origin(layer.javaClass)}")
            appendLine("ImageInfo loader  : ${ImageInfo::class.java.classLoader}")
            appendLine("ImageInfo from    : ${origin(ImageInfo::class.java)}")
            appendLine("own SkiaLayer     : ${layer.javaClass.classLoader === probeLoader}")
            appendLine("own core          : ${ImageInfo::class.java.classLoader === probeLoader}")
            // A production binding, not a test helper: Ganesh's native code calls into Skia code that lives in core
            // (the Ganesh library imports hundreds of symbols from core). It needs no GL context.
            try {
                org.jetbrains.skia.BackendRenderTarget.makeGL(64, 64, 0, 8, 0, 0x8058 /* GL_RGBA8 */).close()
                appendLine("ganesh->core call : ok")
            } catch (t: Throwable) {
                appendLine("ganesh->core call : FAILED $t")
            }
            // The native check: the core library instance this plugin's classes use must be the one platform is
            // linked to. Both addresses come from test helpers that exist in non-release Skiko builds.
            try {
                val core = org.jetbrains.skiko.tests._nCoreInstance()
                val seenByPlatform = org.jetbrains.skiko.tests._nCoreInstanceSeenByPlatform()
                appendLine("core instance     : 0x${core.toString(16)}")
                appendLine("platform's core   : 0x${seenByPlatform.toString(16)}")
                appendLine("own native core   : ${core == seenByPlatform}")
            } catch (e: UnsatisfiedLinkError) {
                appendLine("own native core   : n/a (no native probe in this Skiko)")
            }
        }
    }
}
