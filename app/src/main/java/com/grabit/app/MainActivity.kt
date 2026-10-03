package com.grabit.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.grabit.app.data.Prefs
import com.grabit.app.engine.Engine
import com.grabit.app.service.DownloadService
import com.grabit.app.ui.AppRoot
import com.grabit.app.ui.theme.SoulTheme

/**
 * GrabIt entry point.
 *
 * Any uncaught error is recorded by [GrabItApp] and shown as a banner on the
 * next launch, so the app never just vanishes without an explanation.
 */
class MainActivity : ComponentActivity() {

    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        askNotifications()

        setContent {
            var accent by remember { mutableStateOf(Prefs.accent(this)) }

            SoulTheme(accentIndex = accent) {
                AppRoot(
                    accentIndex = accent,
                    onAccentChange = { i ->
                        accent = i
                        Prefs.setAccent(this, i)
                    },
                    onOpenBrowser = { url ->
                        startActivity(
                            Intent(this, BrowserActivity::class.java).putExtra("url", url)
                        )
                    },
                    onStartDownload = { url, quality -> startDownload(url, quality) }
                )
            }
        }
    }

    private fun startDownload(url: String, quality: Int) {
        askNotifications()
        DownloadService.start(this, url, quality)
        Toast.makeText(
            this,
            "Download started - " + Engine.qualityLabel(quality),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun askNotifications() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
