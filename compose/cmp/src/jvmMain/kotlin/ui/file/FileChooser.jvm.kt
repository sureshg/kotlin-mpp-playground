package ui.file

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter
import javax.swing.filechooser.FileSystemView

@Composable
fun FileDialog(parent: Frame? = null, onClose: (result: List<File>) -> Unit) =
    AwtWindow(
        visible = true,
        create = {
          object : FileDialog(parent, "Choose a file", LOAD) {
            override fun isMultipleMode() = true

            override fun setVisible(visible: Boolean) {
              super.setVisible(visible)
              if (visible) {
                onClose(files.toList())
              }
            }
          }
        },
        dispose = FileDialog::dispose)

fun fileChooser(parent: Frame? = null, onClose: (result: List<File>) -> Unit) {
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
  val chooser =
      JFileChooser(FileSystemView.getFileSystemView()).apply {
        currentDirectory = File(System.getProperty("user.dir"))
        fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES
        isMultiSelectionEnabled = true
        dialogTitle = "Select a folder"
        approveButtonText = "Select"
        approveButtonToolTipText = "Select current directory as save destination"
        fileFilter = FileNameExtensionFilter("JPG & GIF Images", "jpg", "gif")
      }

  val files = chooser.selectedFiles.toList()
  chooser.isVisible = false
  onClose(files)
}
