package com.breathy

import android.app.Application
import com.breathy.di.AppModule
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import timber.log.Timber

/**
 * Application class for Breathy.
 *
 * Responsibilities:
 * - Initializes Firebase ([FirebaseApp.initializeApp])
 * - Configures Firestore offline persistence and cache size
 * - Enables/disables Crashlytics based on build type
 * - Plants Timber logging trees (debug tree or Crashlytics-forwarding tree)
 * - Creates the manual dependency injection [AppModule]
 */
class BreathyApplication : Application() {

    /**
     * App-scoped dependency container.
     * Lazily created on first access so that Firebase is fully initialized
     * before any Firebase service instances are obtained.
     */
    val appModule: AppModule by lazy {
        AppModule(this)
    }

    override fun onCreate() {
        super.onCreate()

        // ── Firebase Initialization ──────────────────────────────────────────
        try {
            FirebaseApp.initializeApp(this)
            Timber.d("Firebase initialized successfully")
        } catch (e: Exception) {
            // In rare cases (e.g., missing google-services.json in debug),
            // initialization may fail — log but don't crash.
            Timber.e(e, "Firebase initialization failed")
        }

        // ── Firestore Configuration ──────────────────────────────────────────
        configureFirestore()

        // ── Crashlytics ─────────────────────────────────────────────────────
        configureCrashlytics()

        // ── Timber Logging ───────────────────────────────────────────────────
        plantTimberTrees()

        // ── Notification Channels ────────────────────────────────────────────
        // Created eagerly so channels exist before any notification is posted.
        appModule.notificationHelper // triggers lazy init of AppModule → NotificationHelper
    }

    /**
     * Configures Firestore with offline persistence enabled and a 100 MB cache.
     *
     * Persistence allows the app to read previously fetched data when offline,
     * which is critical for a quit-smoking tracker that users may consult in
     * areas with poor connectivity.
     */
    private fun configureFirestore() {
        try {
            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .setCacheSizeBytes(FIRESTORE_CACHE_SIZE_BYTES)
                .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
            Timber.d("Firestore configured: persistence=true, cacheSize=%dMB",
                FIRESTORE_CACHE_SIZE_BYTES / (1024 * 1024))
        } catch (e: Exception) {
            Timber.e(e, "Failed to configure Firestore settings")
        }
    }

    /**
     * Enables Crashlytics collection only in release builds.
     * Debug builds avoid polluting the Crashlytics dashboard with stack traces
     * from development iterations.
     */
    private fun configureCrashlytics() {
        try {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(
                !BuildConfig.DEBUG
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to configure Crashlytics")
        }
    }

    /**
     * Plants the appropriate Timber tree based on the build type:
     * - **Debug**: [Timber.DebugTree] prints to Logcat with PrettyStackTree
     * - **Release**: [ReleaseCrashlyticsTree] forwards errors to Crashlytics
     */
    private fun plantTimberTrees() {
        if (BuildConfig.DEBUG) {
            Timber.plant(BreathyDebugTree())
        } else {
            Timber.plant(ReleaseCrashlyticsTree())
        }
    }

    // ── Custom Timber Trees ─────────────────────────────────────────────────

    /**
     * Debug tree that tags logs with the calling class name for easy
     * Logcat filtering.
     */
    private class BreathyDebugTree : Timber.DebugTree() {
        override fun createStackElementTag(element: StackTraceElement): String {
            // Format: "Breathy: ClassName.methodName:lineNumber"
            return "Breathy: ${super.createStackElementTag(element)}.${element.methodName}:${element.lineNumber}"
        }
    }

    /**
     * Release tree that forwards logged errors to Firebase Crashlytics.
     * Only errors and assertions are forwarded to avoid PII and noise.
     * Warnings and below are silently dropped in release builds.
     */
    private class ReleaseCrashlyticsTree : Timber.Tree() {
        override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
            if (priority >= android.util.Log.ERROR) {
                if (t != null) {
                    FirebaseCrashlytics.getInstance().recordException(t)
                } else {
                    FirebaseCrashlytics.getInstance().log("$tag: $message")
                }
            }
        }
    }

    companion object {
        /** 100 MB cache size for Firestore offline persistence. */
        private const val FIRESTORE_CACHE_SIZE_BYTES = 100L * 1024L * 1024L
    }
}
