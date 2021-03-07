/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.karn.countdown.ui.layout

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import io.karn.countdown.MainViewModel
import io.karn.countdown.R
import io.karn.countdown.ext.popOrNull
import io.karn.countdown.ext.stackOf
import io.karn.countdown.util.formatSeconds

// Null indicates a separator
val DIGITS = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, null, 0, null)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainLayout(navController: NavHostController, viewModel: MainViewModel) {
    val secondsRemaining = viewModel.remainingTime.collectAsState()
    val isPaused = viewModel.isPaused.collectAsState()

    val isEditing = remember { mutableStateOf(false) }

    val targetTime = remember { mutableStateListOf<Int>(*viewModel.targetTime.toTypedArray()) }

    // TODO: These animations start immediately with the composable, we can optimize when this is
    // created.
    val infiniteTransition = rememberInfiniteTransition()
    val pausedTextAlphaAnimation by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF12C2E9),
        targetValue = Color(0xFFC471ED),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 200, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFFF64F59),
        targetValue = Color(0xFF9F438C),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, delayMillis = 230, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val backgroundAlpha: Float by animateFloatAsState(
        if (isEditing.value || (secondsRemaining.value == 0)) 0f else 1f,
        animationSpec = tween(2500)
    )

    Column(
        modifier = Modifier.fillMaxSize()
            .background(
                Brush.linearGradient(Pair(0f, color1), Pair(400f, color2)),
                alpha = backgroundAlpha
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
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

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                alpha = if (!isEditing.value || targetTime.size > 0) {
                                    if (isPaused.value) pausedTextAlphaAnimation else 1f
                                } else {
                                    0.5f
                                }
                            }
                            .clickable(
                                enabled = !isEditing.value && secondsRemaining.value == 0,
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(
                                    bounded = false,
                                    radius = 100.dp,
                                    color = MaterialTheme.colors.primary.copy(alpha = 0.5f)
                                )
                            ) {
                                isEditing.value = !isEditing.value
                            },
                        text = if (isEditing.value) formatTargetTime(targetTime) else formatSeconds(
                            secondsRemaining.value
                        ),
                        textAlign = TextAlign.Center,
                        style = if (isEditing.value) MaterialTheme.typography.h4 else MaterialTheme.typography.h2,
                    )
                    if (!isEditing.value && secondsRemaining.value > 0) {
                        Text(
                            text = "ADD 60s TO COUNTDOWN",
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.onTimerAddSecondsRequest(60)
                                },
                            color = MaterialTheme.colors.primary,
                            style = MaterialTheme.typography.subtitle1,
                        )
                    }
                }
            }

            if (isEditing.value) {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .clickable(enabled = targetTime.size > 0) {
                            targetTime.removeAt(targetTime.size - 1)
                        },
                    painter = painterResource(R.drawable.ic_backspace),
                    tint = MaterialTheme.colors.onBackground,
                    contentDescription = "clear last digit"
                )
            }
        }

        if (isEditing.value) {
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
                                        interactionSource = remember { MutableInteractionSource() },
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

        Row(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            if (!isEditing.value) {
                Text(
                    text = "DELETE",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .padding(16.dp)
                        .clickable {
                            viewModel.onTimerDeleteRequest()
                        },
                    style = MaterialTheme.typography.subtitle1,
                )
            }

            Button(
                modifier = Modifier.padding(24.dp).animateContentSize(),
                shape = CircleShape,
                // TODO: Handle pause and unpause
                enabled = if (isEditing.value) targetTime.size > 0 else secondsRemaining.value > 0,
                elevation = ButtonDefaults.elevation(0.dp, 0.dp, 0.dp),
                onClick = {
                    // Pause and unpause the timer
                    if (isEditing.value) {
                        viewModel.onTimerStartRequest(targetTimeToSeconds(targetTime))
                        viewModel.targetTime = targetTime.toList()
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
                Text(
                    text = if (isEditing.value || secondsRemaining.value == 0) "START" else (if (isPaused.value) "RESUME" else "PAUSE"),
                    style = MaterialTheme.typography.subtitle1
                )
            }

            if (!isEditing.value) {
                Text(
                    text = "RESET",
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .align(Alignment.CenterVertically)
                        .padding(16.dp)
                        .clickable {
                            if (targetTime.size > 0) {
                                // Reset the state by clearing cancelling the timer
                                viewModel.onTimerResetRequest(targetTimeToSeconds(targetTime))
                                isEditing.value = false
                            }
                        },
                    style = MaterialTheme.typography.subtitle1,
                )
            }
        }
    }
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
