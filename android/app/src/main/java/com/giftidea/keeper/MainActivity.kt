package com.giftidea.keeper

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import com.giftidea.keeper.core.di.ServiceLocator
import com.giftidea.keeper.core.theme.AppTheme
import com.giftidea.keeper.presentation.app.AppRoot
import com.giftidea.keeper.presentation.app.HostEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class MainActivity : ComponentActivity() {
    private val hostChannel = Channel<HostEvent>(Channel.BUFFERED)
    val hostEvents = hostChannel.receiveAsFlow()
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { hostChannel.trySend(HostEvent.Refresh) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT), navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        ServiceLocator.initialize(applicationContext)
        if (savedInstanceState == null) hostChannel.trySend(HostEvent.Notification(intent.getStringExtra("person_id"), intent.getStringExtra("event_date")))
        setContent { AppTheme { AppRoot() } }
    }

    override fun onResume() {
        super.onResume()
        hostChannel.trySend(HostEvent.Refresh)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        hostChannel.trySend(HostEvent.Notification(intent.getStringExtra("person_id"), intent.getStringExtra("event_date")))
    }

    fun requestNotifications() {
        if (Build.VERSION.SDK_INT >= 33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        else hostChannel.trySend(HostEvent.Refresh)
    }

    fun openNotificationSettings() {
        startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, packageName))
    }
}
