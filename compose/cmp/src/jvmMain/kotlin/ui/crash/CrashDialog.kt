@file:OptIn(ExperimentalComposeUiApi::class)

package ui.crash

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.WindowExceptionHandler
import androidx.compose.ui.window.WindowExceptionHandlerFactory
import dev.suresh.log
import java.awt.Dimension
import java.awt.Font
import java.io.File
import javax.swing.JOptionPane
import javax.swing.JScrollPane
import javax.swing.JTextArea
import kotlin.system.exitProcess

private const val FONT_SIZE = 10
private const val MSG_WIDTH = 640
private const val MSG_HEIGHT = 480

fun showCrashDialog(logDirectory: File, exception: Throwable) {
  val title = "App failed unexpectedly"
  val text = buildString {
    appendLine("Log directory:")
    appendLine(logDirectory)
    appendLine()
    appendLine("Stack trace:")
    appendLine(exception.stackTraceToString())
  }
  JOptionPane.showMessageDialog(
      null,
      JScrollPane(
              JTextArea(text).apply {
                font = Font(Font.MONOSPACED, Font.PLAIN, FONT_SIZE)
                lineWrap = false
              },
              JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
              JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS,
          )
          .apply { preferredSize = Dimension(MSG_WIDTH, MSG_HEIGHT) },
      title,
      JOptionPane.ERROR_MESSAGE,
  )
}

val windowExceptionHandlerFactory = WindowExceptionHandlerFactory { window ->
  WindowExceptionHandler { ex ->
    window.dispose()
    log.error(ex) { "Application UI crash" }
    showCrashDialog(File("."), ex)
    exitProcess(1)
  }
}
