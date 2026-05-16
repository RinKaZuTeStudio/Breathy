package com.breathy.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
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

// ── Route Constants ────────────────────────────────────────────────────────

object BreathyRoutes {
    const val AUTH = "auth"
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val COMMUNITY = "community"
    const val STORY_DETAIL = "storyDetail/{storyId}"
    const val POST_STORY = "postStory"
    const val PUBLIC_PROFILE = "publicProfile/{userId}"
    const val FRIENDS = "friends"
    const val CHAT = "chat/{chatId}"
    const val LEADERBOARD = "leaderboard"
    const val EVENTS = "events"
    const val EVENT_CHALLENGE = "eventChallenge/{eventId}"
    const val EVENT_CHECKIN = "eventCheckin/{eventId}"
    const val ADMIN_REVIEW = "adminReview"
    const val AI_COACH = "aiCoach"
    const val PROFILE = "profile"
    const val SUBSCRIPTION = "subscription"
    const val ACHIEVEMENTS = "achievements"

    // Helper functions for routes with arguments
    fun storyDetail(storyId: String) = "storyDetail/$storyId"
    fun publicProfile(userId: String) = "publicProfile/$userId"
    fun chat(chatId: String) = "chat/$chatId"
    fun eventChallenge(eventId: String) = "eventChallenge/$eventId"
    fun eventCheckin(eventId: String) = "eventCheckin/$eventId"
}

// ── Bottom Navigation Items ────────────────────────────────────────────────

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

// ── Routes that should NOT show the bottom bar ─────────────────────────────

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

// ── Animation Specs ────────────────────────────────────────────────────────

private const val ANIM_DURATION_MS = 200

private val enterTransition: EnterTransition = fadeIn(
    animationSpec = tween(ANIM_DURATION_MS)
)

private val exitTransition: ExitTransition = fadeOut(
    animationSpec = tween(ANIM_DURATION_MS)
)

private val popEnterTransition: EnterTransition = fadeIn(
    animationSpec = tween(ANIM_DURATION_MS)
)

private val popExitTransition: ExitTransition = fadeOut(
    animationSpec = tween(ANIM_DURATION_MS)
)

// ── Main Nav Host Composable ───────────────────────────────────────────────

@Composable
fun BreathyNavHost(
    deepLinkRoute: String?,
    onDeepLinkConsumed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Handle deep link routing from notifications
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
                // Route not found in graph — navigate to home as fallback
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

    // Determine whether to show bottom bar
    val showBottomBar = currentDestination?.route !in noBottomBarRoutes &&
        currentDestination?.route != null

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
            // ── Auth ───────────────────────────────────────────────────
            composable(BreathyRoutes.AUTH) {
                // AuthScreen(navController = navController)
                // Placeholder composable — replaced with real AuthScreen
                PlaceholderScreen("Auth")
            }

            // ── Onboarding ─────────────────────────────────────────────
            composable(BreathyRoutes.ONBOARDING) {
                // OnboardingScreen(navController = navController)
                PlaceholderScreen("Onboarding")
            }

            // ── Home ───────────────────────────────────────────────────
            composable(BreathyRoutes.HOME) {
                // HomeScreen(navController = navController)
                PlaceholderScreen("Home")
            }

            // ── Community ──────────────────────────────────────────────
            composable(BreathyRoutes.COMMUNITY) {
                // CommunityScreen(navController = navController)
                PlaceholderScreen("Community")
            }

            // ── Story Detail ───────────────────────────────────────────
            composable(
                route = BreathyRoutes.STORY_DETAIL,
                arguments = listOf(navArgument("storyId") { type = NavType.StringType })
            ) { backStackEntry ->
                val storyId = backStackEntry.arguments?.getString("storyId") ?: return@composable
                // StoryDetailScreen(storyId = storyId, navController = navController)
                PlaceholderScreen("Story Detail: $storyId")
            }

            // ── Post Story ─────────────────────────────────────────────
            composable(BreathyRoutes.POST_STORY) {
                // PostStoryScreen(navController = navController)
                PlaceholderScreen("Post Story")
            }

            // ── Public Profile ─────────────────────────────────────────
            composable(
                route = BreathyRoutes.PUBLIC_PROFILE,
                arguments = listOf(navArgument("userId") { type = NavType.StringType })
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
                // PublicProfileScreen(userId = userId, navController = navController)
                PlaceholderScreen("Public Profile: $userId")
            }

            // ── Friends ────────────────────────────────────────────────
            composable(BreathyRoutes.FRIENDS) {
                // FriendsScreen(navController = navController)
                PlaceholderScreen("Friends")
            }

            // ── Chat ───────────────────────────────────────────────────
            composable(
                route = BreathyRoutes.CHAT,
                arguments = listOf(navArgument("chatId") { type = NavType.StringType })
            ) { backStackEntry ->
                val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
                // ChatScreen(chatId = chatId, navController = navController)
                PlaceholderScreen("Chat: $chatId")
            }

            // ── Leaderboard ────────────────────────────────────────────
            composable(BreathyRoutes.LEADERBOARD) {
                // LeaderboardScreen(navController = navController)
                PlaceholderScreen("Leaderboard")
            }

            // ── Events ─────────────────────────────────────────────────
            composable(BreathyRoutes.EVENTS) {
                // EventsScreen(navController = navController)
                PlaceholderScreen("Events")
            }

            // ── Event Challenge Detail ─────────────────────────────────
            composable(
                route = BreathyRoutes.EVENT_CHALLENGE,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                // EventChallengeScreen(eventId = eventId, navController = navController)
                PlaceholderScreen("Event Challenge: $eventId")
            }

            // ── Event Check-in ─────────────────────────────────────────
            composable(
                route = BreathyRoutes.EVENT_CHECKIN,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: return@composable
                // EventCheckinScreen(eventId = eventId, navController = navController)
                PlaceholderScreen("Event Check-in: $eventId")
            }

            // ── Admin Review ───────────────────────────────────────────
            composable(BreathyRoutes.ADMIN_REVIEW) {
                // AdminReviewScreen(navController = navController)
                PlaceholderScreen("Admin Review")
            }

            // ── AI Coach ───────────────────────────────────────────────
            composable(BreathyRoutes.AI_COACH) {
                // AiCoachScreen(navController = navController)
                PlaceholderScreen("AI Coach")
            }

            // ── Profile ────────────────────────────────────────────────
            composable(BreathyRoutes.PROFILE) {
                // ProfileScreen(navController = navController)
                PlaceholderScreen("Profile")
            }

            // ── Subscription ───────────────────────────────────────────
            composable(BreathyRoutes.SUBSCRIPTION) {
                // SubscriptionScreen(navController = navController)
                PlaceholderScreen("Subscription")
            }

            // ── Achievements ───────────────────────────────────────────
            composable(BreathyRoutes.ACHIEVEMENTS) {
                // AchievementsScreen(navController = navController)
                PlaceholderScreen("Achievements")
            }
        }
    }
}

// ── Bottom Navigation Bar ──────────────────────────────────────────────────

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

// ── Temporary Placeholder Screen (will be replaced by real screens) ─────────

@Composable
private fun PlaceholderScreen(name: String) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .then(androidx.compose.foundation.layout.fillMaxSize())
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.then(
                androidx.compose.foundation.layout.fillMaxSize()
            ),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                color = TextPrimary,
                fontSize = 24.sp
            )
        }
    }
}
