package com.breathy.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import nl.dionsegijn.konfetti.compose.OnParticleSystemUpdateListener
import nl.dionsegijn.konfetti.compose.ParticleSystem
import nl.dionsegijn.konfetti.compose.ParticleSystemManager
import nl.dionsegijn.konfetti.core.Angle
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.Spread
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import java.util.concurrent.TimeUnit

/**
 * Helper for creating confetti celebrations using the Konfetti library.
 *
 * Provides predefined confetti presets matching the Breathy app theme,
 * with neon green, gold, and blue accent colors. Each preset is tuned
 * for a specific celebration context:
 *
 * - [Preset.SMALL_BURST]  — Small celebration (craving defeated, daily reward)
 * - [Preset.BIG_CELEBRATION] — Large celebration (level up, streak milestone)
 * - [Preset.MILESTONE]    — Milestone reached (1 week, 1 month smoke-free)
 * - [Preset.ACHIEVEMENT]  — Achievement unlocked
 *
 * ## Compose Integration
 *
 * Use [BreathyConfetti] composable for easy integration:
 * ```
 * var showConfetti by remember { mutableStateOf(false) }
 *
 * Box {
 *     // Your content
 *     BreathyConfetti(
 *         preset = ConfettiHelper.Preset.ACHIEVEMENT,
 *         isActive = showConfetti,
 *         modifier = Modifier.fillMaxSize()
 *     )
 * }
 *
 * // Trigger
 * showConfetti = true
 * ```
 *
 * Or use the [ParticleSystemManager] directly with [ConfettiHelper.createParty]:
 * ```
 * val manager = remember { ParticleSystemManager() }
 *
 * ParticleSystem(
 *     systemManager = manager,
 *     modifier = Modifier.fillMaxSize()
 * )
 *
 * // Trigger confetti
 * val parties = ConfettiHelper.createParty(ConfettiHelper.Preset.BIG_CELEBRATION)
 * parties.forEach { manager.emit(it) }
 * ```
 */
object ConfettiHelper {

    // ═══════════════════════════════════════════════════════════════════════════
    //  Color Palettes — Matching Breathy App Theme
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Primary confetti palette — neon green, gold, blue accents.
     * Used for general celebrations and everyday achievements.
     */
    val PALETTE_PRIMARY = listOf(
        0xFF00E676.toInt(), // Neon Green  — AccentPrimary
        0xFFFFD740.toInt(), // Gold        — SemanticWarning
        0xFF448AFF.toInt()  // Neon Blue   — AccentSecondary
    )

    /**
     * Extended confetti palette — includes purple and pink accents.
     * Used for special celebrations (milestones, big achievements).
     */
    val PALETTE_EXTENDED = listOf(
        0xFF00E676.toInt(), // Neon Green  — AccentPrimary
        0xFFFFD740.toInt(), // Gold        — SemanticWarning
        0xFF448AFF.toInt(), // Neon Blue   — AccentSecondary
        0xFFB388FF.toInt(), // Neon Purple — AccentPurple
        0xFFFF4081.toInt(), // Neon Pink   — AccentPink
        0xFFFF9100.toInt()  // Neon Orange — AccentOrange
    )

    /**
     * Achievement confetti palette — gold and purple focus.
     * Matches the premium/achievement visual language.
     */
    val PALETTE_ACHIEVEMENT = listOf(
        0xFFFFD740.toInt(), // Gold
        0xFFFFD740.toInt(), // Gold (doubled for emphasis)
        0xFFB388FF.toInt(), // Neon Purple
        0xFF00E676.toInt()  // Neon Green
    )

    /**
     * Milestone confetti palette — green focus with gold highlights.
     * Matches the smoke-free milestone visual language.
     */
    val PALETTE_MILESTONE = listOf(
        0xFF00E676.toInt(), // Neon Green (emphasized)
        0xFF00E676.toInt(), // Neon Green (emphasized)
        0xFFFFD740.toInt(), // Gold
        0xFF448AFF.toInt()  // Neon Blue
    )

    // ═══════════════════════════════════════════════════════════════════════════
    //  Presets
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Confetti preset configurations for different celebration contexts.
     *
     * Each preset defines a list of [Party] objects that can be emitted
     * together for a multi-layered celebration effect.
     */
    enum class Preset(
        val displayName: String,
        val description: String,
        val durationMs: Long,
        val colors: List<Int>
    ) {
        /**
         * Small burst — quick celebration for minor events.
         * Examples: craving defeated, daily reward claimed.
         *
         * - 30 particles, 1.5s duration
         * - Center-out burst at 45° spread
         * - Primary palette
         */
        SMALL_BURST(
            displayName = "Small Burst",
            description = "Quick celebration for minor victories",
            durationMs = 1500L,
            colors = PALETTE_PRIMARY
        ),

        /**
         * Big celebration — large, multi-layer effect for major events.
         * Examples: level up, event challenge completed.
         *
         * - 80 particles total (2 layers), 3s duration
         * - Wide top-down shower + center burst
         * - Extended palette
         */
        BIG_CELEBRATION(
            displayName = "Big Celebration",
            description = "Large celebration for major accomplishments",
            durationMs = 3000L,
            colors = PALETTE_EXTENDED
        ),

        /**
         * Milestone — elegant celebration for smoke-free milestones.
         * Examples: 1 week, 1 month, 1 year smoke-free.
         *
         * - 60 particles, 4s duration
         * - Slow fountain from bottom-center
         * - Milestone palette (green + gold)
         */
        MILESTONE(
            displayName = "Milestone",
            description = "Elegant celebration for smoke-free milestones",
            durationMs = 4000L,
            colors = PALETTE_MILESTONE
        ),

        /**
         * Achievement — focused celebration for achievement unlocks.
         * Examples: "First Breath", "Craving Crusher" unlocked.
         *
         * - 50 particles, 2.5s duration
         * - Directed burst upward with spread
         * - Achievement palette (gold + purple)
         */
        ACHIEVEMENT(
            displayName = "Achievement",
            description = "Focused celebration for achievement unlocks",
            durationMs = 2500L,
            colors = PALETTE_ACHIEVEMENT
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Party Factory Methods
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create a list of [Party] objects for the given [preset].
     *
     * Each preset generates one or more Party objects that should be
     * emitted together for the full effect. The [sizeProvider] callback
     * supplies the container dimensions so the confetti can be positioned
     * relative to the viewport (e.g., center, top-center, bottom-center).
     *
     * @param preset       The confetti preset to create.
     * @param sizeProvider Function that returns (width, height) of the container
     *                     in pixels. Defaults to a zero-offset so confetti
     *                     originates from the center of the ParticleSystem.
     * @return A list of [Party] objects ready to be emitted.
     */
    fun createParty(
        preset: Preset,
        sizeProvider: () -> Pair<Int, Int> = { Pair(0, 0) }
    ): List<Party> {
        return when (preset) {
            Preset.SMALL_BURST -> createSmallBurst(sizeProvider)
            Preset.BIG_CELEBRATION -> createBigCelebration(sizeProvider)
            Preset.MILESTONE -> createMilestone(sizeProvider)
            Preset.ACHIEVEMENT -> createAchievement(sizeProvider)
        }
    }

    /**
     * Small burst — center-out burst with 45° spread.
     *
     * Configuration:
     * - Origin: center of viewport
     * - Angle: 0° (right) with SPREAD_45
     * - Speed: 15–30 dp/s
     * - 30 particles over 300ms
     * - Duration: 1.5s
     * - Sizes: small mixed rectangles
     */
    private fun createSmallBurst(sizeProvider: () -> Pair<Int, Int>): List<Party> {
        val (width, height) = sizeProvider()
        val centerX = width / 2
        val centerY = height / 2

        return listOf(
            Party(
                emitter = Emitter(duration = 300, TimeUnit.MILLISECONDS)
                    .perSecond(100),
                position = Position.Relative(0.5, 0.5),
                angle = Angle.RIGHT,
                spread = Spread.SPREAD_45,
                speed = 15f..30f,
                colors = Preset.SMALL_BURST.colors,
                timeToLive = 1500L,
                sizes = listOf(
                    Size(sizeInDp = 4, mass = 3f),
                    Size(sizeInDp = 6, mass = 5f),
                    Size(sizeInDp = 8, mass = 7f)
                ),
                rotation = nl.dionsegijn.konfetti.core.Rotation.Face(0f),
                fadeOutEnabled = true
            )
        )
    }

    /**
     * Big celebration — two-layer effect: top shower + center burst.
     *
     * Layer 1 (shower): Particles rain from the top-center, falling down
     * Layer 2 (burst): Particles burst outward from the center
     *
     * Total: ~80 particles over 2 seconds
     * Duration: 3s
     */
    private fun createBigCelebration(sizeProvider: () -> Pair<Int, Int>): List<Party> {
        return listOf(
            // Layer 1: Top-down shower
            Party(
                emitter = Emitter(duration = 2000, TimeUnit.MILLISECONDS)
                    .perSecond(30),
                position = Position.Relative(0.0, 0.0)
                    .between(Position.Relative(1.0, 0.0)),
                angle = Angle.BOTTOM,
                spread = Spread.SMALL_SPREAD,
                speed = 8f..18f,
                colors = Preset.BIG_CELEBRATION.colors,
                timeToLive = 3000L,
                sizes = listOf(
                    Size(sizeInDp = 4, mass = 3f),
                    Size(sizeInDp = 6, mass = 5f),
                    Size(sizeInDp = 9, mass = 8f)
                ),
                rotation = nl.dionsegijn.konfetti.core.Rotation.Face(0f),
                fadeOutEnabled = true,
                acceleration = nl.dionsegijn.konfetti.core.Acceleration(
                    x = 0f, y = 0.5f // Slight downward acceleration (gravity)
                )
            ),
            // Layer 2: Center burst
            Party(
                emitter = Emitter(duration = 500, TimeUnit.MILLISECONDS)
                    .perSecond(100),
                position = Position.Relative(0.5, 0.5),
                angle = Angle.RIGHT,
                spread = Spread.SPREAD_AROUND,
                speed = 20f..40f,
                colors = Preset.BIG_CELEBRATION.colors,
                timeToLive = 2500L,
                sizes = listOf(
                    Size(sizeInDp = 5, mass = 4f),
                    Size(sizeInDp = 8, mass = 6f),
                    Size(sizeInDp = 12, mass = 10f)
                ),
                rotation = nl.dionsegijn.konfetti.core.Rotation.Face(0f),
                fadeOutEnabled = true,
                acceleration = nl.dionsegijn.konfetti.core.Acceleration(
                    x = 0f, y = 0.3f // Gravity effect
                )
            )
        )
    }

    /**
     * Milestone — slow fountain from bottom-center.
     *
     * Particles emerge from the bottom-center of the viewport and arc
     * upward and outward, creating an elegant fountain effect.
     *
     * 60 particles over 3 seconds
     * Duration: 4s
     */
    private fun createMilestone(sizeProvider: () -> Pair<Int, Int>): List<Party> {
        return listOf(
            // Fountain — upward from bottom center
            Party(
                emitter = Emitter(duration = 3000, TimeUnit.MILLISECONDS)
                    .perSecond(20),
                position = Position.Relative(0.5, 1.0),
                angle = Angle.TOP,
                spread = Spread.SPREAD_60,
                speed = 15f..35f,
                colors = Preset.MILESTONE.colors,
                timeToLive = 4000L,
                sizes = listOf(
                    Size(sizeInDp = 5, mass = 4f),
                    Size(sizeInDp = 7, mass = 6f),
                    Size(sizeInDp = 10, mass = 8f)
                ),
                rotation = nl.dionsegijn.konfetti.core.Rotation.Face(0f),
                fadeOutEnabled = true,
                acceleration = nl.dionsegijn.konfetti.core.Acceleration(
                    x = 0f, y = -0.5f // Upward then gravity pulls down
                )
            )
        )
    }

    /**
     * Achievement — directed burst upward with spread from bottom-third.
     *
     * Particles shoot upward from the bottom third of the viewport,
     * creating a focused celebration effect.
     *
     * 50 particles over 800ms
     * Duration: 2.5s
     */
    private fun createAchievement(sizeProvider: () -> Pair<Int, Int>): List<Party> {
        return listOf(
            // Upward burst
            Party(
                emitter = Emitter(duration = 800, TimeUnit.MILLISECONDS)
                    .perSecond(60),
                position = Position.Relative(0.5, 0.7),
                angle = Angle.TOP,
                spread = Spread.SPREAD_45,
                speed = 18f..32f,
                colors = Preset.ACHIEVEMENT.colors,
                timeToLive = 2500L,
                sizes = listOf(
                    Size(sizeInDp = 5, mass = 4f),
                    Size(sizeInDp = 8, mass = 6f),
                    Size(sizeInDp = 11, mass = 9f)
                ),
                rotation = nl.dionsegijn.konfetti.core.Rotation.Face(0f),
                fadeOutEnabled = true,
                acceleration = nl.dionsegijn.konfetti.core.Acceleration(
                    x = 0f, y = -0.4f // Upward then gravity
                )
            )
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    //  Custom Party Builders
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Create a custom party with the Breathy color palette.
     *
     * Use this when the predefined presets don't exactly fit your needs
     * but you still want to use the app's confetti color scheme.
     *
     * @param position       Particle origin position.
     * @param angle          Direction in degrees (0 = right, 90 = down, etc.).
     * @param spread         Spread angle from the direction.
     * @param speedMin       Minimum particle speed in dp/s.
     * @param speedMax       Maximum particle speed in dp/s.
     * @param durationMs     Emitter duration in milliseconds.
     * @param particlesPerSec Particles emitted per second.
     * @param timeToLiveMs   Particle lifetime in milliseconds.
     * @param colors         Color palette (defaults to [PALETTE_PRIMARY]).
     * @param gravity        Vertical acceleration (negative = upward push).
     * @return A configured [Party] object.
     */
    fun createCustomParty(
        position: Position = Position.Relative(0.5, 0.5),
        angle: Int = Angle.RIGHT,
        spread: Int = Spread.SPREAD_45,
        speedMin: Float = 10f,
        speedMax: Float = 25f,
        durationMs: Long = 1000L,
        particlesPerSec: Int = 50,
        timeToLiveMs: Long = 2000L,
        colors: List<Int> = PALETTE_PRIMARY,
        gravity: Float = 0.3f
    ): Party {
        return Party(
            emitter = Emitter(duration = durationMs, TimeUnit.MILLISECONDS)
                .perSecond(particlesPerSec),
            position = position,
            angle = angle,
            spread = spread,
            speed = speedMin..speedMax,
            colors = colors,
            timeToLive = timeToLiveMs,
            sizes = listOf(
                Size(sizeInDp = 5, mass = 4f),
                Size(sizeInDp = 8, mass = 6f),
                Size(sizeInDp = 11, mass = 9f)
            ),
            rotation = nl.dionsegijn.konfetti.core.Rotation.Face(0f),
            fadeOutEnabled = true,
            acceleration = nl.dionsegijn.konfetti.core.Acceleration(
                x = 0f, y = gravity
            )
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
//  Compose Integration
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Easy Compose integration for Breathy confetti effects.
 *
 * This composable wraps the Konfetti [ParticleSystem] and provides
 * a simple [isActive] flag to trigger confetti. When [isActive]
 * becomes `true`, the confetti preset is automatically emitted.
 *
 * The composable is transparent and overlays on top of your content,
 * so it should be placed in a [Box] scope above your content.
 *
 * ## Usage
 * ```
 * var showConfetti by remember { mutableStateOf(false) }
 *
 * Box(modifier = Modifier.fillMaxSize()) {
 *     // Your screen content
 *     MyScreenContent()
 *
 *     // Confetti overlay
 *     BreathyConfetti(
 *         preset = ConfettiHelper.Preset.SMALL_BURST,
 *         isActive = showConfetti,
 *         modifier = Modifier.fillMaxSize()
 *     )
 * }
 *
 * // Trigger confetti
 * scope.launch { showConfetti = true }
 * ```
 *
 * @param preset    The confetti preset to use when triggered.
 * @param isActive  When `true`, emits the confetti. Set to `false` to reset.
 * @param modifier  Modifier for positioning and sizing the confetti canvas.
 * @param onFinished Optional callback when the confetti animation completes.
 */
@Composable
fun BreathyConfetti(
    preset: ConfettiHelper.Preset,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onFinished: (() -> Unit)? = null
) {
    val manager = remember { ParticleSystemManager() }

    ParticleSystem(
        modifier = modifier,
        systemManager = manager,
        onUpdateListener = object : OnParticleSystemUpdateListener {
            override fun onParticleSystemEnded(systemId: Int, activeSystems: Int) {
                if (activeSystems == 0) {
                    onFinished?.invoke()
                }
            }
        }
    )

    // Emit confetti when isActive becomes true
    androidx.compose.runtime.LaunchedEffect(isActive) {
        if (isActive) {
            val parties = ConfettiHelper.createParty(preset)
            parties.forEach { party ->
                manager.emit(party)
            }
        }
    }
}

/**
 * Multi-preset confetti composable for complex celebrations.
 *
 * Use this when you want to trigger different preset combinations,
 * e.g., a milestone + achievement celebration at the same time.
 *
 * @param presets   List of presets to emit simultaneously.
 * @param isActive  When `true`, emits all preset confetti.
 * @param modifier  Modifier for positioning and sizing.
 * @param onFinished Optional callback when all confetti animations complete.
 */
@Composable
fun BreathyMultiConfetti(
    presets: List<ConfettiHelper.Preset>,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onFinished: (() -> Unit)? = null
) {
    val manager = remember { ParticleSystemManager() }

    ParticleSystem(
        modifier = modifier,
        systemManager = manager,
        onUpdateListener = object : OnParticleSystemUpdateListener {
            override fun onParticleSystemEnded(systemId: Int, activeSystems: Int) {
                if (activeSystems == 0) {
                    onFinished?.invoke()
                }
            }
        }
    )

    androidx.compose.runtime.LaunchedEffect(isActive) {
        if (isActive) {
            presets.forEach { preset ->
                val parties = ConfettiHelper.createParty(preset)
                parties.forEach { party ->
                    manager.emit(party)
                }
            }
        }
    }
}
