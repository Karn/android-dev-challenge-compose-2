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
package io.karn.countdown.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerService : Service() {

    // [SO] Binder
    inner class ServiceBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): TimerService = this@TimerService
    }

    // Create instance of the binder on initialization
    private val binder = ServiceBinder()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }
    // [EO] Binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(
            "TAG",
            "onStartCommand() called with: intent = $intent, flags = $flags, startId = $startId"
        )

        return super.onStartCommand(intent, flags, startId)
    }

    val remainingTime = MutableStateFlow(0)
    val isPaused = MutableStateFlow(false)
    private var currentJob: Job? = null

    fun startTimer(seconds: Int, startImmediately: Boolean = true) {
        // Cancel existing
        // Notify of cancel

        // Start new job
        remainingTime.value = seconds
        initTimer(startImmediately)
    }

    private fun initTimer(startImmediately: Boolean) {
        if (currentJob != null) {
            // Cancel the job to restart the counter
            currentJob?.cancel()
        }

        isPaused.value = !startImmediately
        currentJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive && remainingTime.value > 0) {
                delay(1000) // Every second
                // Decrement the seconds remaining
                if (!isPaused.value) {
                    remainingTime.value = remainingTime.value - 1
                }
            }
        }
    }

    fun pauseTimer() {
        isPaused.value = true
    }

    fun resumeTimer() {
        isPaused.value = false
    }

    fun addTimeToCurrent(seconds: Int): Boolean {
        currentJob ?: return false

        remainingTime.value = remainingTime.value + seconds

        return true
    }

    fun cancelCurrent(): Boolean {
        currentJob?.cancel() ?: return false

        // Update the remaining time to be 0
        remainingTime.value = 0
        isPaused.value = false
        currentJob = null

        return true
    }
}
