package ui

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.*
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ComposeTest {

  @Test
  fun uiTest() = runComposeUiTest {
    setContent {
      var text by remember { mutableStateOf("Hello") }
      Text(text = text, modifier = Modifier.testTag("txtTag"))
      Button(onClick = { text = "Compose" }, modifier = Modifier.testTag("btnTag")) {
        Text("Click me")
      }
    }

    onNodeWithTag("txtTag").assertTextEquals("Hello")
    onNodeWithTag("btnTag").performClick()
    onNodeWithTag("txtTag").assertTextEquals("Compose")
  }
}
