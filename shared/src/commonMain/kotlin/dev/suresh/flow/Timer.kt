package dev.suresh.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// import androidx.compose.runtime.*
// import app.cash.molecule.RecompositionMode
// import app.cash.molecule.moleculeFlow
//
// @Composable
// fun timer(tz: TimeZone): LocalDateTime {
//   var time by remember { mutableStateOf(currentTime(tz)) }
//   LaunchedEffect(Unit) {
//     while (true) {
//       delay(1000)
//       time = currentTime(tz)
//     }
//   }
//   return time
// }
//
// fun timerComposeFlow(tz: TimeZone = TimeZone.currentSystemDefault()) =
//     moleculeFlow(RecompositionMode.Immediate) { timer(tz) }

private fun currentTime(tz: TimeZone) = Clock.System.now().toLocalDateTime(tz)

fun timerComposeFlow(tz: TimeZone = TimeZone.currentSystemDefault()) = flow {
  while (true) {
    delay(1000)
    emit(currentTime(tz))
  }
}
