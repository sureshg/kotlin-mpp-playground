package dev.suresh.flow

// import androidx.compose.runtime.*
// import app.cash.molecule.RecompositionMode
// import app.cash.molecule.moleculeFlow
// import kotlinx.coroutines.delay
//
//
// @Composable
// fun counter(start: Int = 0): Int {
//    var count by remember { mutableStateOf(start) }
//    LaunchedEffect(Unit) {
//        while (true) {
//            delay(1000)
//            count++
//        }
//    }
//    return count
// }
//
// fun countFlow(start: Int = 0) = moleculeFlow(RecompositionMode.Immediate) {
//    counter(start)
// }
