package skikotest

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.ui.Messages

/**
 * Tools | "Skiko Check (Plugin X)": runs this plugin's [SkikoProbe] (the copy loaded by this plugin's class loader)
 * and shows the report, so the check can be done by hand in a normal IDE session.
 */
class ShowProbeAction : AnAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.EDT

    override fun actionPerformed(e: AnActionEvent) {
        val text = try {
            val report = SkikoProbe().run()
            val ok = listOf("own SkiaLayer", "own core", "own native core").all { key ->
                report.lines().any { it.startsWith(key) && it.endsWith("true") }
            }
            report + "\nRESULT: " + if (ok) "OK" else "FAILED"
        } catch (t: Throwable) {
            "FAILED: " + t.stackTraceToString()
        }
        Messages.showInfoMessage(e.project, text, e.presentation.text)
    }
}
