package io.karn.countdown

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val themeConfig = MutableStateFlow(Pair(false, false))

    // Timer
    val remainingTime = MutableStateFlow(0)

    var onTimerStartRequest: (Int) -> Unit = {}
}