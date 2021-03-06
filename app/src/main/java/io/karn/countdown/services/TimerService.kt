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
    private var currentJob: Job? = null

    fun startTimer(seconds: Int) {
        // Cancel existing
        // Notify of cancel

        // Start new job
        remainingTime.value = seconds
        currentJob = CoroutineScope(Dispatchers.IO).launch {
            while (remainingTime.value > 0) {
                delay(1000) // Every second
                // Decrement the seconds remaining
                remainingTime.value = remainingTime.value - 1
            }

            // Stop the current service
            stopSelf()
        }
    }

    fun addTimeToCurrent(seconds: Int): Boolean {
        currentJob ?: return false

        remainingTime.value = remainingTime.value + seconds

        return true
    }

    fun cancelCurrent(): Boolean {
        currentJob?.cancel() ?: return false

        return true
    }
}