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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.breathy.di.AppModule
import com.breathy.ui.navigation.BreathyNavHost
import com.breathy.ui.theme.BreathyTheme
import com.breathy.ui.theme.ThemeMode
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

    /** Reactive theme mode — changes trigger recomposition of BreathyTheme. */
    private var themeMode by mutableStateOf(ThemeMode.SYSTEM)

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

        // Load saved theme preference
        val prefs = getSharedPreferences("breathy_prefs", MODE_PRIVATE)
        val themeName = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
        themeMode = try { ThemeMode.valueOf(themeName) } catch (_: Exception) { ThemeMode.SYSTEM }

        // Initialize AdMob SDK via AdManager (avoids double initialization)
        appModule.adManager.initialize()

        // Request notification permission on Android 13+
        requestNotificationPermissionIfNeeded()

        // Handle deep link from intent extras (e.g., from push notifications)
        handleDeepLinkFromIntent(intent)

        // Mark first launch complete so onResume can show app-open ads
        isFirstLaunch = false

        setContent {
            BreathyTheme(themeMode = themeMode) {
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

    override fun onResume() {
        super.onResume()

        // Re-read theme preference in case it was changed in ProfileScreen
        val prefs = getSharedPreferences("breathy_prefs", MODE_PRIVATE)
        val themeName = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM"
        themeMode = try { ThemeMode.valueOf(themeName) } catch (_: Exception) { ThemeMode.SYSTEM }

        // Show app-open ad only when coming back from background (not on first launch)
        // The AdManager handles frequency capping and premium exemption
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Only show on resume, not on initial create (onCreate handles that)
            if (!isFirstLaunch) {
                appModule.adManager.showAppOpenAd(this) {}
            }
        }
    }

    /** Track whether this is the first launch to avoid showing app-open ad twice */
    private var isFirstLaunch = true

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
