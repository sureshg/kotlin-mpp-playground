import androidx.compose.runtime.Composable
import androidx.compose.ui.window.AwtWindow
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import javax.swing.JFileChooser
import javax.swing.UIManager
import javax.swing.filechooser.FileNameExtensionFilter

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

fun fileChooser(parent: Frame? = null): List<File> {
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
  val chooser =
      JFileChooser().apply {
        fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
        isMultiSelectionEnabled = true
        dialogTitle = "Select a folder"
        approveButtonText = "Select"
        approveButtonToolTipText = "Select current directory as save destination"
        fileFilter = FileNameExtensionFilter("JPG & GIF Images", "jpg", "gif")
      }

  chooser.showOpenDialog(parent)
  val files = chooser.selectedFiles.toList()
  chooser.isVisible = false
  return files
}
