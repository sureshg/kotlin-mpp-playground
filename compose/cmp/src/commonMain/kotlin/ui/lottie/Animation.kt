package ui.lottie

import KottieAnimation
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import dev.suresh.compose.res.Res
import kottieComposition.KottieCompositionSpec
import kottieComposition.animateKottieCompositionAsState
import kottieComposition.rememberKottieComposition
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun lottie(modifier: Modifier = Modifier, res: String = "files/lottie/anim.json") {
  var afterEffectAnim by remember { mutableStateOf("") }

  LaunchedEffect(Unit) { afterEffectAnim = Res.readBytes(res).decodeToString() }

  val composition = rememberKottieComposition(spec = KottieCompositionSpec.File(afterEffectAnim))
  val animationState by
      animateKottieCompositionAsState(
          composition = composition,
          isPlaying = true,
      )

  KottieAnimation(
      modifier = modifier,
      composition = composition,
      progress = { animationState.progress },
  )
}
