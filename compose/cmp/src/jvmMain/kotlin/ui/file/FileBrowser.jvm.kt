@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)

package ui.file

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropTarget
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draganddrop.dragData
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
    var fPaths by remember { mutableStateOf(emptyList<Path>()) }
    Column {
      DragDropBox { dragData ->
        if (dragData is DragData.FilesList) {
          val newPaths =
              dragData.readFiles().mapNotNull {
                URI(it).toPath().takeIf { it.exists(LinkOption.NOFOLLOW_LINKS) }
              }
          fPaths = (newPaths + fPaths).distinct()
        }
      }
      FileListView(files = fPaths)
    }
  }
}

@Composable
fun DragDropBox(modifier: Modifier = Modifier, onDrop: (DragData) -> Unit) {
  var isDragging by remember { mutableStateOf(false) }
  val dndColor = if (isDragging) FileColors.active else FileColors.default
  var fdOpen by remember { mutableStateOf(false) }
  // val (textField, setTextField) = remember { mutableStateOf(TextFieldValue()) }

  val dndTarget = remember {
    object : DragAndDropTarget {
      override fun onEntered(event: DragAndDropEvent) {
        isDragging = true
      }

      override fun onExited(event: DragAndDropEvent) {
        isDragging = false
      }

      override fun onDrop(event: DragAndDropEvent): Boolean {
        isDragging = false
        val dragData = event.dragData()
        return when (dragData) {
          is DragData.FilesList -> {
            onDrop(dragData)
            true
          }
          else -> {
            println("Unsupported drag data: $dragData")
            false
          }
        }
      }
    }
  }

  Box(
      modifier =
          modifier
              .dashedBorder(strokeWidth = 2.dp, color = dndColor, cornerRadius = 8.dp)
              .dragAndDropTarget(shouldStartDragAndDrop = { true }, target = dndTarget)) {
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
