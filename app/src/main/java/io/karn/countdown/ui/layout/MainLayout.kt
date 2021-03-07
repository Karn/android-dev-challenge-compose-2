package io.karn.countdown.ui.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import io.karn.countdown.MainViewModel
import io.karn.countdown.ext.popOrNull
import io.karn.countdown.ext.stackOf

enum class CountDownState {
    TIMER,
    TIMER_CONFIG
}

// Null indicates a seperator
val DIGITS = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, null, 0, null)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainLayout(navController: NavHostController, viewModel: MainViewModel) {
    val secondsRemaining = viewModel.remainingTime.collectAsState()
    val isPaused = viewModel.isPaused.collectAsState()

    val currentState = remember { mutableStateOf(CountDownState.TIMER) }
    val isEditing = remember { mutableStateOf(false) }

    val targetTime = remember { mutableStateListOf<Int>() }


    Column(
        modifier = Modifier.fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
        ) {
            if (isEditing.value) {
                Spacer(
                    modifier = Modifier.weight(1f)
                )
            }

            Box(
                modifier = Modifier
                    .weight(10f)
                    .fillMaxSize()
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .alpha(if (!isEditing.value || targetTime.size > 0) 1f else 0.5f)
                        .clickable(
                            enabled = !isEditing.value,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(false, radius = 200.dp)
                        ) {
                            isEditing.value = !isEditing.value
                        },
                    text = if (isEditing.value) formatTargetTime(targetTime) else formatSeconds(
                        secondsRemaining.value
                    ),
                    textAlign = TextAlign.Center,
                    style = if (isEditing.value) MaterialTheme.typography.h4 else MaterialTheme.typography.h2
                )
            }

            if (isEditing.value) {
                IconButton(
                    // Less-efficient version of View.INVISIBLE
                    modifier = Modifier.weight(1f).align(Alignment.CenterVertically),
                    enabled = targetTime.size > 0,
                    onClick = {
                        targetTime.removeAt(targetTime.size - 1)
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "clear last digit"
                    )
                }
            }
        }

        AnimatedVisibility(
            modifier = Modifier.fillMaxHeight(0.5f),
            visible = isEditing.value
        ) {
            Column(modifier = Modifier.padding(vertical = 32.dp)) {
                DIGITS.chunked(3).forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                    ) {
                        rowItems.map { digit ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(
                                        enabled = digit != null,
                                        interactionSource = MutableInteractionSource(),
                                        indication = rememberRipple(
                                            bounded = false,
                                            radius = 46.dp,
                                            color = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                        ),
                                    ) {
                                        // There are six total items HHMMSS
                                        if (digit != null && targetTime.size < 6) {
                                            // Skip adding zero to the start of the list
                                            val isFirstDigitZero =
                                                digit == 0 && targetTime.size == 0
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
        }

        Row {
            Text(
                text = "Delete",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .clickable {

                    }
            )

            Button(
                modifier = Modifier.padding(24.dp),
                shape = CircleShape,
                // TODO: Handle pause and unpause
                enabled = if (isEditing.value) targetTime.size > 0 else secondsRemaining.value > 0,
                elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
                onClick = {
                    // Pause and unpause the timer
                    if (isEditing.value) {
                        viewModel.onTimerStartRequest(targetTimeToSeconds(targetTime))
                        isEditing.value = false
                    } else {
                        // Pause unpause
                        if (isPaused.value) {
                            viewModel.onTimerResumeRequest()
                        } else {
                            viewModel.onTimerPauseRequest()
                        }
                    }
                }
            ) {
                if (isEditing.value) {
                    Text(
                        text = "START",
                        style = MaterialTheme.typography.subtitle1
                    )
                } else {
                    Icon(
                        imageVector = if (secondsRemaining.value == 0 || isPaused.value) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = "play"
                    )
                }
            }

            Text(
                text = "Delete",
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .clickable {

                    },
            )
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