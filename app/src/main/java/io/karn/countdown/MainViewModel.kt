package io.karn.countdown

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val themeConfig = MutableStateFlow(Pair(false, false))

    // Timer
    val remainingTime = MutableStateFlow(0)
    val isPaused = MutableStateFlow(false)

    var onTimerStartRequest: (Int) -> Unit = {}
    var onTimerPauseRequest: () -> Unit = {}
    var onTimerResumeRequest: () -> Unit = {}
    var onTimerResetRequest: (Int) -> Unit = {}
    var onTimerDeleteRequest: () -> Unit = {}
}