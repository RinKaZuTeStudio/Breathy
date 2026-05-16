package com.breathy.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages AdMob app-open and interstitial ads for Breathy.
 *
 * App-open ads are shown on cold start; interstitial ads are shown
 * after specific high-engagement transitions (e.g., after posting a story,
 * after completing a craving exercise). Premium subscribers are exempt.
 */
@Singleton
class AdManager @Inject constructor(
    private val context: Context
) {
    private var appOpenAd: AppOpenAd? = null
    private var interstitialAd: InterstitialAd? = null
    private var isAppOpenAdLoading = false
    private var isInterstitialLoading = false
    private var appOpenAdLoadTime: Long = 0L

    var isPremiumUser: Boolean = false

    companion object {
        // Replace with real Ad Unit IDs from AdMob console before release
        private const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-3940256099942544/9257395921" // Test ID
        private const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712" // Test ID
        private const val AD_LOAD_TIMEOUT_MS = 3000L
        private const val AD_CACHE_DURATION_MS = 4 * 60 * 60 * 1000L // 4 hours
    }

    /**
     * Pre-load an app-open ad. Should be called during app startup
     * so the ad is ready when the user reaches the home screen.
     */
    fun loadAppOpenAd() {
        if (isPremiumUser) return
        if (isAppOpenAdLoading || isAppOpenAdAvailable()) return

        isAppOpenAdLoading = true
        AppOpenAd.load(
            context,
            APP_OPEN_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    appOpenAdLoadTime = Date().time
                    isAppOpenAdLoading = false
                    Timber.d("App open ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenAdLoading = false
                    Timber.w("App open ad failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Show the app-open ad if one is available. Calls [onAdDismissed]
     * whether the ad was shown or skipped (error, not loaded, premium, etc.),
     * so the caller can always proceed to the next screen.
     */
    fun showAppOpenAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (isPremiumUser) {
            onAdDismissed()
            return
        }

        if (!isAppOpenAdAvailable()) {
            Timber.d("App open ad not available, proceeding")
            onAdDismissed()
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                loadAppOpenAd() // Pre-load next ad
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                loadAppOpenAd()
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Timber.d("App open ad showed successfully")
            }
        }

        appOpenAd?.show(activity) ?: run {
            onAdDismissed()
        }
    }

    /**
     * Load an interstitial ad. Should be called well before the moment
     * you intend to show it (e.g., when a user starts a story post).
     */
    fun loadInterstitialAd() {
        if (isPremiumUser) return
        if (isInterstitialLoading || interstitialAd != null) return

        isInterstitialLoading = true
        InterstitialAd.load(
            context,
            INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Timber.d("Interstitial ad loaded successfully")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Timber.w("Interstitial ad failed to load: ${error.message}")
                }
            }
        )
    }

    /**
     * Show an interstitial ad if available. Calls [onAdDismissed]
     * whether the ad was shown or skipped.
     */
    fun showInterstitialAd(activity: Activity, onAdDismissed: () -> Unit) {
        if (isPremiumUser) {
            onAdDismissed()
            return
        }

        if (interstitialAd == null) {
            Timber.d("Interstitial ad not available, proceeding")
            onAdDismissed()
            return
        }

        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitialAd = null
                loadInterstitialAd() // Pre-load next ad
                onAdDismissed()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitialAd = null
                loadInterstitialAd()
                onAdDismissed()
            }

            override fun onAdShowedFullScreenContent() {
                Timber.d("Interstitial ad showed successfully")
            }
        }

        interstitialAd?.show(activity) ?: run {
            onAdDismissed()
        }
    }

    /**
     * Check if the cached app-open ad is still valid.
     * Ads expire after [AD_CACHE_DURATION_MS] to avoid showing stale creative.
     */
    private fun isAppOpenAdAvailable(): Boolean {
        return appOpenAd != null && (Date().time - appOpenAdLoadTime) < AD_CACHE_DURATION_MS
    }
}
