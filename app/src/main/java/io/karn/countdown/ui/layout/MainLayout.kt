package io.karn.countdown.ui.layout

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import io.karn.countdown.MainViewModel
import io.karn.countdown.ext.popOrNull
import io.karn.countdown.ext.stackOf

enum class CountDownState {
    ACTIVE,
    UNSET
}

// Null indicates a seperator
val DIGITS = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, null, 0, null)

@Composable
fun MainLayout(navController: NavHostController, viewModel: MainViewModel) {
    val secondsRemaining = viewModel.remainingTime.collectAsState()

    val currentState = remember { mutableStateOf(CountDownState.UNSET) }

    Crossfade(
        targetState = currentState,
        animationSpec = tween(500),
        modifier = Modifier.fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) { state ->
        when (state.value) {
            CountDownState.ACTIVE -> ActiveTimer(secondsRemaining)
            CountDownState.UNSET -> UnsetTimer { targetTime ->
                viewModel.onTimerStartRequest(targetTime)

                currentState.value = CountDownState.ACTIVE
            }
        }
    }
}

@Composable
fun ActiveTimer(secondsRemaining: State<Int>) {

    Column {
        Box(modifier = Modifier.weight(1f)) {
            Text(text = formatSeconds(secondsRemaining.value))
        }

        Row {
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Delete")
            }
            Button(onClick = { /*TODO*/ }) {
                Text(text = "Delete")
            }
        }
    }
}

@Composable
fun UnsetTimer(onStart: (Int) -> Unit) {
    val targetTime = remember { mutableStateListOf<Int>() }

    Column {
        // Defines the time required
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                text = formatTargetTime(targetTime),
                style = MaterialTheme.typography.h3
            )

            IconButton(
                modifier = Modifier.align(Alignment.CenterVertically),
                onClick = {
                    if (targetTime.size > 0) {
                        targetTime.removeAt(targetTime.size - 1)
                    }
                }) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    modifier = Modifier.size(24.dp),
                    contentDescription = "delete last digit"
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            DIGITS.chunked(3).forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    modifier = Modifier
                ) {
                    rowItems.map { digit ->
                        Box(
                            modifier = Modifier.weight(1f)
                                .clickable(enabled = digit != null) {
                                    // There are six total items HHMMSS
                                    if (digit != null && targetTime.size < 6) {
                                        // Skip adding zero to the start of the list
                                        val isFirstDigitZero = digit == 0 && targetTime.size == 0
                                        if (!isFirstDigitZero) {
                                            targetTime.add(digit)
                                        }
                                    }
                                }
                        ) {
                            if (digit != null) {
                                Text(
                                    modifier = Modifier.align(Alignment.Center),
                                    text = "$digit", style = MaterialTheme.typography.h3
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                modifier = Modifier.padding(24.dp),
                shape = CircleShape,
                onClick = {
                    onStart(targetTimeToSeconds(targetTime))
                }) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "play")
            }
        }
    }
}

fun formatSeconds(timeInSeconds: Int): String {
    val hours = timeInSeconds / 3600
    val secondsLeft = timeInSeconds - hours * 3600
    val minutes = secondsLeft / 60
    val seconds = secondsLeft - minutes * 60

    return StringBuilder()
        .append("$hours".padStart(2, '0'))
        .append(":")
        .append("$minutes".padStart(2, '0'))
        .append(":")
        .append("$seconds".padStart(2, '0'))
        .toString()
}

fun targetTimeToSeconds(target: SnapshotStateList<Int>): Int {
    var result = 0
    // Convert the values in to a stack for readable conversion
    val value = stackOf(target.toList())

    // Start with the two right most seconds values, we pop and transform individually because there
    // is no guarantee that these denominations come in pairs.
    result += 1 * (value.popOrNull() ?: return result)
    result += 10 * (value.popOrNull() ?: return result)

    // minutes values
    result += 1 * 60 * (value.popOrNull() ?: return result)
    result += 10 * 60 * (value.popOrNull() ?: return result)

    // hours values
    result += 1 * 3600 * (value.popOrNull() ?: return result)
    result += 10 * 3600 * (value.popOrNull() ?: return result)

    return result
}

fun formatTargetTime(timeInSeconds: SnapshotStateList<Int>): String {

    val output = timeInSeconds.joinToString(separator = "").padStart(6, '0')
        .chunked(2)

    return StringBuilder()
        .append("${output[0]}h ")
        .append("${output[1]}m ")
        .append("${output[2]}s")
        .toString()
}