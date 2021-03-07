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
package io.karn.countdown

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import io.karn.countdown.services.TimerService
import io.karn.countdown.ui.layout.MainLayout
import io.karn.countdown.ui.layout.SettingsLayout
import io.karn.countdown.ui.theme.MyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    private lateinit var timerService: TimerService
    private var isServiceBound: Boolean = false

    /** Defines callbacks for service binding, passed to bindService()  */
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as TimerService.ServiceBinder
            timerService = binder.getService()
            isServiceBound = true

            // Attach handlers
            viewModel.onTimerStartRequest = { seconds ->
                startService(Intent(this@MainActivity, TimerService::class.java))
                timerService.startTimer(seconds)
            }

            viewModel.onTimerPauseRequest = {
                timerService.pauseTimer()
            }

            viewModel.onTimerResumeRequest = {
                timerService.resumeTimer()
            }

            // Start collecting the values and updating the ViewModel.
            // TODO: We can hack together a ViewModel to be scoped to the application which will
            // allow us to share said ViewModel between the UI and the Service.
            CoroutineScope(Dispatchers.IO).apply {
                launch {
                    timerService.remainingTime.collect { seconds ->
                        viewModel.remainingTime.value = seconds
                    }
                }

                launch {
                    timerService.isPaused.collect { isRunning ->
                        viewModel.isPaused.value = isRunning
                    }
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            isServiceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This app draws behind the system bars, so we want to handle fitting system windows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val scope = rememberCoroutineScope()
            val themeConfig = viewModel.themeConfig.collectAsState(context = scope.coroutineContext)

            ProvideWindowInsets {
                val (useSystemSettings, darkMode) = themeConfig.value

                MyTheme(if (useSystemSettings) isSystemInDarkTheme() else darkMode) {
                    MyApp(viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Bind to the service and await
        bindService(Intent(this, TimerService::class.java), connection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        super.onStop()

        if (isServiceBound) {
            unbindService(connection)
            isServiceBound = false
        }
    }
}

// Start building your app here!
@Composable
fun MyApp(viewModel: MainViewModel) {
    val navController = rememberNavController()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        NavHost(navController, startDestination = "main") {
            // The main view
            composable("main") {
                MainLayout(navController, viewModel)
            }

            // Settings
            composable("settings") {
                SettingsLayout(navController, viewModel)
            }
        }
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp(MainViewModel(Application()))
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        val viewModel = MainViewModel(Application()).also {
            it.themeConfig.value = Pair(false, true)
        }

        MyApp(viewModel)
    }
}
