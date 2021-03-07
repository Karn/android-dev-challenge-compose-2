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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import io.karn.countdown.MainActivity
import io.karn.countdown.R
import io.karn.countdown.util.formatSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

const val TIMER_SERVICE_FOREGROUND_NOTIFICATION = 99

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
    private var isRunningAsForegroundService = false

    private val pendingIntent: PendingIntent by lazy {
        Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, 0, notificationIntent, 0)
        }
    }

    fun isTimerActive(): Boolean {
        return remainingTime.value > 0
    }

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
                // TODO: Use deltas to compute how much we actually want to delay
                delay(1000) // Every second
                // Decrement the seconds remaining
                if (!isPaused.value) {
                    Log.w("TAG", "Timer tick: ${remainingTime.value}")
                    remainingTime.value = remainingTime.value - 1
                }

                updateNotification()
            }

            // Remove the foreground notification when this is complete
            stopForForeground()
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

    fun startForForeground() {
        isRunningAsForegroundService = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createDefaultNotificationChannel()
        }

        // Notification ID cannot be 0.
        startForeground(
            TIMER_SERVICE_FOREGROUND_NOTIFICATION,
            getNotificationForSeconds(remainingTime.value)
        )
    }

    fun stopForForeground() {
        stopForeground(true)
        isRunningAsForegroundService = false
    }

    private fun updateNotification() {
        if (!isRunningAsForegroundService) {
            return
        }

        NotificationManagerCompat
            .from(this)
            .notify(
                TIMER_SERVICE_FOREGROUND_NOTIFICATION,
                getNotificationForSeconds(remainingTime.value)
            )
    }

    private fun getNotificationForSeconds(seconds: Int): Notification {
        // TODO: Add notification actions to delete, pause/resume, reset the timer
        return NotificationCompat.Builder(this, "default")
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_timer_24px)
            .setContentTitle(formatSeconds(seconds)) // Remaining Seconds
            .setContentText(getText((if (isPaused.value) R.string.notification_message_paused else R.string.notification_message_active)))
            .setCategory(Notification.CATEGORY_SERVICE)
            .setColor(Color.DarkGray.toArgb())
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createDefaultNotificationChannel() {
        val channel = NotificationChannel(
            "default",
            "Default notification channel",
            NotificationManager.IMPORTANCE_DEFAULT
        ).also {
            it.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        }
        val service = getSystemService<NotificationManager>()!!
        service.createNotificationChannel(channel)
    }
}
