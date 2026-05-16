package com.breathy

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.breathy.di.AppModule
import com.breathy.ui.navigation.BreathyNavHost
import com.breathy.ui.theme.BreathyTheme
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Single-activity host for the Breathy application.
 *
 * Responsibilities:
 * - Initializes AdMob SDK off the main thread
 * - Requests POST_NOTIFICATIONS permission on Android 13+
 * - Handles deep links from push notifications and URI-based intents
 * - Provides the Compose navigation host wrapped in [BreathyTheme]
 */
class MainActivity : ComponentActivity() {

    private var deepLinkRoute by mutableStateOf<String?>(null)

    /** Lazy reference to the app-scoped [AppModule] for manual DI. */
    private val appModule: AppModule by lazy {
        (application as BreathyApplication).appModule
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Timber.d("Notification permission granted")
        } else {
            Timber.w("Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize AdMob SDK off main thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                MobileAds.initialize(this@MainActivity) { initializationStatus ->
                    Timber.d("AdMob initialized: ${initializationStatus.adapterStatusMap}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to initialize AdMob")
            }
        }

        // Request notification permission on Android 13+
        requestNotificationPermissionIfNeeded()

        // Handle deep link from intent extras (e.g., from push notifications)
        handleDeepLinkFromIntent(intent)

        setContent {
            // Breathy uses a dark-only theme; BreathyTheme always applies
            // the dark color scheme internally.
            BreathyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    BreathyNavHost(
                        deepLinkRoute = deepLinkRoute,
                        onDeepLinkConsumed = { deepLinkRoute = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLinkFromIntent(intent)
    }

    // ── Deep Link Handling ──────────────────────────────────────────────────

    /**
     * Extracts the navigation route from intent extras or data URI.
     *
     * Push notifications include a "route" extra that maps directly to a
     * navigation destination (e.g., "chat/uid123_uid456", "events").
     * Fallback chain: route → storyId → chatId → eventId → userId → URI path
     */
    private fun handleDeepLinkFromIntent(intent: Intent?) {
        if (intent == null) return

        // 1. Direct route string from FCM data payload
        val routeExtra = intent.getStringExtra("route")
        if (!routeExtra.isNullOrBlank()) {
            deepLinkRoute = routeExtra
            Timber.d("Deep link route from extras: $routeExtra")
            return
        }

        // 2. Story ID (story like/reply notifications)
        val storyId = intent.getStringExtra("storyId")
        if (!storyId.isNullOrBlank()) {
            deepLinkRoute = "storyDetail/$storyId"
            Timber.d("Deep link route from storyId: storyDetail/$storyId")
            return
        }

        // 3. Chat ID (chat message notifications)
        val chatId = intent.getStringExtra("chatId")
        if (!chatId.isNullOrBlank()) {
            deepLinkRoute = "chat/$chatId"
            Timber.d("Deep link route from chatId: chat/$chatId")
            return
        }

        // 4. Event ID (event notifications)
        val eventId = intent.getStringExtra("eventId")
        if (!eventId.isNullOrBlank()) {
            deepLinkRoute = "eventChallenge/$eventId"
            Timber.d("Deep link route from eventId: eventChallenge/$eventId")
            return
        }

        // 5. User ID (friend request / profile notifications)
        val userId = intent.getStringExtra("userId")
        if (!userId.isNullOrBlank()) {
            deepLinkRoute = "publicProfile/$userId"
            Timber.d("Deep link route from userId: publicProfile/$userId")
            return
        }

        // 6. URI-based deep links (e.g., breathy://app/storyDetail/abc123)
        val uri = intent.data
        if (uri != null) {
            val path = uri.path
            if (!path.isNullOrBlank()) {
                deepLinkRoute = path.trimStart('/')
                Timber.d("Deep link route from URI: $deepLinkRoute")
            }
        }
    }

    // ── Permission Handling ─────────────────────────────────────────────────

    /**
     * Requests the POST_NOTIFICATIONS permission on Android 13 (API 33)+.
     * Required for the app to show push notifications on those versions.
     */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } catch (e: Exception) {
                Timber.e(e, "Failed to request notification permission")
            }
        }
    }
}
