package com.retrivedmods.luxclient.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.retrivedmods.luxclient.navigation.Navigation
import com.retrivedmods.luxclient.ui.component.LoadingScreen
import com.retrivedmods.luxclient.ui.theme.MuCuteClientTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            MuCuteClientTheme {
                var isLoading by remember { mutableStateOf(true) }

                if (isLoading) {
                    LoadingScreen(
                        onDone = {
                            isLoading = false
                        }
                    )
                } else {
                    Navigation()
                }
            }
        }
    }
}
