package dev.suresh.flow
//
// import androidx.compose.runtime.*
// import app.cash.molecule.RecompositionMode
// import app.cash.molecule.moleculeFlow
// import kotlinx.coroutines.delay
// import kotlinx.datetime.*
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
// private fun currentTime(tz: TimeZone) = Clock.System.now().toLocalDateTime(tz)
//
// fun timerComposeFlow(tz: TimeZone = TimeZone.currentSystemDefault()) =
//     moleculeFlow(RecompositionMode.Immediate) { timer(tz) }
