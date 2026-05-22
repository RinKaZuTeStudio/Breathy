# Breathy — Product Requirements Document

> **Version:** 1.0.0  
> **Date:** 2025-03-04  
> **Author:** AppForge  
> **Status:** Final Draft  

---

## 1. Executive Summary

Breathy is a mission-driven Android application designed to be the ultimate quit-smoking companion for the modern era. Smoking remains one of the leading preventable causes of death worldwide, and yet millions of people struggle to break free from nicotine addiction because they lack the right combination of structure, support, and motivation. Breathy exists to close that gap by delivering a beautifully crafted, deeply personal experience that walks alongside every quitter from their first cigarette-free minute to their thousandth day of freedom. Our vision is a world where no one has to face the quit journey alone, and where technology transforms willpower into a sustainable, measurable, and even joyful process.

The core value proposition of Breathy rests on five interconnected pillars that work in concert to address every dimension of the quitting experience. **Health Tracking** provides the scientific backbone, showing users exactly how their body is recovering through a detailed timeline of physiological milestones — from heart rate normalization at the twenty-minute mark all the way to lung cancer risk halving after a decade. **Community Support** ensures that no one quits in isolation; users can share stories, offer encouragement, and build genuine friendships with fellow quitters who understand the struggle firsthand. **AI Coaching** leverages the power of OpenAI's language models to deliver personalized, context-aware guidance that knows your quit date, your craving patterns, and your emotional state, providing support precisely when willpower wavers. **Gamification** transforms the often-grueling quit process into an engaging journey of experience points, levels, achievements, and daily rewards that make progress tangible and addictive in the healthiest possible way. Finally, **Event Challenges** bring the community together around structured activities — like push-up challenges — that replace the ritual of smoking with healthier habits and create accountability through daily check-ins and video proof.

Breathy is built with Kotlin and Jetpack Compose on Material 3, backed by Firebase's robust suite of authentication, database, storage, and cloud functions, and enhanced by OpenAI's API for intelligent coaching conversations. The app targets Android devices running SDK 24 (Android 7.0) and above, with optimization for SDK 34 (Android 14). Monetization follows a lightweight, user-friendly model: a one-dollar "Support me" subscription that removes interstitial ads and funds ongoing development, supplemented by AdMob open-app and interstitial advertisements for free users. Every design decision, from the breathing exercise animation to the craving bottom sheet, is rooted in behavioral science and crafted to reduce friction, increase engagement, and ultimately help more people stay smoke-free for life.

---

## 2. User Personas

### Persona A: "Determined Dan"

| Attribute | Detail |
|---|---|
| **Age** | 35 |
| **Occupation** | Warehouse Supervisor |
| **Location** | Midwest USA |
| **Smoking History** | 15 years, ~20 cigarettes/day |
| **Quit Attempts** | 4 serious attempts in the past 5 years |
| **Tech Comfort** | Moderate — uses a smartphone daily but avoids complex apps |

**Story:** Dan has tried quitting smoking four times over the past five years. His longest streak was 47 days, but each time he relapsed during a stressful period at work or after a night out with friends who still smoke. He knows the health risks, he knows the financial cost, and he genuinely wants to quit — but willpower alone has never been enough. Dan craves structure and accountability. He needs an app that doesn't just tell him to stop but gives him a concrete framework for tracking progress, understanding what his body is going through, and holding himself to a commitment. He's skeptical of "feel-good" features that lack substance, but he responds powerfully to measurable data and visible milestones that prove his effort is paying off.

**Goals:**
- Achieve a sustained quit lasting at least one year
- Understand the physiological recovery process so he can reframe cravings as healing
- Save the significant money he currently spends on cigarettes (estimated $2,500+/year)
- Build a streak he's proud of and unwilling to break

**Pain Points:**
- Loses motivation after the initial "honeymoon phase" of quitting fades
- No structured system to quantify progress beyond counting days
- Relapses during acute stress and lacks a go-to coping mechanism
- Feels isolated because none of his close friends are quitting

**How Breathy Addresses Dan's Needs:**
The health timeline is Dan's anchor — seeing that his heart rate normalizes within 20 minutes and his circulation improves at two weeks gives him a scientific framework that transforms abstract willpower into observable, body-level progress. The craving bottom sheet provides him with an immediate, structured response when urges strike: the breathing exercise gives him a physical ritual to replace the cigarette break, the mini-game distracts his hands and mind, and the AI Coach offers personalized encouragement grounded in his actual quit data. The money-saved calculator speaks directly to his practical side, turning every avoided cigarette into a tangible dollar amount. Achievements like "First Day," "One Week," and "Money Saver" create accountability through visible milestones he doesn't want to lose, and the streak counter acts as a commitment device that makes relapse psychologically expensive.

**Key Features Dan Would Use Most:**
Health timeline, craving logger with breathing exercise, money saved tracker, achievement badges, daily motivation notifications, and the leaderboard for competitive accountability.

---

### Persona B: "Social Sarah"

| Attribute | Detail |
|---|---|
| **Age** | 28 |
| **Occupation** | Marketing Coordinator |
| **Location** | Urban metro (Austin, TX) |
| **Smoking History** | 7 years, ~12 cigarettes/day |
| **Quit Attempts** | 2 attempts, longest 3 weeks |
| **Tech Comfort** | High — active on Instagram, TikTok, Discord |

**Story:** Sarah is a social butterfly who started smoking in college as a bonding activity with friends. Her identity is deeply intertwined with her social life, and cigarettes have been a constant companion at parties, coffee breaks, and late-night conversations. She's tried quitting twice before, but each time the social pressure proved overwhelming — her friends still smoke, the ritual of stepping outside for a cigarette is deeply embedded in her routine, and the loneliness of being the only non-smoker in her circle drives her back. Sarah thrives on encouragement, validation, and the energy of a community. She doesn't need cold data; she needs warm human connection that makes her feel seen, supported, and celebrated. If she can find a tribe of people who understand what she's going through and cheer her on, she believes she can finally make the quit stick.

**Goals:**
- Quit smoking without losing her social identity
- Find a supportive community of fellow quitters who "get it"
- Share her journey publicly and inspire others
- Replace the social ritual of smoking with healthier communal activities

**Pain Points:**
- Social environments are her biggest trigger (parties, bars, friend gatherings)
- Feels lonely and misunderstood as the "only one quitting" in her circle
- Craves real-time encouragement during weak moments, especially late at night
- Needs more than a tracker — she needs a tribe

**How Breathy Addresses Sarah's Needs:**
The community story feed is Sarah's lifeline. Reading success stories from people who've been smoke-free for months or years gives her proof that freedom is possible, while posting her own milestones invites the encouragement she needs to keep going. The ability to like and reply to stories creates the conversational, emotionally rich environment she craves. Friend requests and one-on-one real-time chat with typing indicators replicate the immediacy and intimacy of texting a friend who's awake at 2 AM when a craving hits hard. Public profiles that showcase quit stats let Sarah build a new identity — not as "the one who still smokes" but as "Sarah, 43 days smoke-free, Level 7 Breathy Champion." Event challenges give her the structured social activities that replace the cigarette break ritual, turning "let's go outside for a smoke" into "let's check in for the Push-up Challenge together." The AI Coach serves as her always-available cheerleader, offering a warm, empathetic voice during moments when no human friend is online.

**Key Features Sarah Would Use Most:**
Community story feed, friend system with real-time chat, event challenges, public profile, AI Coach for late-night support, and social notifications.

---

### Persona C: "Tech-Savvy Tom"

| Attribute | Detail |
|---|---|
| **Age** | 42 |
| **Occupation** | Software Engineer |
| **Location** | San Francisco Bay Area |
| **Smoking History** | 20 years, ~25 cigarettes/day |
| **Quit Attempts** | 1 attempt with a competitor app (lasted 2 months) |
| **Tech Comfort** | Very high — early adopter, quantified-self enthusiast |

**Story:** Tom is a numbers guy. He tracks his steps, his sleep, his screen time, his calorie intake — and yet the one metric that matters most, his smoking habit, has stubbornly resisted his attempts to quantify and conquer it. He tried a competitor quit-smoking app two years ago and lasted two months, but found it shallow: the gamification felt gimmicky, the data was too simplistic, and the AI was clearly a chatbot reading from a script. Tom wants depth. He wants an app that treats his quit journey the way he treats his code — with precision, feedback loops, and measurable optimization. If he can see a dashboard of his progress, understand the statistical trends in his craving patterns, and earn real achievements that reflect genuine effort, he's all in. But if the app feels like it was designed for casual users who just want a "you can do it!" sticker, he'll bounce.

**Goals:**
- Quit smoking using a data-driven, systems-thinking approach
- Analyze craving patterns to identify and eliminate triggers
- Compete on leaderboards and earn achievements that reflect real effort
- Integrate the quit journey into his broader quantified-self ecosystem

**Pain Points:**
- Existing quit apps are too simplistic and lack analytical depth
- No way to correlate craving data with time, context, or coping method effectiveness
- Gamification systems in other apps feel shallow and unearned
- Wants AI that's actually intelligent, not a glorified FAQ bot

**How Breathy Addresses Tom's Needs:**
The craving statistics feature is Tom's playground. Every craving he logs — including the time, the coping method he chose, and whether he succeeded — feeds into a personal analytics dashboard that reveals his trigger patterns (e.g., "75% of your cravings occur between 9 PM and 11 PM") and coping method effectiveness ("Breathing exercises work 82% of the time for you, vs. 61% for mini-games"). The XP and leveling system gives him the kind of progression mechanics he appreciates in well-designed games — not just a streak counter, but a multi-dimensional reward system that tracks and values diverse achievements. The global leaderboard satisfies his competitive instincts, while the event challenges with video proof and admin review provide the rigorous accountability he respects. The AI Coach's context-awareness — knowing his quit date, craving history, and current progress — delivers the intelligent, personalized interaction he craves rather than generic platitudes. And the health timeline's scientifically grounded milestones appeal to his analytical mind, providing evidence-based proof of physiological recovery that he can't argue with.

**Key Features Tom Would Use Most:**
Craving statistics and analytics, XP/leveling system, global leaderboard, AI Coach (context-aware), health timeline, event challenges with video check-ins, and achievement badges.

---

## 3. Functional Requirements

### 3.1 Core Quit Tracking

The core quit tracking system is the foundational engine of Breathy, responsible for capturing, storing, and presenting every data point that defines a user's quit journey. This system must be robust enough to handle both instant-quit and gradual-quit scenarios while presenting all derived metrics — money saved, cigarettes avoided, health milestones — in a clear, motivating, and scientifically accurate format.

**Quit Date Configuration:**
- Users must be able to set a quit date during onboarding or from the profile screen at any time. The quit date serves as the epoch (time zero) from which all time-based calculations derive. For an **instant quit**, the date is set to the current moment, and the clock begins immediately. For a **gradual quit**, the user specifies a target quit date in the future, and the app provides a tapering schedule that gradually reduces the daily cigarette allowance between the current date and the target date. The gradual quit mode must calculate a linear reduction from the user's current cigarettes-per-day to zero, presenting the daily allowance on the home screen so the user knows their limit each day. The system must persist the quit type (instant or gradual), the original quit date, and the target quit date (for gradual) in Firestore under the user's document.

**Smoking Habit Input:**
- During onboarding (and editable later from profile settings), users must input three critical values: **cigarettes per day** (integer, range 1–100), **price per pack** (decimal, currency-formatted based on device locale), and **cigarettes per pack** (integer, typically 20 but variable by country). These three values form the basis for all financial and consumption calculations. The system must validate that all inputs are positive, non-zero numbers and provide inline error messages for invalid entries. Default values should be offered as placeholders (e.g., 20 cigarettes/day, $8.00/pack, 20 cigarettes/pack) to reduce friction.

**Derived Metrics Calculation:**
- **Money Saved:** Calculated as `(cigarettes_per_day * price_per_pack / cigarettes_per_pack) * days_smoke_free`. This must update in real-time on the home screen, displaying both the total amount saved and a "per-day" savings rate. The currency symbol must match the user's device locale. For gradual-quit users, the formula adjusts to account for the tapering reduction in daily consumption.
- **Cigarettes Avoided:** Calculated as `cigarettes_per_day * days_smoke_free`. For gradual-quit users, this reflects the difference between the baseline consumption and the reduced allowance over time. The display should show both the total count and a contextual comparison (e.g., "That's 15 packs not smoked!").
- **Days Smoke-Free:** A simple day-count from the quit date (or original start date for gradual quitters), displayed prominently on the home screen with a large, celebratory number. For users still in the gradual-quit phase, this shows "Days on Plan" instead.

**Health Timeline:**
The health timeline is one of Breathy's most powerful motivational tools, presenting a scrollable, visually rich list of physiological recovery milestones that the user's body achieves over time. Each milestone must include: a timestamp (e.g., "20 minutes"), a title (e.g., "Heart Rate Normalizes"), a descriptive paragraph explaining the physiological change, an icon or illustration, and a visual indicator of whether the milestone has been achieved (checkmark/bright) or is still upcoming (dimmed/locked). The complete milestone list is:

| Time Since Quit | Milestone | Description |
|---|---|---|
| 20 minutes | Heart Rate Normalizes | Your heart rate and blood pressure begin to drop to normal levels, reducing cardiovascular stress immediately. |
| 8 hours | Carbon Monoxide Levels Drop | Carbon monoxide levels in your blood decrease by half, allowing oxygen levels to return to normal. Your cells begin receiving the oxygen they need. |
| 24 hours | Heart Attack Risk Decreases | Your risk of heart attack begins to decrease as the effects of carbon monoxide and nicotine on your cardiovascular system begin to reverse. |
| 48 hours | Taste and Smell Improve | Nerve endings begin to regrow, and your ability to taste and smell is noticeably enhanced. Food begins to taste richer and more complex. |
| 72 hours | Breathing Becomes Easier | Bronchial tubes relax and lung capacity increases, making breathing feel noticeably easier and deeper. |
| 2 weeks | Circulation Improves | Blood circulation throughout your body improves significantly, making walking and exercise easier and more enjoyable. |
| 1 month | Lung Function Improves | Lung cilia regrow and lung function increases by up to 30%, significantly improving respiratory health and reducing coughing. |
| 1–9 months | Coughing Decreases | Cilia in the lungs fully regrow, reducing the frequency and severity of coughing, sinus congestion, and shortness of breath. |
| 1 year | Heart Disease Risk Halved | Your risk of coronary heart disease is now half that of a smoker's, a dramatic reduction in cardiovascular danger. |
| 5 years | Stroke Risk Equal to Non-Smoker | Your risk of having a stroke is now reduced to that of a non-smoker, a major milestone in vascular health recovery. |
| 10 years | Lung Cancer Risk Halved | Your risk of lung cancer falls to about half that of a smoker, and your risk of cancers of the mouth, throat, esophagus, and pancreas also decreases. |
| 15 years | Heart Disease Risk Equal to Non-Smoker | Your risk of coronary heart disease is now equivalent to that of someone who has never smoked — full cardiovascular recovery. |

The timeline must auto-scroll to the most recently achieved milestone on launch, with achieved milestones displayed in a vibrant, completed style and upcoming milestones shown in a dimmed, aspirational style. Tapping an upcoming milestone reveals its description and a "You'll reach this in X days" countdown.

---

### 3.2 Craving Management

The craving management system is Breathy's real-time intervention layer — the feature that activates when a user is actively struggling with the urge to smoke and needs immediate, practical support. This system must be accessible from every screen in the app via a persistent floating action button (FAB), ensuring that help is never more than one tap away. When the user taps the craving FAB, a bottom sheet slides up presenting three distinct coping methods, each designed to address a different aspect of the craving experience: physiological, cognitive, and emotional.

**Craving Bottom Sheet — Coping Methods:**

1. **Breathing Exercise (Animated Circle):** A full-screen breathing animation guides the user through a structured breathing cycle: inhale for 4 seconds, hold for 4 seconds, exhale for 4 seconds, hold for 4 seconds (box breathing). The visual is a smoothly expanding and contracting circle with a gradient color shift (e.g., blue expanding for inhale, teal holding, green contracting for exhale, teal holding). Text prompts ("Breathe In...", "Hold...", "Breathe Out...", "Hold...") are displayed inside the circle. The exercise runs for a minimum of 3 cycles (48 seconds) and can be extended by the user. Upon completion, the user is prompted to rate whether the craving passed (success) or persists (failure).

2. **Mini-Game (Tap Counter):** A simple but engaging distraction game where the user must tap a target as many times as possible within 30 seconds. The target moves to a random position on screen after each tap, and a running counter displays the score with haptic feedback on each tap. The game leverages the behavioral principle that physical engagement disrupts the mental loop of craving. After the timer expires, the user is shown their score and prompted to rate craving success/failure.

3. **AI Coach Chat:** Opens the AI Coach chat interface pre-populated with a contextual message like "I'm having a craving right now. Help me through this." The AI Coach responds immediately with empathetic, personalized support drawing on the user's quit data (e.g., "You've been smoke-free for 12 days — that's huge! Your body is actively healing right now. What triggered this craving?"). This is the most emotionally supportive option and is particularly effective for users who need verbal reassurance rather than physical or cognitive distraction.

**Craving Logging:**
After using any coping method (or choosing to skip all three), the user is presented with a quick logging form that captures: **timestamp** (auto-filled), **coping method used** (breathing / mini-game / AI coach / none), **craving intensity** (1–5 scale with emoji icons), and **outcome** (success / failure / skipped). This data is stored in a `cravings` subcollection under the user's Firestore document and feeds directly into the craving statistics dashboard. The logging step must be optional (user can dismiss it) but should be gently encouraged with a brief message like "Logging helps you understand your patterns — just one tap!"

**Craving Statistics:**
The craving statistics screen presents an analytical dashboard showing: total cravings logged, average intensity, success rate (percentage of cravings marked as "success"), most common trigger times (grouped by hour of day in a bar chart), coping method effectiveness comparison (pie chart of success rates by method), and a 7-day/30-day trend line of craving frequency. This screen must update in real-time as new cravings are logged and must be accessible from the profile screen and the home screen quick-stats area.

---

### 3.3 Gamification & Rewards

Gamification is the psychological engine that transforms the quit journey from a grueling test of endurance into an engaging, rewarding adventure. The system is designed around three interlocking currencies and progression mechanics — XP, coins, and levels — that reward diverse behaviors (not just time smoke-free) and create multiple motivational hooks for different user types. Every meaningful action in the app earns XP, which drives level progression and unlocks achievements. Coins provide a lighter, more frequent reward that can be claimed daily, giving users a reason to open the app every single day.

**XP (Experience Points) System:**
XP is earned for a wide range of activities across the app, ensuring that every positive behavior is recognized and rewarded. The XP earning rules are:

| Action | XP Earned | Frequency |
|---|---|---|
| Complete a day smoke-free | 50 XP | Daily |
| Log a craving (any outcome) | 10 XP | Per craving |
| Successfully resist a craving | 25 XP | Per craving |
| Post a community story | 20 XP | Per story |
| Like a community story | 5 XP | Per like (max 50/day) |
| Reply to a community story | 10 XP | Per reply |
| Complete AI Coach session | 15 XP | Per session |
| Join an event | 30 XP | Per event |
| Daily event check-in | 20 XP | Per day |
| Claim daily reward | 5 XP | Daily |
| Send a friend request | 5 XP | Per request |
| Add a friend (request accepted) | 15 XP | Per friend |

**Level System:**
Levels are determined by cumulative XP thresholds, creating a clear progression path that takes weeks or months to traverse. The level thresholds are:

| Level | XP Required | Title |
|---|---|---|
| 1 | 0 | Fresh Start |
| 2 | 100 | First Steps |
| 3 | 300 | Gaining Ground |
| 4 | 600 | Steady Climb |
| 5 | 1,000 | Committed |
| 6 | 1,500 | Resilient |
| 7 | 2,200 | Unbreakable |
| 8 | 3,000 | Freedom Fighter |
| 9 | 4,000 | Champion |
| 10 | 5,500 | Legend |
| 11–20 | +1,500 per level | Ascending tiers |

Each level-up must trigger a celebratory animation (confetti, sound effect, full-screen overlay) and unlock any associated achievements. The current level and XP progress bar are displayed on the home screen, profile screen, and leaderboard entries.

**Coins System:**
Coins are a secondary, lighter currency earned primarily through the daily reward claim. The daily reward grants a random coin amount between 10 and 50 coins, with a streak bonus that increases the base amount by 10% per consecutive day of claiming (capped at 200% bonus). Coins currently serve as a collection metric visible on the profile, with future expansion planned for cosmetic customization (avatar frames, themes). The daily reward claim screen must display: current streak, today's coin reward amount, streak bonus percentage, and a "Claim" button that triggers a satisfying animation. If the user misses a day, the streak resets to zero.

**Achievements / Badges System:**
Achievements are permanent, visible badges earned for reaching specific milestones or completing specific actions. Each achievement must include: a unique ID, a name, a description, an icon, an unlock condition, and a timestamp of when it was earned. Achievements are displayed on the user's profile in a grid layout, with earned achievements shown in full color and unearned achievements shown as locked silhouettes (tapping reveals the unlock condition). The initial achievement set is:

| Achievement | Condition | Category |
|---|---|---|
| First Breath | Complete Day 1 smoke-free | Time |
| One Week Strong | Reach 7 days smoke-free | Time |
| One Month Free | Reach 30 days smoke-free | Time |
| One Year Clean | Reach 365 days smoke-free | Time |
| Money Saver | Save $100 total | Financial |
| Big Saver | Save $1,000 total | Financial |
| Craving Crusher | Successfully resist 50 cravings | Craving |
| Breathing Master | Complete 100 breathing exercises | Craving |
| Community Star | Receive 50 total likes on stories | Community |
| Storyteller | Post 10 community stories | Community |
| Social Butterfly | Add 10 friends | Social |
| Event Champion | Complete an event challenge | Events |
| Streak Keeper | Claim daily reward 30 days in a row | Gamification |
| Level 5 | Reach Level 5 | Progression |
| Level 10 | Reach Level 10 | Progression |
| AI Explorer | Have 50 AI Coach conversations | AI Coach |

New achievements may be added in future updates without requiring an app update, driven by a remote configuration in Firestore.

---

### 3.4 Community

The community module is Breathy's emotional heart — the place where quitters find inspiration, connection, and the irreplaceable power of shared experience. It centers around a story feed that surfaces content from the global Breathy community, creating a scrolling mosaic of triumph, struggle, and solidarity. The design philosophy is borrowed from successful social platforms but tempered by the unique needs of a recovery community: content must be encouraging rather than performative, authentic rather than curated, and supportive rather than competitive.

**Story Feed:**
- The community home screen displays a vertically scrolling feed of stories posted by all Breathy users (globally). Each story card shows: the author's nickname and profile photo, their current days smoke-free and level, the story text (up to 500 characters), an optional attached photo (displayed as a thumbnail that expands on tap), a timestamp ("2 hours ago"), a like count with a heart icon, and a reply count. The feed must be ordered by recency (newest first) with a pull-to-refresh gesture. Pagination must load 20 stories at a time, with automatic loading of the next batch when the user scrolls near the bottom. Stories older than 90 days may be archived and removed from the main feed to maintain freshness. The feed must be accessible to all users (including those without a community profile) but posting requires a minimum of Day 1 achievement to prevent spam.

**Post Stories:**
- Users can create a new story by tapping the compose button (pen icon in the top-right of the community screen). The compose screen includes: a text input area (multiline, 500-character limit with live counter), an optional photo attachment button (launching the device camera or gallery, with a 5MB size limit and JPEG/PNG format requirement), and a "Post" button. Before posting, the system must perform content moderation via a Firebase Cloud Function that checks for prohibited content (spam, self-harm references, substance promotion) using keyword filtering and, optionally, the OpenAI moderation endpoint. Stories that pass moderation are immediately published to the feed. Stories that are flagged are held for admin review and the user is shown a "Your story is being reviewed" message.

**Like Stories:**
- Users can like a story by tapping the heart icon on the story card. Each user can like a story at most once; tapping again removes the like (toggle behavior). The like count updates in real-time using Firestore listeners. Liking a story earns the user 5 XP (capped at 50 XP per day from likes) and sends a push notification to the story author ("[Name] liked your story!"). The liked state must persist across sessions and be indicated by a filled heart icon.

**Reply to Stories:**
- Tapping the reply count or a reply icon opens the story detail screen, which shows the original story at the top followed by a chronological list of replies. Each reply displays: the replier's nickname and photo, reply text (up to 200 characters), and a timestamp. Users can type and submit replies at the bottom of the screen. Replies must follow the same content moderation pipeline as stories. Replying earns 10 XP (no daily cap) and sends a push notification to the story author ("[Name] replied to your story!").

**Public Profiles:**
- Tapping a user's nickname or photo on any story or reply navigates to their public profile screen. The public profile displays: profile photo, nickname, location (if set), days smoke-free, money saved, cigarettes avoided, current level and XP, achievement badge grid (earned only), and a "Add Friend" button (if not already friends) or a "Message" button (if already friends). The public profile must be accessible from the community feed, leaderboard, event participant lists, and chat headers.

---

### 3.5 Social Features

The social features layer provides the one-on-one interpersonal connection that complements the broader community experience. While the community feed offers broadcast-style inspiration, the social features — friend requests, friend management, and real-time chat — create the deep, personal bonds that sustain users through their hardest moments. A fellow quitter who's available for a 2 AM chat when a craving hits can be the difference between relapse and resilience.

**Friend Requests:**
- Any user can send a friend request to any other user by visiting their public profile and tapping "Add Friend." The request creates a document in a `friend_requests` Firestore subcollection, containing: sender UID, sender nickname, sender photo URL, recipient UID, timestamp, and status (pending/accepted/rejected). The recipient receives a push notification ("[Name] wants to be your friend!") and sees the request on their friends screen under a "Pending Requests" tab. Accepting a friend request creates a bi-directional friendship link (both users appear in each other's friend list) and triggers push notifications to both users ("You and [Name] are now friends!"). Rejecting a friend request silently removes the request (the sender is not notified of rejection). Each user can have a maximum of 200 friends to prevent spam and maintain manageable social graphs.

**Friend List:**
- The friends screen displays two tabs: "Friends" and "Pending." The Friends tab shows a scrollable list of the user's friends, each displaying: profile photo, nickname, days smoke-free, and current level. Tapping a friend opens their public profile; swiping right or tapping a chat icon opens the one-on-one chat. The Pending tab shows incoming friend requests with "Accept" and "Decline" buttons. The friend list is ordered alphabetically by default, with an optional sort by days smoke-free or level. A search bar at the top allows filtering by nickname.

**One-on-One Real-Time Chat:**
- The chat interface follows standard messaging conventions: messages are displayed in a chronological list with the sender's messages aligned right (blue bubble) and the friend's messages aligned left (gray bubble). Each message bubble shows the text content and a timestamp. The chat input bar at the bottom includes a text field (up to 1,000 characters), a send button, and optional emoji quick-access. Messages are stored in a `chats/{chatId}/messages` Firestore subcollection, where `chatId` is generated as the sorted concatenation of the two users' UIDs (ensuring a consistent chat ID regardless of who initiates). Messages must be delivered in real-time using Firestore `addSnapshotListener` on the messages collection, ordered by timestamp. The chat screen must display the last 50 messages on load, with older messages loaded on scroll-up (pagination).

**Typing Indicators:**
- When a user is actively typing in the chat input field (detected by text changes with a 1-second debounce), a `typing` document in the chat's Firestore subcollection is updated with the user's UID and a timestamp. The other user's chat screen displays a "typing..." indicator below the last message (animated three dots). When the user stops typing for 3 seconds or sends the message, the typing indicator is cleared. This feature must use Firestore's real-time listeners and must not generate more than 1 write per second to avoid excessive billing.

**Push Notifications for Messages:**
- When a message is sent, a Firebase Cloud Function is triggered to send a push notification to the recipient's device via Firebase Cloud Messaging (FCM). The notification displays: sender's nickname, message preview (first 100 characters), and tapping the notification opens the chat screen with that friend. If the recipient has the chat screen open and in the foreground, the notification is suppressed (the message appears in real-time via the listener). The notification must include the sender's UID and chat ID in its data payload for deep-linking. Users can disable chat notifications in their profile settings.

---

### 3.6 Leaderboard

The leaderboard module provides a competitive, aspirational dimension to the quit journey. By surfacing the most successful quitters in the global community and among friends, the leaderboard creates social proof ("others have done this, and they're thriving") and friendly competition ("I want to catch up to that person"). The design must balance motivational visibility with sensitivity — the leaderboard celebrates achievement without shaming those who are earlier in their journey.

**Global Ranking:**
- The global leaderboard ranks all Breathy users by **days smoke-free** in descending order. Each leaderboard entry displays: rank number, profile photo, nickname, days smoke-free, current level, and total XP. The top 3 users receive special styling (gold, silver, bronze badges and highlighted backgrounds). The leaderboard must paginate in batches of 50 entries. The current user's entry must always be visible (either in the current page or as a pinned entry at the bottom showing their global rank). The global leaderboard must update in real-time using Firestore listeners on the `users` collection, ordered by a `daysSmokeFree` field that is recalculated daily via a scheduled Cloud Function.

**Filter by Friends:**
- A toggle or tab at the top of the leaderboard screen switches between "Global" and "Friends" views. The Friends view shows only the user's friends ranked by days smoke-free, with the same entry format as the global view. The Friends view provides a more intimate, achievable competitive context — rather than comparing against a stranger with 5 years smoke-free, users can see how they stack up against peers at similar stages. The current user is included in the Friends leaderboard with their rank highlighted.

**XP and Level Display:**
- In addition to days smoke-free, each leaderboard entry shows the user's current level (with title, e.g., "Level 7 — Unbreakable") and total XP. Tapping a leaderboard entry navigates to that user's public profile. The leaderboard must support sorting by an alternative metric (XP or level) via a dropdown selector, with the default sort being days smoke-free.

---

### 3.7 Events & Challenges

Events and challenges are Breathy's structured, time-bound activities that bring the community together around shared goals beyond just staying smoke-free. By replacing the ritual and community of smoking with healthier alternatives — exercise challenges, mindfulness streaks, gratitude journaling — events provide concrete, positive habits that fill the void left by cigarettes and create accountability through social proof and video verification.

**Admin-Created Events:**
- Events are created by Breathy administrators via a Firebase Cloud Function or a web-based admin panel. Each event document in Firestore contains: event ID, title (e.g., "Push-up Challenge 2025"), description (up to 1,000 characters), start date, end date, daily requirement (e.g., "30 push-ups per day"), prize description (e.g., "500 coins + Champion badge"), banner image URL, and status (upcoming/active/completed). Events are displayed on the Events tab in a card layout showing the banner image, title, date range, participant count, and a "Join" button (for upcoming/active events) or "View Results" button (for completed events). Active events are highlighted with a pulsing indicator.

**User Join Flow:**
- Tapping "Join" on an active or upcoming event adds the user to the event's `participants` subcollection and awards 30 XP. The user's Events tab then shows the joined event with a daily check-in button. Users can join an event at any point during its active period, but only check-ins from their join date forward count toward their ranking.

**Daily Check-In with Video Proof:**
- Each day during an active event, participants must submit a check-in by tapping the "Check In" button on the event card. This launches the device camera in video recording mode, limited to 15 seconds. The user records themselves completing the daily requirement (e.g., doing push-ups) and submits the video. The video is uploaded to Firebase Storage under `events/{eventId}/checkins/{userId}/{date}.mp4`, and a check-in document is created in Firestore containing: user ID, date, video URL, and status (pending/approved/rejected). The check-in screen must display the current day number, total days checked in, and the event's daily requirement as a reminder. If the user misses a day, their streak resets but they can continue checking in on subsequent days (their ranking reflects total approved check-ins, not streak length).

**Admin Review of Check-Ins:**
- Administrators access a review dashboard (web-based or in-app) that displays all pending check-ins for each event, organized by date. Each check-in entry shows: user nickname, date, video thumbnail (tappable to play), and Approve/Reject buttons. Approved check-ins update the document status to "approved" and award the user 20 XP. Rejected check-ins update the status to "rejected" with an optional reason, and the user is notified via push notification ("Your check-in for [Event] was not approved. Keep trying!"). The review must be completed within 48 hours of submission.

**Rankings Within Events:**
- Each event maintains a real-time ranking of participants based on the number of approved check-ins (descending). Ties are broken by the earliest submission time. The ranking is displayed on the event detail screen, showing: rank, nickname, profile photo, and approved check-in count. The current user's rank is highlighted or pinned for easy reference.

**Prizes for Completion:**
- When an event reaches its end date, all participants who achieved a qualifying threshold (e.g., 80% approved check-ins) receive the event prize: coins (added to their balance), XP bonus (e.g., 200 XP), and the "Event Champion" achievement badge. Winners are announced via a push notification and a congratulatory card in the event detail screen.

---

### 3.8 AI Coach

The AI Coach is Breathy's intelligent, empathetic companion — an OpenAI-powered conversational agent that provides personalized support, motivation, and guidance based on the user's unique quit journey. Unlike a generic chatbot, the AI Coach is deeply context-aware: it knows the user's quit date, their craving patterns and statistics, their current health milestones, their coping method preferences, and their emotional trajectory. This contextual awareness transforms every conversation from a generic wellness script into a deeply personal, genuinely helpful interaction that feels like talking to a supportive friend who happens to have perfect recall.

**Chat Interface:**
- The AI Coach is accessed via a dedicated tab or prominent entry point on the home screen. The chat interface follows standard messaging conventions: user messages on the right (blue bubble), AI responses on the left (purple/gradient bubble with a small Breathy logo avatar). The input bar includes a text field (up to 500 characters per message) and a send button. The AI Coach's responses must appear with a typing indicator (animated dots for 1–3 seconds) to simulate natural conversation pacing. The chat history is persisted in Firestore under `users/{userId}/ai_coach_messages` and loaded on screen open, displaying the most recent 50 messages with pagination for older messages. The AI Coach must greet the user with a context-aware opening message (e.g., "Hey Dan! You're 12 days smoke-free — that's amazing! How are you feeling today?") rather than a generic "How can I help you?"

**Context-Aware System Prompt:**
- Each AI Coach conversation is initialized with a system prompt that injects the user's current context. The system prompt must include: the user's nickname, quit date, days smoke-free, total cravings logged, craving success rate, most used coping method, current level and XP, recent health milestones achieved, and any recent community activity. This context is refreshed from Firestore each time a new conversation session begins. The system prompt must instruct the AI to: (1) be warm, encouraging, and non-judgmental; (2) reference the user's specific data when relevant ("You've saved $147 so far!"); (3) never provide medical advice, but instead recommend consulting a healthcare professional for medical concerns; (4) keep responses concise (under 150 words); (5) proactively suggest coping strategies during cravings; and (6) celebrate milestones enthusiastically.

**Rate Limiting:**
- Free users are limited to **10 AI Coach messages per day** to manage OpenAI API costs. A counter in the user's Firestore document tracks daily message count, reset at midnight local time. When a free user reaches the limit, the AI Coach displays a friendly message: "You've used all your chats for today! Come back tomorrow for more support, or upgrade to Breathy Supporter for unlimited chats." Users who have purchased the "Support me" subscription receive **unlimited** AI Coach messages. The rate limit must be enforced on the server side (Firebase Cloud Function) to prevent client-side bypass.

**Tone and Safety:**
- The AI Coach must maintain an encouraging, supportive, and empathetic tone at all times. It must never be dismissive, sarcastic, or judgmental about relapses. If a user indicates they've relapsed, the AI Coach must respond with compassion ("It's okay — quitting is one of the hardest things you'll ever do, and a slip doesn't erase your progress. Let's talk about what happened and how we can prevent it next time.") and suggest re-engaging with coping tools. The AI Coach must detect and respond appropriately to crisis language (e.g., expressions of self-harm) by providing crisis helpline information and recommending professional support.

---

### 3.9 Subscription

The subscription module implements Breathy's lightweight, user-friendly monetization strategy. Rather than a recurring subscription that creates ongoing financial commitment (and the friction that comes with it), Breathy offers a one-time "Support me" purchase for one dollar. This approach aligns with the app's mission of accessibility — no one should be locked out of core features by a paywall — while providing a voluntary, low-barrier revenue stream from users who value the app and want to support its continued development.

**"Support me" Purchase:**
- The purchase is presented as a one-time, one-dollar transaction through Google Play Billing. The offering must be clearly described: "Remove interstitial ads and support Breathy's development for just $1 — forever." The purchase flow is triggered from a banner in the app's settings screen, a prompt after the 7-day milestone ("Loving Breathy? Support us for just $1!"), or a contextual prompt when the user encounters an interstitial ad. The purchase uses Google Play Billing's `BillingClient` API with a `SkuDetails` query for the "support_me" product ID. Upon successful purchase verification (via server-side receipt validation through a Firebase Cloud Function), the user's Firestore document is updated with `isSupporter: true`.

**Ad Removal:**
- Users with `isSupporter: true` must not be shown interstitial ads. The AdMob SDK initialization must check the supporter flag before requesting interstitial ad units. Open-app ads (displayed on cold start) remain for all users including supporters, as they are non-intrusive and help cover baseline costs. The supporter flag must be cached locally to prevent ad flicker on startup before the Firestore listener resolves.

**Google Play Billing Integration:**
- The billing flow must handle all standard edge cases: network errors during purchase (show retry prompt), purchase pending (show "Processing..." state), already purchased (restore purchase and update UI), and refund (detect via `PurchasesUpdatedListener` and revert supporter status). The app must implement `BillingClient.stateListener` to handle billing service disconnections gracefully. All purchase verification must occur server-side to prevent tampering.

---

### 3.10 Profile

The profile module is the user's personal dashboard — the place where they can view their complete quit identity, customize their public presence, and manage their account settings. The profile must balance comprehensiveness with clarity, presenting the user's journey at a glance while providing easy access to deeper data and account management functions.

**Edit Profile:**
- Users can edit their **nickname** (3–30 characters, no profanity filter via Cloud Function), **profile photo** (selected from gallery or camera, cropped to a 1:1 ratio within a 200x200dp bounding box, compressed to JPEG at 80% quality, uploaded to Firebase Storage at `users/{userId}/profile.jpg`), and **location** (free-text field, e.g., "Austin, TX", displayed publicly on their profile). Changes must be saved to Firestore immediately with optimistic UI updates. The old profile photo must be deleted from Storage when replaced.

**View Achievements:**
- The profile screen displays the user's achievement badge grid, with earned badges shown in full color with their unlock date and unearned badges shown as locked silhouettes with their unlock condition. Tapping an earned badge shows a detail card with the achievement name, description, and date earned. Tapping an unearned badge shows the unlock condition and current progress (e.g., "30/50 cravings crushed"). The achievement grid must use a responsive layout (3 columns on phones, 4 on tablets).

**View Stats:**
- The stats section of the profile displays: days smoke-free, money saved, cigarettes avoided, total cravings logged, craving success rate, current level and XP, total XP earned, coins balance, and daily reward streak. Each stat must be displayed in a card with an icon, the value in large text, and a label below. Tapping a stat card may navigate to a detailed view (e.g., tapping "Cravings" navigates to the craving statistics screen).

**Logout:**
- Tapping "Logout" in the profile settings must: sign the user out of Firebase Auth, clear all local data caches (Firestore persistence, SharedPrefs), navigate to the login/welcome screen, and cancel all push notification subscriptions. A confirmation dialog must be shown: "Are you sure you want to log out? Your data is saved in the cloud and will be available when you log back in."

**Delete Account:**
- Tapping "Delete Account" must initiate a destructive, irreversible flow. A two-step confirmation is required: first, a dialog explaining what will be deleted ("Your profile, stories, chat messages, achievements, and all quit data will be permanently deleted. This cannot be undone."), then a text input requiring the user to type "DELETE" to confirm. Upon confirmation, a Firebase Cloud Function executes to: delete the user's Firestore document and all subcollections (cravings, achievements, chat messages, friend relationships), delete the user's Storage files (profile photo, event check-in videos), remove the user from all leaderboards and event rankings, delete the Firebase Auth account, and revoke all FCM tokens. The deletion must complete within 60 seconds, and the user must be signed out and returned to the welcome screen immediately after confirmation.

---

### 3.11 Onboarding

The onboarding flow is the user's first experience with Breathy, and it must be fast, welcoming, and information-gathering without feeling like a chore. The flow consists of four carefully sequenced steps that establish the user's identity, motivation, quit parameters, and smoking habits — all within a visually appealing, swipeable interface with progress indicators. The entire onboarding must be completable in under 3 minutes, and every input must be pre-filled with sensible defaults to minimize typing.

**Step 1: Welcome + Why Quitting**
- This is Breathy's first impression — a warm, visually stunning welcome screen with the Breathy logo, an inspiring tagline ("Your journey to freedom starts now"), and a brief value proposition. Below the welcome content, the user is asked: "What's your main reason for quitting?" with a selectable list of options: Health, Money, Family, Fitness, Freedom, and Other (with free-text input). This data is stored for personalization — the AI Coach and notifications can reference the user's primary motivation. The screen includes a "Next" button and a progress indicator showing "Step 1 of 4."

**Step 2: Set Quit Date and Quit Type**
- The user chooses between two quit modes via two large, visually distinct cards: **"Quit Now"** (instant quit, with a flame icon and description: "Start your smoke-free life immediately") and **"Gradual Quit"** (with a stepping-stones icon and description: "Reduce slowly over time"). Selecting "Quit Now" sets the quit date to the current date/time. Selecting "Gradual Quit" reveals a date picker for the target quit date (must be at least 7 days and at most 180 days in the future). The quit type and date are saved to Firestore. A brief explanation of each mode is shown below the cards. Progress indicator: "Step 2 of 4."

**Step 3: Enter Smoking Habits**
- Three input fields are presented with clear labels and pre-filled defaults: **Cigarettes per day** (number input, default 20, range 1–100), **Price per pack** (decimal input, currency-formatted, default $8.00), and **Cigarettes per pack** (number input, default 20, range 1–50). Each field includes a helper text explaining its purpose (e.g., "How much does one pack cost where you live?"). Real-time calculation preview is shown below the inputs: "Based on your habits, you'll save approximately $X per year." Input validation must prevent zero or negative values and show inline errors. Progress indicator: "Step 3 of 4."

**Step 4: Complete + Encouragement**
- A celebratory completion screen with confetti animation, the Breathy logo, and a personalized encouragement message incorporating the user's data: "You're all set, [Nickname]! Based on your habits, you'll save $X per year and avoid Y cigarettes. Your body starts healing in just 20 minutes. Let's do this!" Below the message, the user sees their first achievement unlocked ("First Breath" if instant quit, or "Journey Begun" if gradual) with a badge animation. A large "Start My Journey" button navigates to the home screen. Progress indicator: "Step 4 of 4" (completed state).

---

### 3.12 Notifications

The notification system is Breathy's ambient engagement layer — the persistent, gentle presence that keeps users connected to their quit journey even when they're not actively using the app. Notifications must be timely, relevant, and respectful of the user's attention, avoiding the spammy patterns that lead to notification fatigue and uninstalls. Every notification must provide genuine value and must be configurable by the user.

**Daily Motivation:**
- A once-daily notification delivered at the user's preferred time (configurable in settings, default 8:00 AM local time). The notification content is drawn from a curated pool of motivational messages, personalized with the user's name and quit stats. Examples: "Good morning, Dan! You've been smoke-free for 14 days and saved $84. Your circulation is improving — keep going!" and "Sarah, you've crushed 23 cravings this week. You're stronger than any craving!" The message pool must contain at least 100 unique templates and must never repeat the same message within a 30-day window. Tapping the notification opens the home screen.

**Milestone Reminders:**
- When the user is approaching a health milestone (e.g., "You're 6 hours away from CO levels dropping!"), a notification is triggered to build anticipation. When a milestone is achieved, a celebratory notification is sent: "Congratulations! Your heart attack risk has decreased — 24 hours smoke-free!" These notifications are critical for maintaining the "body is healing" narrative that reinforces quit motivation. Tapping the notification opens the health timeline scrolled to the relevant milestone.

**Friend Requests:**
- When another user sends a friend request, the recipient receives a push notification: "[Name] wants to be your friend on Breathy!" Tapping the notification opens the friends screen with the pending request highlighted. If the user has disabled friend request notifications, no notification is sent (the request still appears in the friends screen).

**Chat Messages:**
- When a friend sends a chat message, the recipient receives a push notification showing the sender's name and message preview (first 100 characters). Tapping the notification opens the one-on-one chat screen with that friend. If the recipient is currently in the chat screen with the sender, the notification is suppressed (the message appears in real-time via Firestore listener). If the user has disabled chat notifications, no notification is sent. Notifications must support grouping (multiple messages from the same sender are grouped into a single notification with a count).

**Event Reminders:**
- For each joined event, the user receives a daily reminder notification at a configurable time (default 6:00 PM) if they haven't checked in yet: "Don't forget to check in for [Event Name]! You're on a 5-day streak." After the user checks in, the reminder is suppressed for that day. When an event is about to end (1 day remaining), a notification is sent: "Last day of [Event Name]! Make sure to check in to earn your prize." When event results are announced, participants receive: "[Event Name] is complete! You ranked #X. Check your prizes!"

**Notification Preferences:**
- Users can configure notification preferences in the profile settings screen, with individual toggles for each notification type: Daily Motivation (on/off + time picker), Milestone Reminders (on/off), Friend Requests (on/off), Chat Messages (on/off), and Event Reminders (on/off + time picker). A "Disable All" master toggle is also available. All preferences are stored in the user's Firestore document and synced to the FCM topic subscriptions accordingly.

---

## 4. Non-Functional Requirements

### 4.1 Offline-First Architecture

Breathy must function as a fully offline-first application, ensuring that users can access their core quit data, log cravings, and view their health timeline regardless of network connectivity. Firestore's built-in persistence mechanism must be enabled (`setPersistenceEnabled(true)`) so that all previously fetched documents are cached locally on the device. When the user performs a write operation while offline (e.g., logging a craving or updating their profile), Firestore's offline queue automatically buffers the write and syncs it to the server when connectivity is restored. The app must handle offline states gracefully by displaying a subtle connectivity indicator (e.g., a small cloud-off icon in the top bar) and disabling features that inherently require network access (AI Coach chat, community feed, real-time chat, leaderboard) with clear "You're offline" messages. Local reads from the cache must return within 100ms. The local cache size must be configured to at least 50MB to accommodate several weeks of user data. Conflict resolution must follow Firestore's default last-write-wins strategy, and the app must implement merge logic in Cloud Functions to prevent data loss in rare concurrent-write scenarios.

### 4.2 Real-Time Updates

All features that involve collaborative or dynamic data must leverage Firestore's real-time listeners (`addSnapshotListener`) to provide instant updates without manual refresh. This includes: chat messages (new messages appear immediately), community feed (new stories and likes update in real-time), leaderboard rankings (position changes reflected instantly), friend request status (accept/reject updates the UI immediately), and event rankings (check-in approvals shift the ranking live). Real-time listeners must be managed carefully to avoid memory leaks and excessive billing: listeners must be detached when the corresponding screen is destroyed (using `LifecycleOwner` lifecycle awareness), and listeners must use `limit()` queries to avoid downloading entire collections. For high-velocity data (chat messages), the app must implement local debouncing to prevent UI flicker from rapid snapshot updates. The AI Coach must use a streaming-like approach where the response is displayed token-by-token as it's generated, creating a natural typing effect.

### 4.3 Performance

Performance is a critical non-functional requirement that directly impacts user experience and retention. The app must meet the following performance benchmarks: **App startup time** (cold start to interactive home screen) must be under 2 seconds on a mid-range device (Snapdragon 680, 6GB RAM). This requires minimizing work in `Application.onCreate()`, using lazy initialization for Firebase and AdMob SDKs, and leveraging Jetpack Compose's lazy composition for the home screen. **Screen transitions** must be under 300ms, achieved by using Compose's built-in transition animations and pre-loading data in ViewModels before navigation. **Firestore queries** must return cached results within 100ms and server results within 1 second for documents under 1KB. **Image loading** (profile photos, story images, event banners) must use a caching image loader (Coil) with a disk cache of at least 50MB and memory cache sized to 25% of available app memory. **Video upload** for event check-ins must support background upload with progress indication, compression to a maximum of 5MB per 15-second clip, and resume-on-reconnect for interrupted uploads. The app must maintain a consistent 60fps frame rate during animations (breathing exercise, confetti, level-up celebrations) and must drop no more than 5 frames per second during scroll-heavy screens (community feed, leaderboard).

### 4.4 Security

Security must be implemented at multiple layers to protect user data, prevent abuse, and maintain the integrity of the platform. **Firebase Authentication** must be the sole authentication mechanism, supporting Google Sign-In and email/password with email verification. Auth tokens must be validated on every Cloud Function invocation, and Firestore security rules must enforce that users can only read/write their own data (with exceptions for public data like stories, leaderboard entries, and public profiles). **Firestore Security Rules** must be written to prevent: unauthorized reads of private user data, writes to other users' documents, injection of malicious content in stories and chat messages (via length limits and content validation), and manipulation of XP, coins, or level values (these must be server-calculated only). **Input Validation** must be applied on both client and server: all text inputs must be sanitized for length, character set, and content; numeric inputs must be range-checked; and file uploads must be validated for type, size, and content (e.g., video files must be verified as valid MP4/MOV). **API Key Security** — the OpenAI API key must never be embedded in the client app; all AI Coach requests must route through a Firebase Cloud Function that holds the key in environment variables. **Rate Limiting** must be enforced server-side for all write operations: AI Coach messages (10/day for free users), story posts (5/day), check-in submissions (1/day per event), and friend requests (20/day) to prevent spam and abuse.

### 4.5 Accessibility

Breathy must be accessible to users with a wide range of abilities, following the Android Accessibility Guidelines and WCAG 2.1 Level AA standards. **Content Descriptions** must be provided for all interactive and informational UI elements — every icon, image, animation, and custom control must have a meaningful `contentDescription` that screen readers (TalkBack) can announce. For the breathing exercise animation, a live region must announce the current phase ("Breathe in... 2 seconds remaining..."). **High Contrast** — all text must meet a minimum contrast ratio of 4.5:1 against its background, and critical interactive elements must meet 3:1. The app must support Android's system-level dark mode and high-contrast text settings. **Large Touch Targets** — all interactive elements must have a minimum touch target size of 48x48dp, with recommended 56x56dp for primary actions. Spacing between adjacent touch targets must be at least 8dp. **Font Scaling** — the app must respect Android's font size settings and use scalable SP units for all text, with layout testing at 200% font scale to ensure no content is clipped or overlapping. **Screen Reader Navigation** — the app must use semantic Compose components (e.g., `Button`, `Heading`, `ListItem`) to provide logical navigation order, and must group related elements (e.g., a leaderboard entry) into single focus units with descriptive summaries.

### 4.6 Scalability

Breathy must be architected to handle significant user growth without degradation in performance or reliability. The target scale is **100,000 concurrent users** with the following design considerations: **Firestore** must be structured with denormalized reads in mind — frequently accessed data (user stats for leaderboard, profile summaries for community) must be pre-computed and stored in top-level fields rather than requiring joins or subcollection queries. Hot documents (e.g., global counters) must be sharded using Firebase's distributed counter pattern. **Cloud Functions** must be designed for idempotency and must use minimum instance counts to reduce cold-start latency for critical paths (AI Coach, push notifications). **Firebase Storage** must use a hierarchical path structure (`users/{userId}/...`, `events/{eventId}/...`) that allows parallel upload/download across storage buckets if needed. **OpenAI API** calls must be queued and throttled to stay within rate limits (using exponential backoff and a request queue in Cloud Functions). **AdMob** must be configured with mediation to maximize fill rates at scale. **Cost monitoring** must be implemented with Firebase budget alerts to detect unexpected spikes in Firestore reads/writes, Cloud Function invocations, or Storage bandwidth.

### 4.7 Reliability

Breathy must achieve **99.9% uptime** for all critical features, defined as: quit tracking, craving logging, health timeline, and local data access. Non-critical features (community feed, chat, AI Coach, leaderboard) must achieve 99.5% uptime. The app must implement the following reliability measures: **Graceful Degradation** — when a backend service is unavailable, the app must display clear, non-alarming error messages ("We're having trouble connecting. Your data is safe locally!") and must never crash or show a blank screen. **Automatic Retry** — all network operations must implement exponential backoff retry (initial delay 1s, max delay 30s, max 3 retries) with a Coroutine-based retry policy. **Health Checks** — a lightweight Cloud Function must serve as a health endpoint, monitored by an external uptime service (e.g., Firebase Performance Monitoring with alerts). **Data Integrity** — Firestore security rules and Cloud Functions must enforce that XP, coins, and levels can only be modified by server-side code, preventing client-side tampering. **Crash Reporting** — Firebase Crashlytics must be integrated with custom keys for user state (quit date, level, current screen) to enable rapid diagnosis of production issues. **Backup** — Firestore data must be exported to Cloud Storage on a weekly schedule using Firebase's scheduled exports, with a 30-day retention policy.

### 4.8 Data Privacy

Breathy must comply with GDPR (EU General Data Protection Regulation) and CCPA (California Consumer Privacy Act), ensuring that user data is collected minimally, stored securely, and fully controllable by the user. **Data Minimization** — the app must collect only the data necessary for core functionality: authentication credentials, smoking habits (cigarettes/day, price/pack, cigarettes/pack), quit date, craving logs, community content (stories, replies), and social connections. No location tracking, no contact list access, no advertising identifiers beyond AdMob's requirements. **User Consent** — on first launch, the app must present a clear, plain-language privacy notice and obtain explicit consent for data collection. Consent must be granular (users can opt out of non-essential data collection like analytics). **Data Access and Portability** — users must be able to request a full export of their data (via the "Delete Account" screen, with an "Export My Data" option) in JSON format, delivered via email within 30 days. **Right to Deletion** — the "Delete Account" feature must fully erase all user data as described in Section 3.10. **Data Encryption** — all data in transit must use TLS 1.2 or higher; all data at rest in Firestore and Firebase Storage is encrypted by default (AES-256). **Third-Party Sharing** — user data must not be shared with any third party beyond Firebase (infrastructure), OpenAI (AI Coach conversations, with no data retention on OpenAI's side as per their API policy), and AdMob (anonymized advertising identifiers). **Privacy Policy** — a comprehensive privacy policy must be accessible from the app's settings screen and the Play Store listing, written in clear, non-legalese language.

---

## 5. Success Metrics

### 5.1 Engagement Metrics

| Metric | Target | Measurement Method |
|---|---|---|
| **DAU/MAU Ratio** | > 30% | Daily Active Users / Monthly Active Users, computed from Firebase Analytics `user_engagement` events. A 30%+ ratio indicates strong habitual engagement — users are returning regularly, not just trying the app once. |
| **7-Day Retention** | > 40% | Percentage of new users who return on Day 7 after install. Measured via Firebase Analytics cohort retention. This is the critical "first week" window where most quit attempts either solidify or fail, making 40%+ retention a strong signal that the app is providing genuine value during the hardest period. |
| **30-Day Retention** | > 20% | Percentage of new users who return on Day 30 after install. A 20%+ rate for a health behavior app is well above industry average (~10-15%) and indicates that Breathy's gamification, community, and AI coaching features are sustaining engagement beyond the initial novelty phase. |
| **Average Session Duration** | > 3 minutes | Mean time-in-app per session. Measured via Firebase Analytics. Sessions under 30 seconds are excluded as accidental opens. Longer sessions indicate meaningful engagement with features like the community feed, AI Coach, and craving tools rather than passive checking. |
| **Sessions Per User Per Day** | > 2 | Average number of daily sessions per active user. Multiple daily sessions suggest users are integrating Breathy into their routine — checking in on cravings, reading community stories, and claiming daily rewards. |

### 5.2 Quit Success Metrics

| Metric | Target | Measurement Method |
|---|---|---|
| **Craving Success Rate** | > 60% | Percentage of logged cravings where the outcome is "success." This is the most direct measure of Breathy's craving management effectiveness. A 60%+ rate indicates that the breathing exercise, mini-game, and AI Coach are providing genuinely useful coping support, not just busywork. |
| **7-Day Smoke-Free Rate** | > 50% | Percentage of users who set a quit date and remain active (no quit date reset) after 7 days. This benchmark is significantly higher than the unassisted quit rate (~10-15%) and demonstrates the app's structural value in the critical first week. |
| **30-Day Smoke-Free Rate** | > 25% | Percentage of users who remain smoke-free after 30 days. This is an ambitious but achievable target given the combined support of tracking, community, AI coaching, and gamification. Industry benchmarks for app-assisted quitting range from 15-25%. |
| **Craving Tool Usage Rate** | > 70% | Percentage of craving episodes where the user engages with at least one coping tool (breathing, mini-game, or AI Coach) rather than skipping. High tool usage indicates that the craving bottom sheet is accessible, relevant, and effective enough that users choose to engage with it rather than dismiss it. |

### 5.3 Community and Social Metrics

| Metric | Target | Measurement Method |
|---|---|---|
| **Stories Posted Per User Per Week** | > 0.5 | Average number of community stories created per active user per week. While not every user will post weekly, a mean of 0.5+ indicates a healthy content ecosystem where a critical mass of users are sharing their journeys and providing the social proof that drives engagement for readers. |
| **Stories Read Per User Per Day** | > 3 | Average number of community stories viewed per active user per day. High read rates demonstrate that the community feed is compelling, relevant, and serving its purpose as a source of inspiration and connection. |
| **Friend Connections Per User** | > 2 | Average number of confirmed friends per active user. Two or more friends creates a meaningful social graph that enables chat support and Friends leaderboard engagement, moving users from passive consumption to active social participation. |
| **Chat Messages Per User Per Day** | > 1 | Average daily chat messages sent per active user. Even a single message per day indicates that users are leveraging the social support layer for real-time encouragement, which is a key differentiator from solitary tracking apps. |

### 5.4 AI Coach Metrics

| Metric | Target | Measurement Method |
|---|---|---|
| **AI Coach Satisfaction Rating** | > 4.0 / 5.0 | After every 5th AI Coach conversation, the user is prompted with a simple 1-5 star rating. The average rating must exceed 4.0, indicating that the AI Coach is perceived as helpful, empathetic, and genuinely valuable rather than annoying or generic. |
| **AI Coach Daily Active Usage** | > 20% of DAU | Percentage of daily active users who send at least one message to the AI Coach. A 20%+ adoption rate indicates that the AI Coach is a valued, regularly-used feature rather than a novelty that users try once and ignore. |
| **Craving-to-Coach Conversion** | > 15% | Percentage of craving episodes where the user selects the AI Coach as their coping method. This measures the AI Coach's effectiveness as a real-time craving intervention tool, as opposed to a general wellness chat. |

### 5.5 Monetization Metrics

| Metric | Target | Measurement Method |
|---|---|---|
| **"Support me" Conversion Rate** | > 5% | Percentage of total users who complete the $1 one-time purchase. A 5%+ rate on a $1 product is achievable given the emotional value of the app (users who've quit smoking with Breathy's help are likely to reciprocate) and the ad-removal benefit. Measured via Google Play Console revenue reports. |
| **Ad Revenue Per DAU** | > $0.02 | Average daily ad revenue per daily active user from AdMob open-app and interstitial ads. While modest on a per-user basis, this compounds at scale and provides baseline revenue sustainability. |
| **LTV (Lifetime Value)** | > $0.50 | Average total revenue per user over their app lifetime (ad revenue + subscription revenue). This must exceed the estimated CAC (Customer Acquisition Cost) of $0.20-0.30 to ensure sustainable growth. |

### 5.6 Technical Performance Metrics

| Metric | Target | Measurement Method |
|---|---|---|
| **App Cold Start Time** | < 2 seconds | Time from app icon tap to interactive home screen, measured via Firebase Performance Monitoring `app_start` trace. P95 (95th percentile) must be under 3 seconds. |
| **Crash-Free Rate** | > 99.5% | Percentage of daily sessions that do not end in a crash, measured via Firebase Crashlytics. This is the industry standard for production-quality Android apps. |
| **ANR Rate** | < 0.5% | Percentage of daily sessions that trigger an Application Not Responding error. ANRs are catastrophic for user experience and must be minimized through strict main-thread discipline (no network or disk I/O on main thread). |

---

## Appendix A: Technology Stack Summary

| Layer | Technology | Version / Details |
|---|---|---|
| Language | Kotlin | 1.9.x |
| UI Framework | Jetpack Compose | Latest stable (BOM) |
| Design System | Material 3 | Dynamic theming supported |
| Min SDK | 24 | Android 7.0 (covers 98%+ of devices) |
| Target SDK | 34 | Android 14 |
| Backend | Firebase | Auth, Firestore, Storage, Functions, FCM, Crashlytics, Analytics, Performance |
| AI | OpenAI API | GPT-4o-mini (via Cloud Functions) |
| Ads | Google AdMob | Open-app + Interstitial |
| Billing | Google Play Billing | BillingClient v6+ |
| Image Loading | Coil | Compose-integrated |
| DI | Hilt | Dagger-based dependency injection |
| Navigation | Compose Navigation | Type-safe navigation with sealed classes |
| State Management | ViewModel + StateFlow | Unidirectional data flow |

---

## Appendix B: Firestore Data Model Overview

```
users/
  {userId}/
    profile: { nickname, photoUrl, location, quitDate, quitType, cigarettesPerDay, pricePerPack, cigarettesPerPack, isSupporter, level, xp, coins, dailyStreak, fcmToken, createdAt }
    cravings/
      {cravingId}/: { timestamp, intensity, copingMethod, outcome }
    achievements/
      {achievementId}/: { name, unlockedAt }
    ai_coach_messages/
      {messageId}/: { role, content, timestamp }
    friend_requests/
      {requestId}/: { fromUserId, fromNickname, fromPhotoUrl, status, timestamp }
    friends/
      {friendId}/: { nickname, photoUrl, addedAt }

chats/
  {chatId (sorted UIDs)}/
    messages/
      {messageId}/: { senderId, content, timestamp }
    typing: { userId, timestamp }

stories/
  {storyId}/: { authorId, authorNickname, authorPhotoUrl, authorDaysFree, authorLevel, text, photoUrl, likeCount, replyCount, createdAt }
  replies/
    {replyId}/: { authorId, authorNickname, authorPhotoUrl, text, createdAt }

events/
  {eventId}/: { title, description, startDate, endDate, dailyRequirement, prize, bannerUrl, status, participantCount }
  participants/
    {userId}/: { nickname, photoUrl, joinedAt }
  checkins/
    {checkinId}/: { userId, date, videoUrl, status, reviewedAt, reviewedBy }

leaderboard/
  global/ { computed by scheduled function }
```

---

## Appendix C: Revision History

| Version | Date | Author | Changes |
|---|---|---|---|
| 1.0.0 | 2025-03-04 | AppForge | Initial PRD release |

---

*End of Document*
