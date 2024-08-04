@file:OptIn(ExperimentalComposeUiApi::class)

package ui.file

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.net.URI
import java.nio.file.LinkOption
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.toPath
import ui.FileColors
import ui.dashedBorder

@Composable
actual fun DragDropListView() {
  Box(modifier = Modifier, contentAlignment = Alignment.Center) {
    Column {
      var droppedPaths by remember { mutableStateOf(emptyList<Path>()) }
      DragDropBox { dragData ->
        if (dragData is DragData.FilesList) {
          val newPaths =
              dragData.readFiles().mapNotNull { fPath ->
                URI(fPath).toPath().takeIf { it.exists(LinkOption.NOFOLLOW_LINKS) }
              }
          droppedPaths = (newPaths + droppedPaths).distinct()
        }
      }
      FileListView(files = droppedPaths)
    }
  }
}

@Composable
fun DragDropBox(modifier: Modifier = Modifier, onDrop: (DragData) -> Unit) {
  var isDragging by remember { mutableStateOf(false) }
  val dndColor = if (isDragging) FileColors.active else FileColors.default
  var fdOpen by remember { mutableStateOf(false) }

  // val (textField, setTextField) = remember { mutableStateOf(TextFieldValue()) }

  Box(
      modifier =
          modifier
              .dashedBorder(strokeWidth = 2.dp, color = dndColor, cornerRadius = 8.dp)
              .onExternalDrag(
                  onDragStart = { isDragging = true },
                  onDragExit = { isDragging = false },
                  onDrop = {
                    isDragging = false
                    onDrop(it.dragData)
                  })) {
        Column(modifier = Modifier.align(Alignment.Center)) {
          Text(
              "Drag & drop files here",
              modifier = Modifier.padding(20.dp),
              color = dndColor,
              fontSize = 14.sp)

          OutlinedButton(
              modifier = modifier.align(Alignment.CenterHorizontally).padding(10.dp),
              enabled = isDragging.not(),
              shape = RoundedCornerShape(10.dp),
              onClick = { fdOpen = !fdOpen }) {
                Icon(Icons.Default.KeyboardArrowUp, "Upload")
                Text("Select")
              }

          if (fdOpen) {
            FileDialog { files ->
              val data =
                  object : DragData.FilesList {
                    override fun readFiles() = files.map { it.toURI().toString() }
                  }

              onDrop(data)
              fdOpen = false
            }
            // DisposableEffect(isOpen) {// open FileChooser}
          }
        }
      }
}

@Composable
fun FileListView(modifier: Modifier = Modifier, files: List<Path>) {
  LazyColumn(modifier) {
    items(files) {
      Box(
          modifier =
              Modifier.padding(5.dp).background(FileColors.fileItemBg, RoundedCornerShape(10.dp))) {
            Text(
                modifier = Modifier.padding(4.dp),
                text = it.fileName.toString(),
                color = FileColors.fileItemFg,
                fontSize = 14.sp)
          }
    }
  }
}
