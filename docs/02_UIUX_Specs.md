# Breathy — UI/UX Specifications

> **Version:** 1.0.0  
> **Platform:** Android (API 26+, Android 8.0 Oreo)  
> **Design Paradigm:** Dark Mode + Neon Accents  
> **Last Updated:** 2025-03-04  

---

## Table of Contents

1. [Design System](#design-system)
   - [Color Palette (Dark Mode + Neon Accents)](#color-palette-dark-mode--neon-accents)
   - [Typography](#typography)
   - [Spacing & Grid](#spacing--grid)
   - [Animation Specs](#animation-specs)
2. [Screen-by-Screen Wireframe Descriptions](#screen-by-screen-wireframe-descriptions)
3. [User Flow Diagrams](#user-flow-diagrams)

---

# Design System

The Breathy design system is built around a dark-mode-first philosophy that leverages deep, rich background tones punctuated by vivid neon accent colors. This approach serves both aesthetic and functional purposes: the dark canvas reduces eye strain during late-night craving sessions when users are most likely to open the app, while the neon accents draw immediate attention to actionable elements, progress indicators, and celebration moments. Every color, type choice, spacing rule, and animation has been calibrated to create a cohesive experience that feels modern and encouraging rather than clinical. The system draws subtle inspiration from gaming interfaces—progress bars, XP counters, achievement badges—because gamification is central to Breathy's retention strategy. Consistency across all screens is enforced through reusable design tokens defined below, which map directly to Android resource values (`colors.xml`, `dimens.xml`, `styles.xml`) and Compose theme extensions.

---

## Color Palette (Dark Mode + Neon Accents)

Breathy's color palette is intentionally divided into three tiers: **backgrounds**, **accents**, and **semantic** colors. Background colors establish depth and visual hierarchy without competing for attention. The deepest tone serves as the full-screen canvas, while surface and surface-variant elevate cards, sheets, and modals above that base. Accent colors are the "neon" elements—they should feel as though they are emitting light against the dark background. Each accent is assigned a specific semantic role to prevent color ambiguity: green always means progress or success, orange always signals a craving or warning, purple denotes premium or achievement, and pink is reserved for social affirmation. This strict mapping ensures that a user who sees a flash of orange instinctively knows it relates to a craving event, even before reading any text.

### Background Colors

| Token Name            | Hex Value  | Usage                                                          |
|-----------------------|------------|----------------------------------------------------------------|
| `bg_primary`          | `#0D0D1A`  | Full-screen background, behind all surfaces                    |
| `bg_surface`          | `#1A1A2E`  | Card backgrounds, bottom sheets, dialog surfaces               |
| `bg_surface_variant`  | `#252540`  | Elevated sub-surfaces: input fields, nested cards, dividers    |

The three-tier background system creates perceptible depth through luminance stepping rather than shadows. A card at `#1A1A2E` on a `#0D0D1A` base is immediately distinguishable; a nested element at `#252540` on that card creates a second layer. On OLED screens—which represent a large share of Android devices—pure black `#000000` is tempting, but `#0D0D1A` is chosen instead because it retains a subtle navy undertone that prevents the "floating card" illusion where surface edges disappear into the true-black bezels of OLED hardware. The slight warmth also reduces the harshness of neon accents placed directly on the background, creating a more comfortable viewing experience during extended sessions.

### Accent Colors

| Token Name              | Hex Value  | Role                                                                  |
|-------------------------|------------|-----------------------------------------------------------------------|
| `accent_primary`        | `#00E676`  | Primary Neon Green — progress bars, success states, CTA buttons       |
| `accent_secondary`      | `#448AFF`  | Secondary Neon Blue — links, secondary actions, informational badges  |
| `accent_purple`         | `#B388FF`  | Accent Neon Purple — premium features, achievement badges, XP         |
| `accent_orange`         | `#FF9100`  | Accent Neon Orange — cravings, warnings, urgency indicators           |
| `accent_pink`           | `#FF4081`  | Accent Neon Pink — likes, hearts, social affirmation                  |

Each neon accent is specified at full saturation to maximize its luminous quality against dark backgrounds. However, accessibility demands that these colors never appear as thin strokes or small text on dark surfaces without a luminance-boosting technique such as a subtle outer glow (`blurRadius: 4dp, color: accent at 30% opacity`) or a slightly larger font weight. For interactive elements, the pressed state shifts the accent 10% lighter (e.g., `#00E676` → `#33EB91`), while the disabled state drops opacity to 40%. The primary green (`#00E676`) was chosen specifically because it is the Material Design "Green A400," which achieves WCAG AA contrast against both `#0D0D1A` and `#1A1A2E` at text sizes ≥ 14sp bold, making it viable even for short label text, not just decorative fills.

### Text Colors

| Token Name          | Hex Value  | Usage                                                       |
|---------------------|------------|--------------------------------------------------------------|
| `text_primary`      | `#FFFFFF`  | Headlines, body text, high-emphasis labels                   |
| `text_secondary`    | `#B0B0CC`  | Subtitles, helper text, metadata, timestamps                 |
| `text_disabled`     | `#606080`  | Disabled states, placeholder text, inactive tabs             |

Text colors follow a three-level emphasis model that mirrors Material Design's opacity-based approach but uses fixed hex values for consistency across different background surfaces. Primary white (`#FFFFFF`) is reserved for content the user must read first—headlines, stat numbers, button labels. Secondary (`#B0B0CC`) carries a lavender tint that harmonizes with the navy-toned backgrounds rather than clashing with a neutral gray; this keeps the palette feeling unified. Disabled text (`#606080`) is dark enough to be clearly inactive yet light enough to remain legible if the user deliberately focuses on it, which matters for elements like expired event dates or grayed-out achievements that users may still want to read.

### Semantic Colors

| Token Name     | Hex Value  | Usage                                                            |
|----------------|------------|-------------------------------------------------------------------|
| `semantic_error`   | `#FF5252`  | Error states, form validation failures, destructive actions       |
| `semantic_success` | `#00E676`  | Success toasts, completed milestones (same as accent_primary)     |
| `semantic_warning` | `#FFD740`  | Warning banners, rate-limit notices, cautionary prompts           |

Semantic colors are deliberately aligned with the accent palette where possible—success is the same green as `accent_primary` to reinforce the association between the brand's primary color and positive outcomes. Error red (`#FF5252`) is the only color that does not double as an accent; it is reserved exclusively for negative states to prevent confusion. Warning yellow (`#FFD740`) is chosen over orange because the orange accent is already semantically bound to craving events; using a distinct yellow for warnings ensures the user can differentiate between "a craving is happening" (orange) and "something needs your attention" (yellow) at a glance.

### Gradients

| Name                      | Value                                                     | Usage                                  |
|---------------------------|-----------------------------------------------------------|----------------------------------------|
| `gradient_primary`        | `linear-gradient(45deg, #00E676, #448AFF)`               | CTA buttons, hero card accents         |
| `gradient_purple`         | `linear-gradient(135deg, #B388FF, #448AFF)`              | Premium feature cards, achievement bg  |
| `gradient_dark_overlay`   | `linear-gradient(180deg, transparent, #0D0D1A@80%)`      | Image overlays for readability         |
| `gradient_glow_green`     | `radial-gradient(circle, #00E676@30%, transparent)`      | Glow effect behind hero stats          |
| `gradient_glow_orange`    | `radial-gradient(circle, #FF9100@25%, transparent)`      | Pulsing glow behind craving button     |

The primary gradient (45° from green to blue) is the app's signature visual motif. It appears on the primary CTA button, the auth screen background, and as a thin accent line at the top of the hero stat card. The angle of 45° is deliberate—horizontal gradients feel static and corporate, while a 45° sweep introduces diagonal energy that subtly suggests forward momentum, reinforcing the quit-smoking narrative of moving forward. Glow gradients are implemented as `RadialGradient` drawables in Android, positioned behind key UI elements to simulate the neon "light bleed" effect. The green glow behind the "Days Smoke-Free" hero number, for instance, is a 120dp radial gradient centered on the number, which creates the illusion that the number itself is a light source illuminating the card surface.

---

## Typography

Typography in Breathy serves two masters: emotional resonance and data legibility. Headlines use Montserrat Bold because its geometric construction and wide x-height project confidence and clarity—critical for an app that needs to motivate users through text like "You've been smoke-free for 47 days!" Body text uses Inter Regular, which is optimized for screen rendering at small sizes and offers exceptional legibility even on low-DPI displays. The standout choice is Space Mono Bold for numeric statistics; monospaced fonts ensure that digits don't shift position as values change (e.g., when a counter increments from 99 to 100), which prevents layout jitter in animated counters and reinforces the "data dashboard" feel that makes progress tangible.

### Font Scale

| Style Token         | Font             | Weight   | Size   | Line Height | Letter Spacing | Usage                                  |
|---------------------|------------------|----------|--------|-------------|----------------|----------------------------------------|
| `headline_large`    | Montserrat       | Bold     | 24sp   | 32sp        | -0.5sp         | Screen titles, onboarding step titles  |
| `headline_medium`   | Montserrat       | Bold     | 20sp   | 28sp        | -0.25sp        | Section headers, card titles           |
| `headline_small`    | Montserrat       | Bold     | 18sp   | 24sp        | 0sp            | Sub-sections, bottom sheet headers     |
| `body_large`        | Inter            | Regular  | 16sp   | 24sp        | 0.15sp         | Primary body text, story content       |
| `body_medium`       | Inter            | Regular  | 14sp   | 20sp        | 0.25sp         | Secondary text, list items, captions   |
| `caption_large`     | Inter            | Light    | 12sp   | 16sp        | 0.4sp          | Timestamps, helper text, chip labels   |
| `caption_small`     | Inter            | Light    | 10sp   | 14sp        | 0.5sp          | Legal text, fine print, badge labels   |
| `stat_hero`         | Space Mono       | Bold     | 48sp   | 56sp        | -1sp           | Hero numbers (days smoke-free)         |
| `stat_card`         | Space Mono       | Bold     | 32sp   | 40sp        | -0.5sp         | Card stat numbers (money, cigarettes)  |

Line heights are set at 1.33× the font size for headlines and 1.5× for body text, which balances compact information density with sufficient breathing room. Letter spacing tightens slightly for large headlines and the hero stat font to maintain visual cohesion at display sizes, while it opens up for captions to preserve legibility at small sizes. The stat styles deserve special note: `stat_hero` at 48sp with Space Mono Bold creates the app's most visually dominant element—the "days smoke-free" counter that anchors the home screen. This number should feel monumental, as if it is the most important piece of information on the device at that moment. A subtle text shadow (`shadowColor: #00E676, dx: 0, dy: 0, blurRadius: 12sp, alpha: 0.3`) is applied to hero stats to produce a neon glow effect consistent with the overall design language.

### Typography Accessibility

All body and caption sizes meet WCAG AA contrast requirements against their respective background colors. For users who enable Android's font scaling (up to 1.5×), the layout uses `dp`-based constraints that expand gracefully; the hero stat at 48sp becomes 72sp at maximum scaling, which is accommodated by the card's `wrap_content` height. The app sets `android:maxLines` judiciously—headlines allow 2 lines maximum to prevent layout breakage, while body text in cards is truncated at 3 lines with an ellipsis and a "Read more" affordance.

---

## Spacing & Grid

Breathy uses an **8dp base grid** system, meaning all spacing, padding, margins, and component dimensions are multiples of 8dp. This creates a rhythmic consistency that feels intentional and polished, even if users cannot articulate why. The 8dp grid was chosen over 4dp because Breathy's dark theme relies on generous whitespace to prevent the "dense dashboard" feeling—each card, each section, needs room to breathe against the dark background. Tighter spacing would make the neon accents feel cramped and visually noisy rather than vibrant and purposeful.

### Core Spacing Tokens

| Token          | Value  | Usage                                                      |
|----------------|--------|--------------------------------------------------------------|
| `space_xs`     | 4dp    | Inline icon-text gap, chip internal padding                  |
| `space_sm`     | 8dp    | Related element gaps, list item internal spacing             |
| `space_md`     | 16dp   | Standard horizontal padding, card internal padding           |
| `space_lg`     | 24dp   | Section gaps, vertical spacing between major blocks          |
| `space_xl`     | 32dp   | Screen edge to first content, large section separators       |
| `space_xxl`    | 48dp   | Full-screen vertical centering offsets, hero stat top margin |

### Padding Specifications

| Component            | Horizontal | Vertical  | Notes                                         |
|----------------------|------------|-----------|-----------------------------------------------|
| Screen edges         | 16dp       | —         | Applied via Scaffold content padding          |
| Cards                | 16dp       | 12dp      | Internal content padding                      |
| List items           | 16dp       | 12dp      | Horizontal start/end, vertical top/bottom     |
| Bottom sheets        | 16dp       | 16dp      | Content area, handle has own 8dp top margin   |
| Buttons (text)       | 16dp H     | 12dp V    | Minimum touch target 48dp                     |
| Input fields         | 16dp H     | 16dp V    | Inner content area within the field border    |

### Border Radius

| Component            | Radius   | Notes                                          |
|----------------------|----------|------------------------------------------------|
| Cards                | 16dp     | All four corners, creates soft elevated feel   |
| Buttons              | 24dp     | Pill-shaped for CTAs, slightly rounded for others |
| FABs                 | 50%      | Circular, enforced by Material FAB spec        |
| Input fields         | 12dp     | Subtler than cards, indicates input vs. display |
| Chips                | 20dp     | Stadium shape (height = 2× radius)             |
| Bottom sheets        | 24dp     | Top corners only, bottom is flush with edge    |
| Dialogs              | 28dp     | Large radius for modern floating appearance    |
| Avatar images        | 50%      | Circular crop for all user avatars             |

### Layout Grid Details

The app uses a **4-column grid** on phones (≤ 599dp width) and an **8-column grid** on foldables/tablets (≥ 600dp). Gutters are 12dp between columns, and margins are 16dp on each side. Cards span full width on phones but may span 2–4 columns on larger screens to avoid excessively wide content lines. The maximum content width is capped at 600dp, centered on wide screens, to preserve readable line lengths for body text (45–75 characters per line). All Compose layouts use `Modifier.fillMaxWidth()` coupled with `Modifier.widthIn(max = 600.dp)` at the root level of each screen's content composable.

---

## Animation Specs

Animation is not decoration in Breathy—it is a core feedback mechanism. Every state change, every achievement, every data point update is accompanied by purposeful motion that tells the user what happened and how to feel about it. Animations are grouped into three categories: **micro-interactions** (button presses, toggles), **transitions** (screen navigation, sheet appearances), and **celebrations** (achievements, milestones). Micro-interactions are kept under 200ms to maintain responsiveness. Transitions are 200–400ms to convey spatial relationships. Celebrations can last 2–5 seconds because they interrupt the flow intentionally—the user should pause and savor the moment.

### Core Animation Specifications

| Animation                | Duration | Easing                          | Details                                                                                   |
|--------------------------|----------|---------------------------------|-------------------------------------------------------------------------------------------|
| Stats card entrance      | 300ms    | `EaseOut`                       | Scale from 0.8→1.0 + Alpha from 0→1, staggered 80ms per card                             |
| Health timeline item     | 300ms    | `EaseOut`                       | Fade-in + slide-up 16dp, staggered 50ms delay per item                                   |
| Craving button pulse     | 1000ms   | `EaseInOut`                     | Scale 1.0→1.05→1.0, repeat infinite, orange glow radial animates opacity 20%→40%→20%     |
| Achievement confetti     | 3000ms   | N/A (particle physics)          | Konfetti library: gold (#FFD740), green (#00E676), blue (#448AFF) particles, 80 count     |
| Navigation fade-through  | 200ms    | `EaseInOut`                     | Outgoing screen fades to 0 alpha, incoming fades to 1, crossfade at 50%                   |
| Like button bounce       | 200ms    | `OvershootInterpolator(2.0)`    | Scale 1.0→1.3→1.0, heart icon morphs from outline to filled                              |
| Reward claim             | 800ms    | `EaseOut`                       | Coin spin (Y-axis 720°) + counter increment animates from old value to new value          |
| Bottom sheet enter       | 250ms    | `EaseOut`                       | Slide up from bottom + slight fade of scrim (0→0.6 alpha)                                |
| Bottom sheet exit        | 200ms    | `EaseIn`                        | Slide down + scrim fade out                                                               |
| Pull-to-refresh          | 400ms    | `EaseOut`                       | Circular indicator rotates, content translates down 48dp then snaps back                  |
| Breathing circle         | Per phase| `Linear`                        | Inhale 4s expand, Hold 7s static, Exhale 8s contract, 3 cycles total                     |

### Implementation Notes

**Stats Card Entrance:** Each stat card on the home screen uses a `AnimatedVisibility` composable with a custom `EnterTransition` that combines `scaleIn(initialScale = 0.8f, animationSpec = tween(300, easing = EaseOut))` and `fadeIn(animationSpec = tween(300))`. Cards are staggered by 80ms using `LaunchedEffect` with a `delay(index * 80L)` pattern. This creates a cascade effect where the hero card appears first, followed by the three sub-cards left-to-right, giving the user's eye a natural scanning path.

**Craving Button Pulse:** The FAB's pulse is implemented using `infiniteTransition.animateFloat(initialValue = 1.0f, targetValue = 1.05f, animationSpec = infiniteRepeatable(tween(1000, easing = EaseInOut), RepeatMode.Reverse))`. The radial glow behind the button uses a separate `RadialGradient` whose alpha animates between 0.2 and 0.4 in sync with the scale pulse. The glow radius is 1.5× the FAB diameter, creating a soft orange halo that makes the button impossible to ignore without being garish.

**Achievement Confetti:** The Konfetti library (`nl.dionsegijn:konfetti-compose:2.0.4`) is triggered when the backend sends an achievement unlock event via the real-time connection. The `PartySystem` creates a `ParticleSystem` with 80 particles across three colors, an emission angle of 60°–120° (upward spread), a speed range of 300–600 dp/s, and a lifetime of 3000ms. Particles use `GravityModule(0.1f)` for a gentle fall and `FadeModule()` for natural disappearance. The confetti view is overlaid on top of the screen content using a `Box` with `zIndex = 999f`.

**Breathing Circle:** The breathing exercise uses a `Canvas` composable that draws a circle whose radius animates according to the 4-7-8 pattern. The circle expands during the 4-second inhale (radius from 40dp to 120dp), holds at 120dp during the 7-second hold, and contracts back to 40dp during the 8-second exhale. A text label in the center shows the current phase ("Inhale", "Hold", "Exhale") and a countdown timer. The circle's stroke color shifts from blue (`#448AFF`) during inhale, to purple (`#B388FF`) during hold, to green (`#00E676`) during exhale, creating a calming chromatic rhythm that reinforces the breathing cadence.

**Reward Claim:** When a user claims a daily reward, a coin icon (a circular composable with a gold gradient) rotates 720° around the Y-axis using `graphicsLayer { rotationY = animatedRotation }` with `Camera(distance = 12f)` for perspective. Simultaneously, the XP counter below animates from its current value to the new value using `animateIntAsState` with a `tween(600, easing = LinearOutSlowInEasing)`, creating a slot-machine-style number roll effect. A small "+10 XP" text flies upward and fades out (translate Y: 0→-40dp, alpha: 1→0, 800ms) to reinforce the gain.

---

# Screen-by-Screen Wireframe Descriptions

## 1. Auth Screen

The Auth Screen is the user's first encounter with Breathy, and it must immediately communicate the app's core promise: freedom and breath. The full-screen gradient background (`gradient_primary`, 45° green-to-blue) creates an immersive, optimistic atmosphere that differentiates Breathy from the gray, clinical login screens of most health apps. Centered at the top third of the screen, the Breathy logo features a stylized lung icon with a subtle breathing animation—the icon expands and contracts on a 5-second loop, reinforcing the breathing motif before the user has even signed in. Below the logo, the tagline "Breathe Free" is rendered in `headline_large` (Montserrat Bold, 24sp) with a faint text shadow that matches the gradient.

The form area is positioned in the center of the screen, contained within a `bg_surface` (`#1A1A2E`) rounded card (28dp radius) with 24dp internal padding. The email input field sits at the top of the form with a mail icon on the leading side and uses the `bg_surface_variant` (`#252540`) as its background fill, creating a subtle inset effect. Below it, the password field includes a visibility toggle icon (eye/eye-off) on the trailing side. Both fields use `body_large` (Inter, 16sp) for input text and `caption_large` for placeholder text in `text_disabled` color. The "Sign In" button spans the full width of the card, uses the primary gradient as its background, and renders "Sign In" in `headline_small` white text. Its pressed state applies a 10% lighter gradient with a subtle scale-down (0.98×) for tactile feedback.

Below the button, "Forgot Password?" is a `caption_large` text link in `accent_secondary` blue, aligned to the end (right) of the card. A horizontal divider follows with the text "or" centered on it—this divider uses a `bg_surface_variant` line (1dp) with "or" in a `caption_large` pill (surface background, 8dp padding). The Google Sign-In button uses an outlined style (1dp border in `text_secondary`, 24dp radius) with the Google "G" icon on the leading side and "Sign in with Google" in `body_medium`. At the very bottom of the screen, below the card, "Don't have an account? Sign Up" uses `caption_large` with "Sign Up" in `accent_secondary` blue and bold weight, serving as the navigation link to the registration variant of this screen.

---

## 2. Onboarding (4 Steps, Horizontal Pager with Dots)

The onboarding flow is presented as a horizontal `ViewPager2` with dot indicators at the bottom, following the widely understood swipe-to-continue pattern. Each step occupies a full screen with consistent layout: an illustration or interactive element in the top 50% of the screen, title and description text in the middle, and action buttons at the bottom anchored by 32dp bottom padding. The dot indicators are 8dp circles below the content—inactive dots are `bg_surface_variant` and the active dot is `accent_primary` green, expanding to 24dp width with an 8dp height (pill shape) for the active state. A "Skip" text link in `caption_large` `text_secondary` sits at the top-right corner for steps 1–3, replaced by nothing on step 4 (the user must complete the flow).

### Step 1: Welcome

The Welcome step establishes emotional connection. A full-width illustration shows a person standing on a hilltop with arms slightly raised, rendered in the app's neon color palette against a dark background—the figure's silhouette is outlined in `accent_primary` green with particle effects suggesting fresh air. The title "Your Journey Starts Here" uses `headline_large` (Montserrat Bold, 24sp) centered below the illustration. A two-line description in `body_medium` `text_secondary` reads: "Thousands have quit smoking with Breathy. Track your progress, connect with others, and celebrate every smoke-free day." The bottom area contains a full-width "Next" button with the primary gradient and a right-arrow icon on the trailing side. This step's purpose is to inspire hope and set expectations for the experience ahead.

### Step 2: Quit Date

The Quit Date step captures when the user's smoke-free journey begins. The illustration area shows a calendar icon with a checkmark, styled in neon green. The title reads "When Did You Quit?" in `headline_large`. Below it, a horizontal toggle with two options—"Instant" and "Gradual"—uses a `bg_surface_variant` track with a `accent_primary` green thumb that slides between options. "Instant" means the user has already quit or is quitting today; "Gradual" implies a reduction plan (which may unlock additional configuration in a future version). When "Instant" is selected, a date picker appears below: three horizontally stacked spinners for Month, Day, and Year, each styled with `bg_surface_variant` backgrounds and `stat_card` typography for the selected value. The selected date is displayed in `headline_small` green text below the spinners (e.g., "March 4, 2025"). If the selected date is today, a badge reads "Day 0 — Your first day!" in `accent_primary`. The "Next" button remains disabled until a valid date is selected.

### Step 3: Habits

The Habits step quantifies the user's smoking habit, which enables all future calculations (money saved, cigarettes avoided, health milestones). The title reads "Tell Us About Your Habit" in `headline_large`. A "Cigarettes per day" stepper uses a horizontal row: a minus button (circular, `bg_surface_variant`, 40dp), the number in `stat_card` Space Mono Bold 32sp centered, and a plus button (same style). The stepper increments/decrements by 1, clamped to 1–100. Below, "Price per pack" shows a currency input field (localized currency symbol on the leading side) with a numeric keyboard. "Cigarettes per pack" uses another stepper (clamped 10–40, default 20). As the user adjusts these values, a live calculation preview appears at the bottom in a `bg_surface_variant` card: "You'll save approximately **$X.XX** per day and avoid **Y** cigarettes." The savings figure uses `stat_card` in green; the avoidance figure uses `stat_card` in blue. This real-time feedback turns a mundane data-entry step into an early motivational moment.

### Step 4: Complete

The Complete step is a celebration. A confetti-style illustration fills the top half—colorful particles in the app's accent colors exploding upward from a central point. The title "You're All Set!" uses `headline_large` in `accent_primary` green. Below, a summary card in `bg_surface` recaps the user's choices: "Quit Date: March 4, 2025", "Cigarettes/day: 15", "Savings goal: $8.50/day"—each line with a small icon and `body_medium` text. A brief motivational message in `body_medium` `text_secondary` reads: "Every breath without smoke is a victory. Let's make today count." The bottom CTA is a full-width "Let's Go!" button with the primary gradient, slightly larger than previous "Next" buttons (56dp height vs. 48dp) to emphasize finality. Tapping this button triggers the Konfetti celebration animation (3 seconds, gold + green + blue particles) before navigating to the Home Screen.

---

## 3. Home Screen (Main Dashboard)

The Home Screen is the heart of Breathy—the screen users see most often and the one that must deliver the most motivational value in a single glance. It follows a vertical scroll layout with distinct sections stacked from top to bottom, designed so that the most impactful information is visible without scrolling (above-the-fold on a standard 6-inch phone: roughly 640dp).

### Top Bar

The top bar is a fixed-height (56dp) row pinned to the top of the screen. On the leading side, a greeting text "Hello, [first name]" uses `headline_medium` (Montserrat Bold, 20sp) in `text_primary`. Next to the greeting, a small circular avatar (32dp diameter) shows the user's profile photo or a default initials circle in `accent_primary`. On the trailing side, a notification bell icon (24dp) sits within a 48dp touch target; if unread notifications exist, a 10dp red dot appears at the top-right of the icon. Tapping the avatar navigates to the Profile Screen; tapping the bell opens a notification dropdown or navigates to a notification list.

### Hero Stat Card

The Hero Stat Card is the visual centerpiece of the Home Screen. It spans the full content width (screen width minus 32dp horizontal margins) and is approximately 180dp tall. The background is `bg_surface` with a 16dp border radius. A subtle radial glow (`gradient_glow_green`) is positioned behind the stat number, creating the illusion that the number is a light source. The primary stat—the number of days smoke-free—is rendered in `stat_hero` (Space Mono Bold, 48sp) in `accent_primary` green with a faint green text shadow. Above the number, "Days Smoke-Free" is displayed in `caption_large` `text_secondary`. Below the number, a motivational subtitle in `body_medium` `text_secondary` changes based on milestone proximity: "Keep going! Your next milestone is 30 days" or "Amazing! You've reached a new milestone!" When a milestone is reached, this card plays the confetti animation and briefly flashes brighter (alpha pulse 1.0→1.2→1.0, 500ms).

### Stat Cards Row

Below the hero card, a horizontal row of three equally-sized stat cards provides secondary metrics. Each card is approximately one-third of the content width (accounting for 8dp gaps between cards) and 100dp tall, using `bg_surface` with 16dp border radius. The three cards are:

1. **Money Saved** — Icon: a coin/banknote icon in `accent_primary` green. The value is rendered in `stat_card` (Space Mono Bold, 32sp) in `accent_primary`. Label: "Saved" in `caption_large` `text_secondary`.
2. **Cigarettes Avoided** — Icon: a cigarette-with-slash icon in `accent_secondary` blue. The value in `stat_card` `accent_secondary`. Label: "Avoided" in `caption_large` `text_secondary`.
3. **Life Regained** — Icon: a heart-pulse icon in `accent_purple`. The value in `stat_card` `accent_purple`. Label: "Life Regained" in `caption_large` `text_secondary`.

Each card enters with the staggered entrance animation described in the Animation Specs section. Tapping a card could expand it to show more detail (e.g., tapping "Money Saved" shows a weekly bar chart), though this is a v2 consideration.

### Health Timeline

The Health Timeline occupies the next scroll section. It is a vertical list of health milestones—scientifically-backed benefits of quitting smoking ordered by when they occur. Each item is a row with: a circular icon (28dp) on the left, a connector line (2dp wide, `bg_surface_variant`) running vertically between items, the milestone title in `body_large` `text_primary`, and the time description in `caption_large` `text_secondary` (e.g., "After 20 minutes — Heart rate drops to normal"). Completed milestones (those whose time has passed since the user's quit date) show their icon in `accent_primary` green with a checkmark overlay and their title in `text_primary`. Upcoming milestones show their icon in `text_disabled` gray with a lock overlay and their title in `text_secondary`. The most recently completed milestone has a subtle green glow ring around its icon. Items animate in with the staggered fade-in described earlier.

### Floating Craving Button

The Floating Action Button is anchored to the bottom-right of the screen, 16dp from the right edge and 16dp above the bottom navigation bar. It is 56dp in diameter, circular (50% radius), with an `accent_orange` background and a white flame/cigarette icon (24dp) centered inside. The button plays the continuous pulse animation described in Animation Specs—scaling between 1.0× and 1.05× with a radial orange glow that breathes in sync. This button is always visible (even during scroll) and serves as the primary entry point for craving management. Tapping it opens the Craving Bottom Sheet.

### Bottom Navigation

The bottom navigation bar is 56dp tall, uses `bg_surface` as its background, and contains five destinations: Home (house icon), Community (people icon), Events (calendar icon), Leaderboard (trophy icon), and Profile (person icon). The active tab shows the icon in `accent_primary` green with a small label below in `caption_small` green; inactive tabs show icons in `text_disabled` gray with no label. Navigation between tabs uses the fade-through transition (200ms). A thin 1dp `bg_surface_variant` line separates the nav bar from the content above.

---

## 4. Craving Bottom Sheet

The Craving Bottom Sheet is the most critical interactive surface in the app because it serves the user at their most vulnerable moment—when they are experiencing a craving. Every design decision in this sheet prioritizes speed of access, calming presentation, and clear pathways to coping strategies. The sheet slides up from the bottom (250ms EaseOut) with a semi-transparent scrim (`#0D0D1A` at 60% alpha) covering the content behind it. The sheet itself uses `bg_surface` as its background, with 24dp top corner radius and a 32dp drag handle (4dp tall, 40dp wide, `text_disabled` color) centered at the top.

### Header

At the top of the sheet, the header reads "Stay Strong! 💪 You've got this" in `headline_small` (Montserrat Bold, 18sp) `text_primary`, centered. Below it, a timer displays the time since the user's last logged craving in `stat_card` `accent_secondary` blue (e.g., "2h 34m since last craving"). If this is the user's first craving, the text reads "Your first craving — you can do this!" in `body_medium` `text_secondary`. The header is designed to be immediately reassuring; the exclamation and emoji (optional, can be removed for minimalism) inject warmth, while the timer contextualizes the craving as a passing event.

### Coping Method Cards

Three horizontally-scrollable cards (each approximately 200dp wide × 160dp tall, with 12dp gaps) offer distinct coping strategies. Each card uses `bg_surface_variant` with 16dp border radius and contains: an icon (40dp) at the top, a title in `headline_small`, a one-line description in `caption_large` `text_secondary`, and a subtle "Tap to start" affordance. The cards are:

1. **Breathing Exercise** — Icon: a wind/lungs icon in `accent_secondary` blue. Title: "Breathe". Description: "4-7-8 breathing to calm cravings." Tapping this opens a full-screen breathing overlay with the animated breathing circle described in Animation Specs. The circle expands/contracts on the 4-7-8 pattern for 3 complete cycles (57 seconds total), with phase labels ("Inhale", "Hold", "Exhale") and a countdown. A subtle progress ring around the circle shows overall completion. The user can dismiss early by tapping "I'm Calm" at the bottom.

2. **Mini Game** — Icon: a gamepad icon in `accent_purple`. Title: "Distract". Description: "Tap as fast as you can for 30 seconds." Tapping opens a simple tap-counter game: a large circular button in the center of the screen with a countdown timer at the top (30→0 seconds) and a tap counter below the button in `stat_hero`. The button uses `accent_primary` green and plays a subtle haptic on each tap. The goal is to reach a target number (e.g., 100 taps), turning the craving energy into a focused physical activity. Results are displayed afterward: "You tapped 127 times! That craving doesn't stand a chance."

3. **AI Coach** — Icon: a chat bubble icon in `accent_primary` green. Title: "Talk". Description: "Chat with your AI coach." Tapping navigates to the AI Coach Screen with a pre-populated context message: "I'm having a craving right now." The coach responds with personalized encouragement based on the user's quit history, time of day, and craving patterns.

### Post-Coping Feedback

After completing any coping method, a feedback card slides up from the bottom of the sheet: "Did it help?" with two buttons—"Yes, I'm good 👍" (green, `accent_primary`) and "Not really 😔" (blue, `accent_secondary`). Selecting "Yes" logs the craving as "resolved" and dismisses the sheet with a success toast: "Craving conquered! +5 XP". Selecting "No" logs the craving as "unresolved," shows a softer message: "That's okay. Cravings are tough. Try another method or talk to your coach," and keeps the sheet open to allow the user to try another coping method. All craving events are logged locally and synced to the backend for the AI coach's context and the user's craving pattern analysis.

---

## 5. Community Screen

The Community Screen is a social feed that transforms the solitary act of quitting into a shared experience. Seeing others succeed, sharing struggles, and receiving encouragement are powerful retention drivers. The screen is organized into a tabbed layout at the top—**Feed** and **My Stories**—using a `TabRow` with `accent_primary` green underline indicator on the active tab. The active tab label uses `headline_small` in `text_primary`; the inactive tab uses `body_medium` in `text_secondary`.

### Feed Tab

The Feed tab displays a vertically-scrolling list of story cards from all users, ordered by recency with an algorithmic boost for stories from friends and high-engagement posts. Each story card uses `bg_surface` with 16dp border radius, 16dp horizontal and 12dp vertical internal padding, and 12dp vertical spacing between cards. The card layout from top to bottom is:

- **Author Row** — Circular avatar (32dp) on the left, followed by nickname in `body_medium` `text_primary` and "X days smoke-free" in `caption_large` `accent_primary` green on the same line. A timestamp (e.g., "2h ago") in `caption_small` `text_disabled` is aligned to the right.
- **Photo** (optional) — If the story includes a photo, it appears below the author row as a full-width image with 12dp top margin and 16dp border radius, capped at 240dp height with `ContentScale.Crop`. Photos that are wider than tall get landscape treatment; portrait photos are center-cropped.
- **Content Text** — Up to 3 lines of preview text in `body_medium` `text_secondary`, truncated with ellipsis. Full text is viewable by tapping the card to open the Story Detail Screen.
- **Life Changes Chips** — A horizontal row of small chips (20dp height, 8dp horizontal padding, 10sp label) in `accent_secondary` blue with 10% opacity background and blue text. Common chips: "Better Breathing", "More Energy", "Saved Money". Maximum 3 chips shown; if more exist, a "+2 more" chip appears.
- **Engagement Row** — At the bottom of the card, a row with: a heart icon + count on the left (filled `accent_pink` if the current user liked it, outline `text_secondary` if not), a chat bubble icon + reply count, and a share icon on the far right. All engagement targets are 48dp minimum touch area.

### My Stories Tab

The My Stories tab uses the same card layout but filters to show only the current user's stories. An additional "Edit" icon (pencil) appears on each card for stories created within the last 24 hours. The empty state shows an illustration and the text "Share your first story to inspire others" with a CTA button that opens the Post Story Screen.

### Share Your Journey FAB

A floating action button in the bottom-right (56dp, `accent_primary` green, pen icon) opens the Post Story Screen. It appears on both tabs and is positioned 16dp above the bottom navigation, 16dp from the right edge. The FAB does not pulse (unlike the craving button) to avoid visual conflict.

### Pull-to-Refresh & Infinite Scroll

The feed supports pull-to-refresh using `SwipeRefresh` (Material 3) with a `accent_primary` green indicator. Upon release, the refresh triggers an API call to fetch the latest stories, replacing the current list. Infinite scroll is implemented via a `LazyColumn` with a `LazyListState` that detects when the user scrolls to the last visible item; a loading spinner (CircularProgressIndicator in `accent_primary`) appears at the bottom while the next page loads. If the network call fails, an inline error message with a "Retry" button appears in place of the spinner.

---

## 6. Post Story Screen

The Post Story Screen is a modal-style screen that slides up from the bottom (or navigates as a new screen with a slide-up transition). The top bar contains a back arrow (left), "Share Your Story" title in `headline_small` centered, and a "Share" button (right) in `accent_primary` green text with `headline_small` weight. The "Share" button is initially disabled (40% opacity) until the content field contains at least 10 characters.

### Photo Upload Area

At the top of the content area, a 200dp-tall dashed-border rectangle (2dp dash, 8dp gap, `text_disabled` color) serves as the photo upload zone. In the center of this area, a camera icon (48dp, `text_disabled`) sits above the text "Tap to add a photo" in `caption_large` `text_secondary`. Tapping the area opens a bottom sheet with two options: "Take Photo" (opens CameraX intent) and "Choose from Gallery" (opens photo picker). After a photo is selected, it fills the upload area with `ContentScale.Crop`, and a small "×" button appears at the top-right corner to remove it. The dashed border becomes a solid 1dp `bg_surface_variant` border when a photo is present.

### Content Text Field

Below the photo area, a multiline text field occupies the remaining vertical space. It uses `body_large` (Inter, 16sp) for input text with a `bg_surface_variant` background and 12dp border radius. The field has no explicit border in its resting state; instead, it relies on the background color contrast against the screen's `bg_primary` base. The placeholder text reads "What's on your mind? Share a victory, a struggle, or a tip..." in `text_disabled`. A character counter in `caption_small` `text_secondary` sits at the bottom-right of the field: "0/500", incrementing with each character typed. The counter turns `semantic_warning` yellow at 450 characters and `semantic_error` red at 500 (the hard limit, beyond which input is blocked).

### Life Changes Chip Selector

Below the text field, a "Life Changes" label in `body_medium` `text_primary` introduces a horizontal wrapping flow of selectable chips. Each chip is 32dp tall with 12dp horizontal padding and uses `caption_large` text. In the unselected state, chips have a 1dp `text_disabled` border with `text_secondary` text. When selected, chips fill with a 10% opacity `accent_secondary` blue background, a solid `accent_secondary` border, and blue text. The available chips are: Better Breathing, More Energy, Improved Taste, Saved Money, Better Skin, Whiter Teeth, Less Coughing, Other. "Other" opens a small text input for a custom tag (max 20 characters). There is no limit on how many chips can be selected, but the preview in the story card shows a maximum of 3 with "+N more."

### Share Button

The full-width "Share" button at the bottom uses the primary gradient background and `headline_small` white text. When tapped, it shows a brief loading spinner (replacing the button text), then navigates back to the Community Feed with the new story appearing at the top and a success toast: "Story shared! 🎉". If the post fails (network error), an inline error message appears above the button with a "Try Again" action.

---

## 7. Story Detail Screen

The Story Detail Screen shows the full content of a community story post, along with its engagement data and a replies section. It is accessed by tapping a story card in the Community Feed. The top bar contains a back arrow and the author's nickname in `headline_small`.

### Story Content Section

The story content section is a scrollable area that begins with the author info row: a larger avatar (48dp) on the left, nickname in `headline_small` `text_primary`, "X days smoke-free" in `body_medium` `accent_primary`, and a "View Profile" link in `caption_large` `accent_secondary` below. Tapping "View Profile" navigates to the Public Profile Screen. If the story has a photo, it appears at full content width (with 16dp border radius) below the author row. The full story text is displayed in `body_large` `text_primary` with 24dp line spacing for readability. Life changes chips are shown below the text in a horizontal wrapping row, identical to their appearance in the card but fully visible (no "+N more" truncation). The like button and reply count are shown at the bottom of this section as a horizontal divider with icons.

### Replies Section

Below the story content, a "Replies (N)" header in `headline_small` introduces the replies list. Each reply is a row with: a smaller avatar (32dp) on the left, the replier's name in `body_medium` `text_primary`, the reply content in `body_medium` `text_secondary`, and a timestamp in `caption_small` `text_disabled` aligned to the right below the content. Replies are ordered chronologically (oldest first). A text input field at the very bottom of the screen (anchored above the bottom navigation if applicable, or at the bottom of the screen if no bottom nav) allows the user to type a reply. The input field uses `bg_surface_variant` background, 12dp radius, and has a send icon button on the trailing side in `accent_primary` green. Tapping send posts the reply immediately (optimistic UI update with rollback on failure) and scrolls the list to show the new reply.

---

## 8. Public Profile Screen

The Public Profile Screen displays another user's profile when tapped from a story card, reply, leaderboard, or friend list. It is a scrollable screen with the following sections from top to bottom:

### Profile Header

The profile header occupies approximately 200dp of vertical space. A circular profile photo (80dp diameter) is centered horizontally at the top, with a subtle `accent_primary` green ring (2dp) around it. Below the photo, the nickname appears in `headline_medium` `text_primary`, and the location (if provided) in `caption_large` `text_secondary` with a map pin icon. A "Days Smoke-Free" badge sits below the location—a pill-shaped chip (36dp height) with `accent_primary` green background at 20% opacity, green text showing "X days smoke-free" in `body_medium` bold. This badge is the social proof element that establishes credibility within the community.

### Stats Row

A horizontal row of three equally-spaced stats below the header: XP (number in `stat_card` `accent_purple`, label "XP" in `caption_large`), Level (number in `stat_card` `accent_secondary`, label "Level" in `caption_large`), and Achievements (number in `stat_card` `accent_primary`, label "Achievements" in `caption_large`). Each stat is vertically stacked and centered. The stats are separated by thin vertical dividers (1dp, `bg_surface_variant`).

### Achievements Grid

A 3-column grid of achievement badges. Each badge is a 72dp × 72dp square with 12dp border radius. Unlocked achievements show the achievement icon in its designated accent color on a `bg_surface_variant` background with a subtle glow matching the icon color. Locked achievements show a grayed-out lock icon on a `bg_surface` background with a `text_disabled` border. Tapping an unlocked achievement shows a tooltip with the achievement name and description; tapping a locked achievement shows the unlock condition. The grid scrolls vertically if there are more than 6 achievements.

### Action Buttons

Two buttons at the bottom of the profile (above the achievements or as a sticky bottom bar): "Add Friend" (primary gradient, full width) if not friends; "Friends" (outlined, `accent_primary` border) if already friends; "Pending" (disabled, `text_disabled`) if a request has been sent but not accepted. If the users are friends, a "Message" button (outlined, `accent_secondary` border) appears next to the "Friends" button, which navigates to the Chat Screen.

---

## 9. Friends Screen

The Friends Screen manages the user's social connections within Breathy. It uses a tabbed layout with two tabs: **Friends** and **Requests**, styled identically to the Community Screen's tab bar.

### Friends Tab

A search bar at the top (36dp height, `bg_surface_variant` background, 20dp radius, magnifying glass icon, "Search friends..." placeholder) filters the friend list in real-time as the user types. Below the search bar, a vertical list of friend items. Each item is a row with: avatar (40dp), nickname in `body_medium` `text_primary`, "X days smoke-free" in `caption_large` `accent_primary`, and "Last active X ago" in `caption_small` `text_disabled`. Tapping a friend item navigates to their Public Profile Screen. The list is sorted alphabetically by default but can be re-sorted by "days smoke-free" via a subtle sort icon next to the search bar. An empty state shows an illustration with "No friends yet. Explore the community to connect!" and a CTA to the Community Screen.

### Requests Tab

A vertical list of friend request cards. Each card uses `bg_surface` with 16dp radius and contains: avatar (40dp) on the left, nickname and days smoke-free on the right, and two buttons at the bottom-right of the card: "Accept" (`accent_primary` green filled, 36dp height) and "Decline" (`semantic_error` red outlined, 36dp height). Accepting a request removes the card with a slide-right animation and adds the friend to the Friends tab. Declining removes it with a slide-left animation. An empty state shows "No pending requests — you're all caught up!" with a subtle checkmark illustration.

---

## 10. Chat Screen

The Chat Screen provides one-on-one messaging between friends. It follows standard messaging UI conventions with Breathy's dark neon aesthetic. The top bar (56dp) contains: a back arrow on the left, the friend's avatar (32dp) and name in `headline_small`, and a green dot indicator for online status (8dp, positioned at the bottom-right of the avatar). The main content area is a vertically-scrolling list of message bubbles, auto-scrolling to the bottom when new messages arrive.

### Message Bubbles

Sent messages appear on the right side of the screen with `accent_primary` green at 15% opacity as the bubble background, rounded corners (16dp top-left, 16dp top-right, 4dp bottom-right, 16dp bottom-left), and `text_primary` text in `body_medium`. Received messages appear on the left with `bg_surface_variant` as the bubble background, rounded corners (16dp top-left, 4dp bottom-left, 16dp top-right, 16dp bottom-right), and `text_primary` text. Each bubble has 12dp internal padding and 8dp vertical spacing between consecutive messages from the same sender (reduced from 16dp for different senders). Timestamps are shown only when there is a >5-minute gap between messages, centered above the message in `caption_small` `text_disabled`.

### Typing Indicator

When the other user is typing, three animated dots appear inside a received-message-style bubble on the left. Each dot is 6dp in `text_secondary` color and animates vertically (translate Y: 0→-6dp→0) with a 150ms stagger between dots and a 600ms cycle, creating the classic "typing" rhythm. The indicator appears 1 second after the other user starts typing (debounced) and disappears when a message is received.

### Message Input

At the bottom of the screen, a message input bar (56dp height) is anchored above the system navigation bar. It contains: a text input field (expandable up to 120dp for multiline messages) with `bg_surface_variant` background, 24dp radius, and "Type a message..." placeholder; and a send button (36dp circular, `accent_primary` green, paper plane icon) on the trailing side. The send button transitions from `text_disabled` (no input) to `accent_primary` (has input) with a 150ms fade. Read receipts (double check marks, `accent_primary` for read, `text_disabled` for delivered) appear at the bottom-right of sent message bubbles in `caption_small` size.

---

## 11. Leaderboard Screen

The Leaderboard Screen taps into competitive motivation by showing how users rank against each other in their smoke-free journey. It uses a tabbed layout: **Global** and **Friends**, with the same tab styling as other tabbed screens.

### Podium (Top 3)

The top of the screen features a podium visualization for the top 3 users. The layout is a custom arrangement where 2nd place stands on the left (shorter podium), 1st place in the center (tallest podium), and 3rd place on the right (shortest podium). Each podium position shows: a circular avatar (56dp for 1st, 48dp for 2nd/3rd), the user's name in `body_medium` `text_primary`, days smoke-free in `caption_large` `accent_primary`, and a crown icon for 1st place. The podiums themselves are rounded rectangles (`bg_surface_variant` for 1st with a gold gradient accent, `bg_surface` for 2nd and 3rd) with heights of 80dp (1st), 60dp (2nd), and 50dp (3rd). The podium section has a subtle radial glow (`gradient_glow_green`) behind the 1st place avatar.

### Leaderboard List

Below the podium, a vertical list shows ranks 4 onward. Each list item is a row with: rank number in `stat_card` `text_secondary` (or `accent_primary` if the current user), avatar (36dp), name in `body_medium` `text_primary`, days smoke-free in `body_medium` `accent_primary`, and XP in `caption_large` `accent_purple`. The current user's row is highlighted with a `accent_primary` 10% opacity background and a 2dp green left border, making them instantly findable in a long list. The list supports infinite scroll with a loading indicator at the bottom. If the user is not in the top 50, a sticky card at the very bottom of the visible list shows "Your Rank: #127 — Keep going!" to maintain motivation even when the user is far from the podium.

---

## 12. Events Screen

The Events Screen displays time-limited challenges that users can join for extra motivation and rewards. The screen is divided into two sections: **Active Events** at the top and **My Events** below. The Active Events section shows a horizontally-scrollable carousel of event cards, each approximately 280dp wide × 200dp tall with 12dp gaps. Each card uses `bg_surface` with 16dp border radius and contains: a banner image at the top (120dp, `ContentScale.Crop`), the event title in `headline_small` `text_primary` below the image, the event dates in `caption_large` `text_secondary` (e.g., "Mar 1–31, 2025"), a participant count in `caption_large` `accent_secondary` with a people icon, and a "Join" button (`accent_primary` green, pill-shaped, 32dp height) at the bottom-right. Cards for events the user has already joined show a "Joined ✓" badge instead of the Join button, rendered in `accent_primary` with a checkmark icon.

### My Events Section

Below the carousel, the "My Events" section shows a vertical list of events the user has joined. Each item is a compact card (full width, 72dp height) with: the event banner thumbnail (56dp × 56dp, rounded 8dp) on the left, event title in `body_medium` `text_primary`, progress text in `caption_large` `accent_primary` (e.g., "Day 12 of 30"), and a right-arrow chevron indicating tappability. Tapping navigates to the Event Challenge Screen. The "My Events" section is hidden if the user has not joined any events, replaced by an empty state encouraging exploration: "Join an event to challenge yourself with others!"

---

## 13. Event Challenge Screen

The Event Challenge Screen provides detailed information about a specific event and the user's progress within it. The top bar contains a back arrow and the event title in `headline_small`. Below, a banner image spans the full width (180dp, `ContentScale.Crop`) with a `gradient_dark_overlay` at the bottom for text readability.

### Event Info

Below the banner, the event description appears in `body_medium` `text_secondary` with a "Read more" expansion if it exceeds 3 lines. Below the description, a "Requirements" section lists the event's check-in requirements (e.g., "Daily video check-in required", "Min 5-second video") in a vertical list with bullet icons in `accent_secondary` blue.

### Progress Section

A progress card uses `bg_surface` with 16dp radius and contains: a circular progress indicator (80dp diameter) showing current streak / total days (e.g., "12/30") in `stat_card` `accent_primary` for the completed fraction and `text_secondary` for the remaining. The progress ring uses `accent_primary` for the completed arc and `bg_surface_variant` for the track. Below the progress ring, "Current Streak: 12 days" appears in `body_medium` `text_primary`, and "Prize: Exclusive Badge + 500 XP" in `body_medium` `accent_purple`.

### Daily Check-in Button

If the user has not checked in today, a full-width "Check In Today" button appears at the bottom of the progress card, using the primary gradient and `headline_small` white text. If already checked in, the button is replaced by "✓ Checked in today" in `accent_primary` green with a disabled appearance. Tapping "Check In Today" navigates to the Event Check-in Screen.

### Event Leaderboard Tab

A tab below the progress section shows an embedded leaderboard specific to this event, using the same list format as the main Leaderboard Screen but filtered to event participants and sorted by check-in streak. This creates a competitive micro-community within each event.

### Prizes Section

At the bottom, a "Prizes" section displays the rewards for completing the event: achievement badge icon, XP amount, and any other rewards (e.g., "Premium badge for profile"). Locked prizes appear grayed out until the user completes the event.

---

## 14. Event Check-in Screen

The Event Check-in Screen enables video-based daily check-ins for event challenges, providing accountability through visual proof. The screen is designed to be quick and straightforward—open, record, submit—because daily friction must be minimized to maintain streaks.

### Video Recording Area

The main area of the screen is a full-width, nearly full-height camera preview using CameraX. The preview fills the available space with `ContentScale.FillBounds` and a subtle `bg_primary` vignette at the edges. At the top of the preview, a semi-transparent overlay bar shows: the event name in `caption_large` white, "Day X of Y" in `caption_large` `accent_primary`, and a close (×) button on the right to cancel and return to the Event Challenge Screen.

### Record Button

At the bottom-center of the screen, a large circular record button (72dp outer ring, 56dp inner circle) sits above a semi-transparent control bar. The outer ring is white (`#FFFFFF`); the inner circle is `semantic_error` red. The button uses a "hold to record" interaction: the user presses and holds to start recording, during which the inner circle shrinks (animated, 300ms) to 40dp and a red recording indicator appears at the top of the preview. A timer shows elapsed time (0s–30s max) in `stat_card` white at the top-center. Releasing the button stops recording. The maximum recording length is 30 seconds; if the user holds for the full duration, recording stops automatically and transitions to the preview state.

### Preview & Submit

After recording, the screen transitions to a preview state: the recorded video plays in a loop in the preview area. Below the preview, two buttons appear: "Retake" (outlined, `text_secondary` border, 48dp height) on the left and "Submit" (`accent_primary` green filled, 48dp height) on the right. Tapping "Retake" returns to the recording state. Tapping "Submit" shows a brief uploading animation (circular progress in `accent_primary`), then navigates back to the Event Challenge Screen with a success toast: "Check-in submitted! Day X complete 🎉". If the upload fails, an error message appears with a "Retry" button that re-attempts the upload without requiring re-recording (the video is cached locally).

---

## 15. Admin Review Screen (Admin Only)

The Admin Review Screen is accessible only to users with admin privileges and is used to review and approve or reject event check-in video submissions. This screen is critical for maintaining the integrity of the event system, ensuring that participants are genuinely fulfilling the check-in requirements.

### Pending Check-ins List

The main view is a vertical list of pending check-in submissions, ordered by submission time (oldest first). Each item is a card using `bg_surface` with 16dp radius and contains: a video thumbnail (80dp × 60dp, rounded 8dp) on the left, the user's avatar (32dp) and name in `body_medium` `text_primary` below the thumbnail, the event name and day number in `caption_large` `text_secondary`, and a timestamp in `caption_small` `text_disabled`. On the right side of the card, two vertically-stacked buttons: a green "Approve" checkmark button (36dp × 36dp, `accent_primary` background) and a red "Reject" cross button (36dp × 36dp, `semantic_error` outlined).

### Inline Video Playback

Tapping a video thumbnail expands it inline within the card, playing the video using ExoPlayer. The expanded video occupies the full card width with a 16dp border radius and auto-plays with sound muted (tap to unmute). A progress bar at the bottom of the video shows playback position. The admin can watch the video and then tap Approve or Reject.

### Approve/Reject Actions

Approving a check-in removes the card with a slide-right animation and sends a server event marking the check-in as approved. The user receives a push notification: "Your check-in for Day X has been approved! ✓" Rejecting opens a small dialog with a required comment field ("Please provide a reason" in `caption_large` `text_disabled`) and a "Reject" confirmation button. The user receives a push notification: "Your check-in for Day X needs attention. Please resubmit." Rejected check-ins reappear in the user's Event Challenge Screen with a "Resubmit" option. A filter bar at the top allows admins to filter by event name or user name, and a tab bar separates "Pending" and "Reviewed" (showing today's approved/rejected items for audit purposes).

---

## 16. AI Coach Screen

The AI Coach Screen provides a conversational interface with an AI-powered quit-smoking coach. It uses a chat UI layout similar to the friend Chat Screen but with several differences that reflect the AI context. The top bar contains: a back arrow, a bot avatar (32dp, stylized brain/lungs icon with a subtle `accent_primary` glow ring), "Breathy Coach" in `headline_small`, and an "info" icon that opens a modal explaining the AI's capabilities and limitations. A context banner sits below the top bar—a thin horizontal strip in `bg_surface_variant` showing the user's key stats: "Day 47 · $312 saved · 705 cigarettes avoided · 4 cravings this week". This banner provides the AI with context (it is also sent as system context in the API call) and reminds the user that the coach "knows" their situation.

### Chat Interface

The chat interface uses the same bubble layout as the friend Chat Screen: sent messages (user) on the right with a slight `accent_primary` tint, received messages (coach) on the left with `bg_surface_variant`. However, coach messages may be longer and include formatted text (bullet points, bold highlights) rendered via a simple Markdown parser. The coach's typing indicator shows "Breathy Coach is thinking..." in `caption_large` `text_secondary` below the typing dots animation, which provides transparency about the AI's processing time.

### Rate Limit Warning

The AI coach has a daily message limit (e.g., 30 messages per day) to manage API costs. When the user reaches 25 messages, a warning banner in `semantic_warning` yellow appears below the context banner: "You've used 25 of 30 daily messages. The coach will be available again tomorrow." At 30 messages, the input field is disabled and shows "Daily limit reached. Come back tomorrow!" in `text_disabled`. Premium (subscribed) users have a higher limit (e.g., 100 messages), and the warning shows accordingly. This rate limit is enforced client-side (with server-side validation) to prevent abuse while ensuring all users have meaningful daily access to the coach.

---

## 17. Profile Screen

The Profile Screen is the user's personal dashboard and settings hub, accessed via the Profile tab in the bottom navigation. It is a scrollable screen organized into distinct sections, each separated by 24dp vertical spacing and optional section dividers (1dp, `bg_surface_variant`).

### Profile Header

The top section shows the user's profile photo (72dp, circular, with a camera icon overlay at the bottom-right for editing), nickname in `headline_medium` `text_primary`, email in `caption_large` `text_secondary`, and an "Edit Profile" pill button (outlined, `accent_secondary`, 32dp height) to the right of the name. Tapping the camera icon or "Edit Profile" opens an edit mode where the user can change their photo, nickname, and location.

### Stats Section

A `bg_surface` card (16dp radius) displays four key stats in a 2×2 grid: Quit Date (icon: calendar, value in `body_medium` `accent_secondary`), Days Free (icon: flame, value in `stat_card` `accent_primary`), Money Saved (icon: coin, value in `stat_card` `accent_primary`), Cigarettes Avoided (icon: cigarette-slash, value in `stat_card` `accent_secondary`). Each stat cell has 16dp padding and is separated by thin `bg_surface_variant` lines.

### Achievements Section

A horizontal scrolling row of achievement badges (64dp × 64dp each, 8dp gap). Unlocked badges show their icon in color; locked badges show a gray lock icon. Tapping any badge opens a bottom sheet with the achievement name, description, and unlock condition. A "See All" button at the end navigates to a full Achievements Screen (grid layout).

### Settings Section

A vertical list of settings items, each a 56dp-tall row with an icon on the left, label in `body_medium` `text_primary`, and a right chevron. The items are: Notifications (bell icon), Privacy (shield icon), Theme (palette icon — toggles dark/light, though dark is default), Help & Support (question mark icon), About (info icon). Each item navigates to its respective settings screen. Below these, a "Support Breathy" card uses `bg_surface` with a subtle `gradient_purple` border (2dp) and contains: a heart icon in `accent_pink`, "Support Breathy" in `headline_small`, and "Remove ads & get an exclusive badge" in `caption_large` `text_secondary`. Tapping this card navigates to the Subscription Screen.

### Logout Button

At the bottom of the screen, a "Log Out" button uses `semantic_error` red text in `body_medium` with a logout icon, centered. Tapping it shows a confirmation dialog: "Are you sure you want to log out?" with "Cancel" (default) and "Log Out" (red) buttons. Logging out clears the local session token and navigates to the Auth Screen.

---

## 18. Subscription Screen

The Subscription Screen presents Breathy's optional one-time purchase to support the app and unlock premium perks. It is designed to feel like a friendly request rather than a hard paywall—Breathy's core functionality remains free, and the subscription is positioned as community support.

### Header

The screen opens with a centered heart illustration (120dp, rendered in `accent_pink` with a subtle pulse animation, scale 1.0→1.05→1.0, 2000ms loop) above the title "Support Breathy" in `headline_large` `text_primary`. Below, a two-line description in `body_medium` `text_secondary`: "Breathy is free, but your support helps us keep it that way. One small contribution makes a big difference."

### Benefits List

A vertical list of three benefit items, each a row with a checkmark icon in `accent_primary` on the left and a description in `body_medium` `text_primary`: "Remove interstitial ads" (with a small "no-ads" icon), "Support ongoing development" (with a code icon), "Exclusive supporter badge on your profile" (with a star icon). Each benefit has 16dp vertical padding and a subtle divider below.

### Price & Purchase

A large price display shows "$1.00" in `stat_hero` (Space Mono Bold, 48sp) `accent_primary` with "one-time purchase" in `caption_large` `text_secondary` below. The "Support Breathy" purchase button is full-width, 56dp height, using the primary gradient background and `headline_small` white text. Tapping it triggers the Google Play Billing flow. During the purchase process, the button shows a loading spinner. On success, the screen transitions to an "Already a Supporter" state.

### Already Purchased State

If the user has already purchased, the price and purchase button are replaced by a thank-you card: a green checkmark icon (48dp, `accent_primary`), "Thank you for your support!" in `headline_small` `text_primary`, and "Your supporter badge is active on your profile" in `caption_large` `text_secondary`. A subtle confetti burst plays when this screen loads for a returning supporter, reinforcing the positive association.

---

# User Flow Diagrams

## 1. New User Onboarding Flow

```
┌─────────────┐
│  App Launch  │
└──────┬───────┘
       │
       ▼
┌─────────────┐     Skip      ┌──────────────┐
│  Auth Screen │──────────────▶│ Home Screen   │
│  (Sign In)   │              │ (Skip Setup)  │
└──────┬───────┘              └───────────────┘
       │ Sign In success
       ▼
┌──────────────────┐
│ Has Quit Date?    │
│ (Backend check)   │
└────┬─────────┬────┘
     │ Yes     │ No
     ▼         ▼
┌────────┐  ┌──────────────────┐
│  Home   │  │  Onboarding      │
│  Screen │  │  Step 1: Welcome │
└────────┘  └────────┬─────────┘
                     │ Next
                     ▼
            ┌──────────────────┐
            │ Step 2: Quit Date│
            │ (Select date +   │
            │  instant/gradual)│
            └────────┬─────────┘
                     │ Next
                     ▼
            ┌──────────────────┐
            │ Step 3: Habits   │
            │ (Cigs/day, price,│
            │  per pack)       │
            └────────┬─────────┘
                     │ Next
                     ▼
            ┌──────────────────┐
            │ Step 4: Complete │
            │ (Summary +       │
            │  "Let's Go!")    │
            └────────┬─────────┘
                     │ Let's Go!
                     ▼
              ┌─────────────┐
              │ Confetti 🎉  │
              │ Animation    │
              └──────┬───────┘
                     │
                     ▼
              ┌─────────────┐
              │ Home Screen  │
              └─────────────┘
```

The new user onboarding flow is designed to minimize friction while capturing essential data. First-time users arrive at the Auth Screen, where they can sign in with email/password or Google. Upon successful authentication, the backend is queried for an existing quit date. If one exists (returning user on new device), the user goes directly to the Home Screen. If no quit date is found, the four-step onboarding pager begins. Each step is mandatory (the "Skip" link on Step 1 only skips the welcome illustration, not the entire flow). Steps 2 and 3 validate their inputs before enabling the "Next" button, preventing incomplete data from reaching the backend. The final step triggers a celebration that psychologically commits the user to their quit journey before they see the dashboard for the first time. The entire flow should take under 90 seconds for a decisive user.

---

## 2. Daily Usage Flow

```
┌──────────────┐
│  App Launch   │
└──────┬───────┘
       │
       ▼
┌──────────────────┐
│  Home Screen      │
│  ┌──────────────┐│
│  │ Hero: Day X   ││  ← First glance: how many days?
│  └──────────────┘│
│  ┌────┐┌────┐┌──┐│
│  │$$$ ││Cig ││Life│ │  ← Secondary stats
│  └────┘└────┘└──┘│
│  Health Timeline  │  ← Scroll: what's next?
└──────┬───────────┘
       │
       ├──── Tab: Community ──▶ Community Feed (scroll, like, reply)
       │
       ├──── Tab: Events ─────▶ Events List ──▶ Event Challenge
       │                                            │
       │                                     ┌──────┴──────┐
       │                                     │ Check-in     │
       │                                     │ today?       │
       │                                     └──┬───────┬───┘
       │                                   Yes │       │ No
       │                                        ▼       ▼
       │                              Check-in Screen  View Progress
       │                                        │
       │                                        ▼
       │                              Submit Video ✓
       │
       ├──── Tab: Leaderboard ──▶ View Rank & Friends
       │
       ├──── Tab: Profile ──────▶ Stats / Settings / Support
       │
       └──── FAB: Craving ──────▶ Craving Bottom Sheet
                                       │
                            ┌──────────┼──────────┐
                            ▼          ▼          ▼
                      Breathe      Mini Game    AI Coach
                      (4-7-8)     (30s tap)    (Chat)
                            │          │          │
                            └──────────┼──────────┘
                                       ▼
                              "Did it help?"
                              Yes → +5 XP, close
                              No  → Try another
```

The daily usage flow revolves around the Home Screen as the hub. Most users will open the app, see their days counter increment (a small celebration animation plays at midnight local time), check their stats, and potentially engage with one or more secondary features. The flow is non-linear by design—users should feel free to explore in whatever order suits their mood. However, the Craving FAB is always one tap away from any screen, ensuring that the most critical feature (craving support) has the shortest possible access path. Event check-ins are a daily habit loop: the Events tab badge shows a count of unchecked-in events, creating a gentle notification nudge. The community feed provides variable reward (new stories, likes on their posts) that drives re-engagement throughout the day.

---

## 3. Craving Management Flow

```
┌────────────────────────┐
│  Craving Trigger       │
│  (User feels urge)     │
└──────────┬─────────────┘
           │
           ▼
┌────────────────────────┐
│  Tap FAB: "I'm Craving"│
│  (Orange pulsing button)│
└──────────┬─────────────┘
           │
           ▼
┌────────────────────────────┐
│  Craving Bottom Sheet       │
│  ┌────────────────────────┐│
│  │ "Stay Strong! You've   ││
│  │  got this"             ││
│  │ Timer: X since last    ││
│  └────────────────────────┘│
│  ┌─────────┐┌─────────┐┌─────────┐
│  │ Breathe ││ Distract ││  Talk   │
│  │ 🫁      ││ 🎮       ││  💬     │
│  └────┬────┘└────┬────┘└────┬────┘
│       │          │          │
└───────┼──────────┼──────────┼──────┘
        │          │          │
        ▼          ▼          ▼
  ┌───────────┐ ┌───────────┐ ┌───────────┐
  │ Breathing │ │ Tap Game  │ │ AI Coach  │
  │ Circle    │ │ 30s       │ │ Chat      │
  │ 4-7-8     │ │ counter   │ │ with      │
  │ 3 cycles  │ │           │ │ context   │
  └─────┬─────┘ └─────┬─────┘ └─────┬─────┘
        │              │              │
        └──────────────┼──────────────┘
                       │
                       ▼
              ┌─────────────────┐
              │  "Did it help?"  │
              │  ┌─────┐ ┌─────┐│
              │  │ Yes │ │ No  ││
              │  └──┬──┘ └──┬──┘│
              └─────┼───────┼───┘
                    │       │
                    ▼       ▼
           ┌──────────┐  ┌──────────────┐
           │ +5 XP    │  │ "Try another │
           │ Toast ✓  │  │  method"     │
           │ Close    │  │ Sheet stays  │
           └──────────┘  │ open         │
                         └──────────────┘
```

The craving management flow is the app's most critical user journey and is designed for speed and reassurance. The orange pulsing FAB is always visible and always one tap away from any screen, because cravings strike without warning and the user should not have to navigate through menus to find help. The bottom sheet opens in 250ms—fast enough to feel instantaneous. The three coping methods cater to different personality types and craving intensities: the breathing exercise for users who want a calm, meditative approach; the mini game for users who need physical distraction and kinetic energy release; and the AI coach for users who want verbal encouragement and personalized advice. The "Did it help?" feedback loop is essential for two reasons: it provides the app with data about which methods work for each user (enabling future personalization of method order), and it gives the user a sense of agency and progress even when the answer is "No"—because trying and failing is still better than not trying, and the app explicitly communicates this through the "That's okay" message.

---

## 4. Community Interaction Flow

```
┌─────────────────────┐
│  Community Tab       │
│  Feed / My Stories   │
└──────────┬───────────┘
           │
     ┌─────┴──────────────────────┐
     │                            │
     ▼                            ▼
┌──────────┐              ┌──────────────┐
│  Scroll   │              │  FAB: Share  │
│  Feed     │              │  Your Journey│
└────┬──────┘              └──────┬───────┘
     │                            │
     ├──── Tap Story Card ────────┤
     │                            ▼
     │                   ┌────────────────┐
     │                   │  Post Story     │
     │                   │  Screen         │
     │                   │  - Add photo    │
     │                   │  - Write text   │
     │                   │  - Select chips │
     │                   │  - Share        │
     │                   └────────┬────────┘
     │                            │ Shared
     │                            ▼
     │                   ┌────────────────┐
     │                   │  Toast: Story   │
     │                   │  shared! 🎉     │
     │                   │  → Feed (top)   │
     │                   └────────────────┘
     │
     ▼
┌──────────────────┐
│  Story Detail     │
│  Screen           │
│  - Full content   │
│  - Author profile │
│  - Life chips     │
│  - Like button    │
│  - Replies        │
└────┬─────────┬────┘
     │         │
     │         │ Tap "View Profile"
     │         ▼
     │  ┌──────────────┐
     │  │ Public Profile │
     │  │ - Stats        │
     │  │ - Achievements │
     │  │ - Add Friend   │
     │  └──────┬─────────┘
     │         │ Add Friend
     │         ▼
     │  ┌──────────────┐
     │  │ Request Sent  │
     │  │ "Pending"     │
     │  └──────────────┘
     │
     │ Like Story
     ▼
┌──────────────┐
│  Heart anim   │
│  +1 to count  │
│  Bounce 1.3×  │
└──────────────┘
```

The community interaction flow is designed around the principle of low-friction engagement. Reading the feed requires no action—scroll and absorb. Liking a story is a single tap with immediate visual feedback (the heart bounce animation). Writing a story is a deliberate action accessed through the FAB, with a guided form that breaks the process into clear steps: photo, text, tags. The flow from story to profile to friend request creates a natural social graph expansion—users discover each other through shared experiences, learn about the person behind the story, and connect if they feel affinity. The "My Stories" tab provides a personal journal view, reinforcing the user's own narrative of progress. Reply interactions are lightweight (no threading, just flat chronological replies) to keep conversations simple and approachable, avoiding the complexity that can discourage participation in more elaborate forum-style systems.

---

## 5. Event Participation Flow

```
┌───────────────────┐
│  Events Tab        │
│  Active Events     │
│  Carousel          │
└──────────┬─────────┘
           │
           ▼
┌───────────────────┐
│  Tap "Join" on     │
│  Event Card        │
└──────────┬─────────┘
           │ Joined ✓
           ▼
┌───────────────────┐
│  Event Challenge   │
│  Screen            │
│  - Banner + Info   │
│  - Progress Ring   │
│  - Check-in Button │
│  - Leaderboard     │
│  - Prizes          │
└──────┬─────────────┘
       │
       │ Daily: Tap "Check In Today"
       ▼
┌───────────────────┐
│  Event Check-in    │
│  Screen            │
│  - Camera preview  │
│  - Hold to Record  │
│  - Max 30 seconds  │
└──────┬─────────────┘
       │
       ▼
┌───────────────────┐
│  Preview Video     │
│  - Retake / Submit │
└──────┬─────────────┘
       │ Submit
       ▼
┌───────────────────┐
│  Upload + Toast ✓ │
│  "Day X complete!" │
└──────┬─────────────┘
       │
       │ (Admin reviews video)
       ▼
┌───────────────────┐     ┌───────────────────┐
│  Approved ✓       │     │  Rejected ✗        │
│  Push notif:      │     │  Push notif:       │
│  "Day X approved" │     │  "Please resubmit" │
│  Streak continues │     │  ┌───────────────┐ │
└───────────────────┘     │  │ Resubmit?     │ │
                          │  │ → Check-in    │ │
                          │  │   Screen      │ │
                          │  └───────────────┘ │
                          └───────────────────┘

       │ (Event completes: Day 30/30)
       ▼
┌───────────────────┐
│  Event Complete! 🎉│
│  Confetti + Badge  │
│  +500 XP awarded   │
│  Badge on profile  │
└───────────────────┘
```

The event participation flow is a habit loop with social accountability. The entry point is the Events tab, where the carousel of active events provides visual variety and a sense of ongoing community activity. Joining is a single tap—low commitment, high potential. The daily check-in is the core habit: the "Check In Today" button serves as a clear daily action, and the video requirement adds accountability without being onerous (30 seconds maximum). The hold-to-record interaction is intentional—it requires deliberate action, preventing accidental or thoughtless submissions. The admin review step ensures integrity; approved check-ins reinforce the user's streak, while rejected ones provide clear feedback and an easy resubmission path. The event completion celebration (confetti + badge + XP) provides a satisfying capstone that motivates participation in future events. The leaderboard within each event creates micro-competition that drives daily engagement beyond the intrinsic motivation of quitting.

---

## Appendix: Design Token Quick Reference

### Android Resource Mapping

| Design Token              | `colors.xml` Name          | Compose Color Name          |
|---------------------------|----------------------------|-----------------------------|
| `#0D0D1A`                 | `bg_primary`               | `BreathyBackground`         |
| `#1A1A2E`                 | `bg_surface`               | `BreathySurface`            |
| `#252540`                 | `bg_surface_variant`       | `BreathySurfaceVariant`     |
| `#00E676`                 | `accent_primary`           | `BreathyGreen`              |
| `#448AFF`                 | `accent_secondary`         | `BreathyBlue`               |
| `#B388FF`                 | `accent_purple`            | `BreathyPurple`             |
| `#FF9100`                 | `accent_orange`            | `BreathyOrange`             |
| `#FF4081`                 | `accent_pink`              | `BreathyPink`               |
| `#FFFFFF`                 | `text_primary`             | `BreathyOnSurface`          |
| `#B0B0CC`                 | `text_secondary`           | `BreathyOnSurfaceVariant`   |
| `#606080`                 | `text_disabled`            | `BreathyDisabled`           |
| `#FF5252`                 | `semantic_error`           | `BreathyError`              |
| `#FFD740`                 | `semantic_warning`         | `BreathyWarning`            |

### Dimension Resource Mapping

| Design Token   | `dimens.xml` Name      | Value |
|----------------|------------------------|-------|
| Space XS       | `spacing_xs`           | 4dp   |
| Space SM       | `spacing_sm`           | 8dp   |
| Space MD       | `spacing_md`           | 16dp  |
| Space LG       | `spacing_lg`           | 24dp  |
| Space XL       | `spacing_xl`           | 32dp  |
| Space XXL      | `spacing_xxl`          | 48dp  |
| Radius Card    | `radius_card`          | 16dp  |
| Radius Button  | `radius_button`        | 24dp  |
| Radius Sheet   | `radius_sheet`         | 24dp  |
| Radius Dialog  | `radius_dialog`        | 28dp  |
| Radius Input   | `radius_input`         | 12dp  |
| Radius Chip    | `radius_chip`          | 20dp  |

---

*End of Breathy UI/UX Specifications v1.0.0*
