import androidx.compose.runtime.Composable

@Composable
fun PageLayout(content: @Composable () -> Unit) {
  Column {
    NavHeader()
    content()
    Footer()
  }
}
