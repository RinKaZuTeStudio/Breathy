package com.breathy.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.breathy.ui.theme.AccentPrimary
import com.breathy.ui.theme.BgSurface
import com.breathy.ui.theme.TextDisabled
import com.breathy.ui.theme.TextPrimary

// ═══════════════════════════════════════════════════════════════════════════════
// Route Constants — Type-safe navigation destinations
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Centralized route definitions for the entire navigation graph.
 *
 * Routes with path parameters use the `{paramName}` syntax required by
 * Navigation Compose. Helper functions provide type-safe construction of
 * parameterized routes at call sites.
 */
object BreathyRoutes {

    // ── Auth & Onboarding ───────────────────────────────────────────────────
    const val AUTH = "auth"
    const val ONBOARDING = "onboarding"

    // ── Main Screens (bottom bar destinations) ──────────────────────────────
    const val HOME = "home"
    const val COMMUNITY = "community"
    const val EVENTS = "events"
    const val LEADERBOARD = "leaderboard"
    const val PROFILE = "profile"

    // ── Detail / Secondary Screens ──────────────────────────────────────────
    const val STORY_DETAIL = "storyDetail/{storyId}"
    const val POST_STORY = "postStory"
    const val PUBLIC_PROFILE = "publicProfile/{userId}"
    const val FRIENDS = "friends"
    const val CHAT = "chat/{chatId}"
    const val EVENT_CHALLENGE = "eventChallenge/{eventId}"
    const val EVENT_CHECKIN = "eventCheckin/{eventId}"
    const val ADMIN_REVIEW = "adminReview"
    const val AI_COACH = "aiCoach"
    const val ACHIEVEMENTS = "achievements"
    const val SUBSCRIPTION = "subscription"

    // ── Helper functions for parameterized routes ───────────────────────────

    /** Build a route to the story detail screen for [storyId]. */
    fun storyDetail(storyId: String): String = "storyDetail/$storyId"

    /** Build a route to the public profile screen for [userId]. */
    fun publicProfile(userId: String): String = "publicProfile/$userId"

    /** Build a route to the chat screen for [chatId]. */
    fun chat(chatId: String): String = "chat/$chatId"

    /** Build a route to the event challenge screen for [eventId]. */
    fun eventChallenge(eventId: String): String = "eventChallenge/$eventId"

    /** Build a route to the event check-in screen for [eventId]. */
    fun eventCheckin(eventId: String): String = "eventCheckin/$eventId"

    // ── Route pattern matching ──────────────────────────────────────────────

    /**
     * Extract the base route name (without parameters) from a full route.
     * Useful for comparing the current destination against a set of known routes.
     */
    fun baseRoute(route: String?): String? {
        if (route == null) return null
        return route.substringBefore("/")
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Bottom Navigation Configuration
// ═══════════════════════════════════════════════════════════════════════════════

private data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(BreathyRoutes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(BreathyRoutes.COMMUNITY, "Community", Icons.Filled.People, Icons.Outlined.People),
    BottomNavItem(BreathyRoutes.EVENTS, "Events", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    BottomNavItem(BreathyRoutes.LEADERBOARD, "Leaderboard", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    BottomNavItem(BreathyRoutes.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

/**
 * Set of route patterns that should NOT display the bottom navigation bar.
 * Typically full-screen overlays, auth flows, or detail screens.
 */
private val noBottomBarRoutes = setOf(
    BreathyRoutes.AUTH,
    BreathyRoutes.ONBOARDING,
    BreathyRoutes.STORY_DETAIL,
    BreathyRoutes.POST_STORY,
    BreathyRoutes.CHAT,
    BreathyRoutes.EVENT_CHECKIN,
    BreathyRoutes.ADMIN_REVIEW,
    BreathyRoutes.AI_COACH,
    BreathyRoutes.SUBSCRIPTION,
    BreathyRoutes.ACHIEVEMENTS
)

// ═══════════════════════════════════════════════════════════════════════════════
// Animation Specs
// ═══════════════════════════════════════════════════════════════════════════════

private const val ANIM_DURATION_MS = 250

/** Forward (push) enter transition — slide in from right + fade. */
private val enterTransition: EnterTransition = fadeIn(
    animationSpec = tween(ANIM_DURATION_MS)
) + AnimatedContentTransitionScope.SlideDirection.Start.tween(ANIM_DURATION_MS)

/** Forward (push) exit transition — fade out. */
private val exitTransition: ExitTransition = fadeOut(
    animationSpec = tween(ANIM_DURATION_MS)
)

/** Backward (pop) enter transition — fade in. */
private val popEnterTransition: EnterTransition = fadeIn(
    animationSpec = tween(ANIM_DURATION_MS)
)

/** Backward (pop) exit transition — slide out to right + fade. */
private val popExitTransition: ExitTransition = fadeOut(
    animationSpec = tween(ANIM_DURATION_MS)
) + AnimatedContentTransitionScope.SlideDirection.End.tween(ANIM_DURATION_MS)

// ═══════════════════════════════════════════════════════════════════════════════
// Main Navigation Host
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Root composable that hosts the entire Breathy navigation graph.
 *
 * @param deepLinkRoute     A route string pushed from push-notification extras
 *                          or URI deep links. Consumed after navigation.
 * @param onDeepLinkConsumed Callback invoked after the deep-link route has
 *                          been consumed so the caller can clear its state.
 * @param modifier          Optional modifier applied to the root Scaffold.
 */
@Composable
fun BreathyNavHost(
    deepLinkRoute: String?,
    onDeepLinkConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // ── Deep link routing ───────────────────────────────────────────────────
    LaunchedEffect(deepLinkRoute) {
        if (deepLinkRoute != null) {
            try {
                navController.navigate(deepLinkRoute) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            } catch (e: IllegalArgumentException) {
                // Route not found in graph — fall back to home
                navController.navigate(BreathyRoutes.HOME) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
            onDeepLinkConsumed()
        }
    }

    // ── Bottom bar visibility ───────────────────────────────────────────────
    val showBottomBar = currentDestination?.route != null &&
            currentDestination?.route !in noBottomBarRoutes

    // ── Scaffold with conditional bottom bar ────────────────────────────────
    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                BreathyBottomBar(
                    currentDestination = currentDestination,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BreathyRoutes.AUTH,
            modifier = Modifier.padding(innerPadding),
            enterTransition = enterTransition,
            exitTransition = exitTransition,
            popEnterTransition = popEnterTransition,
            popExitTransition = popExitTransition
        ) {
            // ── Auth ────────────────────────────────────────────────────
            composable(BreathyRoutes.AUTH) {
                // TODO: Replace with AuthScreen(navController)
                PlaceholderScreen("Auth")
            }

            // ── Onboarding ──────────────────────────────────────────────
            composable(BreathyRoutes.ONBOARDING) {
                // TODO: Replace with OnboardingScreen(navController)
                PlaceholderScreen("Onboarding")
            }

            // ── Home ────────────────────────────────────────────────────
            composable(BreathyRoutes.HOME) {
                // TODO: Replace with HomeScreen(navController)
                PlaceholderScreen("Home")
            }

            // ── Community ───────────────────────────────────────────────
            composable(BreathyRoutes.COMMUNITY) {
                // TODO: Replace with CommunityScreen(navController)
                PlaceholderScreen("Community")
            }

            // ── Story Detail ────────────────────────────────────────────
            composable(
                route = BreathyRoutes.STORY_DETAIL,
                arguments = listOf(
                    navArgument("storyId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val storyId = backStackEntry.arguments?.getString("storyId")
                    ?: return@composable
                // TODO: Replace with StoryDetailScreen(storyId, navController)
                PlaceholderScreen("Story Detail: $storyId")
            }

            // ── Post Story ──────────────────────────────────────────────
            composable(BreathyRoutes.POST_STORY) {
                // TODO: Replace with PostStoryScreen(navController)
                PlaceholderScreen("Post Story")
            }

            // ── Public Profile ──────────────────────────────────────────
            composable(
                route = BreathyRoutes.PUBLIC_PROFILE,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                    ?: return@composable
                // TODO: Replace with PublicProfileScreen(userId, navController)
                PlaceholderScreen("Public Profile: $userId")
            }

            // ── Friends ─────────────────────────────────────────────────
            composable(BreathyRoutes.FRIENDS) {
                // TODO: Replace with FriendsScreen(navController)
                PlaceholderScreen("Friends")
            }

            // ── Chat ────────────────────────────────────────────────────
            composable(
                route = BreathyRoutes.CHAT,
                arguments = listOf(
                    navArgument("chatId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId")
                    ?: return@composable
                // TODO: Replace with ChatScreen(chatId, navController)
                PlaceholderScreen("Chat: $chatId")
            }

            // ── Leaderboard ─────────────────────────────────────────────
            composable(BreathyRoutes.LEADERBOARD) {
                // TODO: Replace with LeaderboardScreen(navController)
                PlaceholderScreen("Leaderboard")
            }

            // ── Events ──────────────────────────────────────────────────
            composable(BreathyRoutes.EVENTS) {
                // TODO: Replace with EventsScreen(navController)
                PlaceholderScreen("Events")
            }

            // ── Event Challenge Detail ──────────────────────────────────
            composable(
                route = BreathyRoutes.EVENT_CHALLENGE,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                    ?: return@composable
                // TODO: Replace with EventChallengeScreen(eventId, navController)
                PlaceholderScreen("Event Challenge: $eventId")
            }

            // ── Event Check-in ──────────────────────────────────────────
            composable(
                route = BreathyRoutes.EVENT_CHECKIN,
                arguments = listOf(
                    navArgument("eventId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId")
                    ?: return@composable
                // TODO: Replace with EventCheckinScreen(eventId, navController)
                PlaceholderScreen("Event Check-in: $eventId")
            }

            // ── Admin Review ────────────────────────────────────────────
            composable(BreathyRoutes.ADMIN_REVIEW) {
                // TODO: Replace with AdminReviewScreen(navController)
                PlaceholderScreen("Admin Review")
            }

            // ── AI Coach ────────────────────────────────────────────────
            composable(BreathyRoutes.AI_COACH) {
                // TODO: Replace with AiCoachScreen(navController)
                PlaceholderScreen("AI Coach")
            }

            // ── Profile ─────────────────────────────────────────────────
            composable(BreathyRoutes.PROFILE) {
                // TODO: Replace with ProfileScreen(navController)
                PlaceholderScreen("Profile")
            }

            // ── Achievements ────────────────────────────────────────────
            composable(BreathyRoutes.ACHIEVEMENTS) {
                // TODO: Replace with AchievementsScreen(navController)
                PlaceholderScreen("Achievements")
            }

            // ── Subscription ────────────────────────────────────────────
            composable(BreathyRoutes.SUBSCRIPTION) {
                // TODO: Replace with SubscriptionScreen(navController)
                PlaceholderScreen("Subscription")
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Bottom Navigation Bar
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Material 3 bottom navigation bar for Breathy's main destinations.
 *
 * @param currentDestination The current nav destination used to determine
 *                           which item is selected.
 * @param onNavigate         Callback invoked with the route of the tapped item.
 */
@Composable
private fun BreathyBottomBar(
    currentDestination: androidx.navigation.NavDestination?,
    onNavigate: (String) -> Unit
) {
    NavigationBar(
        containerColor = BgSurface,
        contentColor = TextPrimary
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.route == item.route
            } == true

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = if (isSelected) AccentPrimary else TextDisabled
                    )
                },
                label = {
                    if (isSelected) {
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            color = AccentPrimary
                        )
                    }
                },
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = AccentPrimary.copy(alpha = 0.12f),
                    selectedIconColor = AccentPrimary,
                    unselectedIconColor = TextDisabled
                )
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// Placeholder Screen (temporary — replaced by real screen composables)
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Temporary placeholder composable used until real screen implementations
 * are wired in. Displays the screen name centered on screen.
 */
@Composable
private fun PlaceholderScreen(name: String) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 24.sp
            )
        }
    }
}
