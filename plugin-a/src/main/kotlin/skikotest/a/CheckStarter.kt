package skikotest.a

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationStarter
import com.intellij.openapi.extensions.PluginId
import java.io.File
import kotlin.system.exitProcess

/**
 * `idea skikoCheck <order>` where order is `ab` or `ba`: runs the probe of each plugin, in its own
 * class loader, in that order; writes a report to -Dskiko.check.out and exits non-zero on failure.
 */
internal class CheckStarter : ApplicationStarter {
    override fun main(args: List<String>) {
        val order = args.getOrElse(1) { "ab" }
        val report = StringBuilder()
        var ok = true
        for (id in order) {
            report.appendLine("=== plugin $id")
            try {
                val plugin = PluginManagerCore.getPlugin(PluginId.getId("org.example.skiko.$id"))
                    ?: error("plugin $id is not installed")
                val loader = plugin.pluginClassLoader ?: error("plugin $id has no class loader")
                val probe = loader.loadClass("skikotest.SkikoProbe").getDeclaredConstructor().newInstance()
                val before = probe.javaClass.getMethod("describe").invoke(probe) as String
                report.append(before)
                System.getProperty("skiko.check.out")?.let { File(it).writeText(report.toString()) }
                val text = probe.javaClass.getMethod("run").invoke(probe) as String
                report.append(text)
                if ("own SkiaLayer     : true" !in text || "own core          : true" !in text ||
                    "ganesh->core call : ok" !in text ||
                    ("own native core   : true" !in text && "own native core   : n/a" !in text)
                ) ok = false
            } catch (t: Throwable) {
                ok = false
                report.appendLine("FAILED: " + t.stackTraceToString())
            }
        }
        // Different Skiko copies must have different native core instances.
        val instances = Regex("core instance     : (0x[0-9a-f]+)").findAll(report).map { it.groupValues[1] }.toList()
        if (instances.size > 1 && instances.toSet().size != instances.size) {
            ok = false
            report.appendLine("FAILED: plugins share one native core instance: $instances")
        }
        report.appendLine(if (ok) "RESULT: OK" else "RESULT: FAILED")
        System.getProperty("skiko.check.out")?.let { File(it).writeText(report.toString()) }
        print(report)
        exitProcess(if (ok) 0 else 1)
    }
}
