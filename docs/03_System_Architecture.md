# Breathy — System Architecture Document

> **Version:** 1.0  
> **Last Updated:** 2026-03-04  
> **Audience:** Engineering team, technical stakeholders  
> **Scope:** Full-stack Android application architecture covering client, backend services, and external integrations

---

## Table of Contents

1. [High-Level Architecture Diagram](#1-high-level-architecture-diagram)
2. [Component Architecture](#2-component-architecture)
3. [Data Flow for Critical Paths](#3-data-flow-for-critical-paths)
4. [Offline-First Strategy](#4-offline-first-strategy)
5. [Authentication Flow](#5-authentication-flow)
6. [Push Notification Architecture](#6-push-notification-architecture)
7. [Security Architecture](#7-security-architecture)
8. [Performance Optimization](#8-performance-optimization)
9. [Error Handling Strategy](#9-error-handling-strategy)

---

## 1. High-Level Architecture Diagram

Breathy follows a **clean layered architecture** pattern that enforces strict separation of concerns, unidirectional data flow, and testability across every module. The architecture is divided into four principal layers: Presentation, Domain, Data, and External Services. Each layer communicates only with its immediate neighbors, ensuring that business logic remains decoupled from UI concerns and that data source implementation details never leak upstream into the domain or presentation tiers.

```
┌─────────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                            │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────────────────┐  │
│  │  Jetpack     │  │  ViewModels  │  │  Navigation Component     │  │
│  │  Compose UI  │──▶ (StateFlow)  │──▶  (Compose Navigation)     │  │
│  └──────┬──────┘  └──────┬───────┘  └───────────────────────────┘  │
│         │                │                                          │
└─────────┼────────────────┼──────────────────────────────────────────┘
          │ observes        │ invokes
          ▼                 ▼
┌─────────────────────────────────────────────────────────────────────┐
│                        DOMAIN LAYER                                │
│  ┌─────────────────┐  ┌────────────────┐  ┌─────────────────────┐  │
│  │  Repository      │  │  Use Cases     │  │  Business Logic     │  │
│  │  Interfaces      │──▶  (Optional)    │──▶  (XP calc, streaks) │  │
│  └──────┬──────────┘  └────────────────┘  └─────────────────────┘  │
│         │                                                          │
└─────────┼──────────────────────────────────────────────────────────┘
          │ implemented by
          ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         DATA LAYER                                 │
│  ┌──────────────────┐  ┌───────────────────┐  ┌────────────────┐   │
│  │  Repository       │  │  Firebase          │  │  Local Caching │   │
│  │  Implementations  │──▶  Firestore/Auth/   │──▶  (Firestore   │   │
│  │                   │  │  Storage            │  │   persistence) │   │
│  └──────┬───────────┘  └───────────────────┘  └────────────────┘   │
│         │                                                          │
└─────────┼──────────────────────────────────────────────────────────┘
          │ calls
          ▼
┌─────────────────────────────────────────────────────────────────────┐
│                     EXTERNAL SERVICES                              │
│  ┌──────────┐ ┌────────┐ ┌──────────┐ ┌────────┐ ┌─────────────┐  │
│  │ Firebase │ │ OpenAI │ │ Google   │ │ AdMob  │ │ Cloud       │  │
│  │ Suite    │ │ API    │ │ Play    │ │        │ │ Functions   │  │
│  │          │ │        │ │ Billing  │ │        │ │             │  │
│  └──────────┘ └────────┘ └──────────┘ └────────┘ └─────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

### Data Flow Between Layers — Concrete Example

Consider the flow when a user views their smoke-free dashboard. The **Presentation Layer** renders the Home screen via a Jetpack Compose composable that observes a `StateFlow<HomeUiState>` emitted by `HomeViewModel`. The ViewModel calls into the **Domain Layer** by invoking `UserRepository.getSmokeFreeStats(userId)`, where `UserRepository` is an interface defining the contract. The **Data Layer** implements this interface via `UserRepositoryImpl`, which queries Firebase Firestore for the user's quit date and cached statistics, merges them with locally persisted data from Firestore's offline cache, and returns a domain model object. If the network is unavailable, Firestore's built-in persistence layer transparently serves the last-known-good data, ensuring the UI always renders something meaningful.

For write operations, the flow reverses: when a user logs a craving, the ViewModel calls `CravingRepository.logCraving(cravingEntry)`. The repository implementation writes to Firestore, which queues the write locally if offline and syncs automatically when connectivity resumes. The ViewModel then updates the UI state to reflect the new entry, potentially triggering an achievement check that flows through `AchievementRepository.checkAndUnlockAchievements(userId)`, which may cascade into an XP update and a confetti celebration in the Presentation Layer.

This unidirectional, layered approach yields several critical benefits: the Presentation Layer is completely agnostic about where data originates, making it trivial to swap Firestore for a different backend; the Domain Layer contains pure business logic that can be unit-tested without any Android framework dependencies; and the Data Layer encapsulates all the messy details of network communication, serialization, and caching behind clean repository interfaces.

---

## 2. Component Architecture

### Navigation

Breathy employs a **Single Activity** architecture where `MainActivity` hosts the entire Compose UI tree. Navigation is managed by Jetpack Compose Navigation with a bottom navigation bar exposing five primary tabs: **Home** (smoke-free tracker dashboard), **Community** (social stories feed), **Chat** (peer messaging), **Events** (check-in challenges), and **Profile** (settings and stats). Each tab maps to a top-level navigation destination, and within each tab, a nested navigation graph handles drill-down screens. For example, the Community tab contains routes for the story feed, story detail, compose story, and user profile viewing. Deep links from push notifications are resolved by the navigation framework and route users to the correct nested destination regardless of which tab is currently active. The navigation state is preserved across configuration changes via `rememberSaveable` and ViewModel persistence, ensuring users never lose their place after screen rotations or process recreation.

### State Management

Every screen in Breathy is backed by a dedicated `ViewModel` that exposes its UI state through a `StateFlow<UiState>` or `MutableStateFlow<UiState>`. The ViewModel collects data from repositories using Kotlin coroutines launched in `viewModelScope`, transforms raw domain models into presentation-ready UI state objects, and emits them to the Compose UI for rendering. User actions—such as button clicks, scroll events, or form submissions—are dispatched to the ViewModel via well-defined intent functions (e.g., `onCravingLogged()`, `onStoryLiked()`), which process the intent, invoke the appropriate repository methods, and update the state flow. This unidirectional data flow pattern (intent → ViewModel → state → UI) eliminates race conditions, makes state transitions predictable and debuggable, and ensures that the Compose UI is always a pure function of the current state. Side effects like navigation events, snackbar displays, and confetti triggers are communicated through a shared `SingleEventFlow` or `Channel` to guarantee they fire exactly once.

### Dependency Injection

Breathy uses **manual dependency injection** via an `AppModule` singleton object initialized in `BreathyApplication`. The AppModule is responsible for constructing and providing all repository implementations, Firebase service instances, and utility managers as singletons or scoped instances. For example, `AppModule.provideUserRepository()` lazily creates a `UserRepositoryImpl` with the Firestore instance and caches it for the app's lifetime. ViewModels receive their dependencies through constructor parameters passed from the Compose UI using `viewModel { ... }` factory lambdas. While this approach lacks the compile-time verification of Dagger or Hilt, it dramatically reduces build complexity, annotation processing overhead, and the learning curve for contributors—trade-offs that are well-suited for a small team shipping a consumer-facing app at high velocity. The manual DI container is also trivially testable: in unit tests, the AppModule can be replaced with a test module that provides mock or in-memory repository implementations.

### Image Loading

All image loading throughout Breathy is handled by **Coil 3.0** with its first-class Compose integration via `AsyncImage` and `SubcomposeAsyncImage` composables. Coil is configured with a singleton `ImageLoader` provided by AppModule, which includes a shared memory cache (sized at 25% of available app memory), a disk cache limited to 50 MB, and custom OkHttp client with configured timeouts and interceptors for authentication headers when loading images from Firebase Storage. Coil's Compose integration automatically cancels image requests when composables leave the composition, preventing memory leaks. Placeholder and error states are handled declaratively within the Compose tree using Coil's built-in state objects, and crossfade transitions provide a polished loading experience. For user-uploaded story photos, Coil fetches Firebase Storage download URLs and caches them aggressively, while profile avatars use a more conservative cache policy to reflect updates promptly.

### Camera

Video recording for event check-ins is powered by **CameraX**, which abstracts away the considerable complexity of the Android camera2 API while maintaining backward compatibility to API 21. Breathy uses `ProcessCameraProvider` to bind a `VideoCapture` use case to the device's back camera, with `Recorder` and `PendingRecording` configurations set to produce MP4 output at 720p resolution with a 10-second maximum duration. The recording lifecycle is managed by the `CheckInViewModel`, which starts and stops recording in response to UI events and propagates the resulting video file URI to the upload pipeline. CameraX handles device-specific quirks—such as varying sensor orientations, flash capabilities, and lens configurations—transparently, ensuring that video recording works consistently across the wide range of Android devices in Breathy's target market. Permission handling follows the Activity Result API pattern, with a rationale dialog shown before requesting camera and audio permissions.

### Payments

Breathy's premium subscription is managed through the **Google Play Billing Library 6.0**, which provides a robust, Google-managed purchasing flow. The `BillingManager` singleton initializes a `BillingClient` in the `onCreate` callback of `BreathyApplication`, queries available products (monthly and yearly subscription SKUs), and exposes purchase state through a `StateFlow<PurchaseState>`. When a user initiates a purchase, the BillingManager launches the billing flow, handles the purchase result in `onPurchasesUpdated()`, acknowledges the purchase on Google's servers, and then verifies the subscription status by writing a `premiumUntil` timestamp to the user's Firestore document. The server-side Cloud Function `validatePurchase` acts as a secondary verification layer, periodically cross-referencing Google Play's purchase API with Firestore records to detect and remediate any discrepancies. Grace periods, billing retries, and subscription pauses are all handled by Google's infrastructure and reflected in the real-time purchase state observed by the UI.

### Ads

Ad monetization is implemented through **AdMob** with two ad formats: **App Open Ads** and **Interstitial Ads**. The `AppOpenAdManager` pre-loads an app open ad during the splash screen and displays it before the user reaches the Home screen, ensuring a non-disruptive placement at a natural transition point. The `InterstitialAdManager` manages interstitial ads shown between specific high-engagement transitions, such as after completing a craving coping activity or after posting a story. Both managers implement the `FullScreenContentCallback` to handle ad lifecycle events (show, dismiss, failure) and update a shared ad availability state that the UI observes. Premium subscribers are exempt from all ad displays—the ViewModel checks `PurchaseState` before triggering any ad load request. Ad impressions are tracked via Firebase Analytics events (`ad_impression`, `ad_click`, `ad_error`), providing the team with real-time monetization metrics. Care is taken to ensure ads never block critical user flows: if an ad fails to load within 3 seconds, the app proceeds without displaying it.

### Notifications

Firebase Cloud Messaging drives Breathy's push notification system, with **five distinct notification channels** providing users fine-grained control over alert preferences. The channels are: **Motivation** (daily motivational quotes and streak reminders), **Milestones** (achievement unlocks and smoke-free anniversaries), **Social** (friend requests, story likes, comments), **Chat** (new direct messages), and **Events** (challenge start reminders, check-in deadlines, admin approvals). Each channel is created at app startup with appropriate importance levels—Chat and Events use `IMPORTANCE_HIGH` with sound and vibration, while Motivation uses `IMPORTANCE_DEFAULT` for passive awareness. The `BreathyMessagingService` extends `FirebaseMessagingService` to handle incoming messages in both foreground and background states, routing them through a `NotificationRouter` that determines the correct channel and deep-link destination. FCM tokens are registered on sign-in and deleted on sign-out to prevent stale notification delivery.

### Confetti

Celebration effects for achievements, streak milestones, and community interactions are powered by the **Konfetti** library, which renders GPU-accelerated particle systems on top of the Compose UI layer. Breathy uses Konfetti's `KonfettiView` composable, which is conditionally rendered based on a `showConfetti` state flag in the screen's ViewModel. When an achievement is unlocked, the ViewModel sets `showConfetti = true` along with a configurable `ConfettiConfig` object that specifies the color palette (brand colors + gold), particle shape (circles and stars), emission rate, duration, and gravity. The confetti effect auto-dismisses after 3 seconds or on user tap, and the state flag is reset to prevent re-triggering on recomposition. Konfetti's efficient particle recycling ensures that celebration effects maintain a smooth 60 FPS even on mid-range devices, and the lightweight library adds only ~50 KB to the APK size.

---

## 3. Data Flow for Critical Paths

### Path 1: User Opens App (App Open Ad Flow)

```
┌──────────┐    ┌──────────────┐    ┌─────────────┐    ┌──────────┐
│  Launch   │───▶│  App Open    │───▶│  Show Ad    │───▶│  Home    │
│  Intent   │    │  Ad Manager  │    │  (3s max)   │    │  Screen  │
└──────────┘    └──────────────┘    └─────────────┘    └────┬─────┘
                                                              │
                    ┌──────────────┐    ┌──────────────┐      │
                    │  Display     │◀───│  Merge with  │◀─────┘
                    │  UiState     │    │  Local Cache │
                    └──────────────┘    └──────────────┘
                           ▲
                    ┌──────────────┐
                    │  Firestore   │
                    │  Query       │
                    └──────────────┘
```

When the user taps the Breathy launcher icon, the system delivers a launch intent to `MainActivity`, which begins its `onCreate` lifecycle. The Activity immediately delegates to the Compose `setContent` block, where the `BreathyApp` composable checks the current authentication state via `FirebaseAuth.currentUser`. If authenticated, the app invokes `AppOpenAdManager.showAdIfAvailable()`, which checks whether a pre-loaded app open ad is ready in the cache. If an ad is available and the user is not a premium subscriber, the ad is displayed as a full-screen overlay with a maximum display timeout of 5 seconds. If the ad fails to load or the 3-second preload window expires, the app gracefully skips the ad and proceeds directly to the Home screen. Once the ad is dismissed (or skipped), the navigation framework routes to the Home destination, where `HomeViewModel.init` fires a coroutine that calls `UserRepository.getSmokeFreeStats()` and `StoryRepository.getRecentStories()`. These repository calls query Firestore with the user's ID, and Firestore's offline persistence layer ensures that cached data is returned immediately while the network fetch proceeds in the background. The ViewModel merges the results into a `HomeUiState.Success` object and emits it via `StateFlow`, causing the Compose UI to render the smoke-free counter, streak badges, and recent community stories. A "last updated" timestamp appears at the bottom of the screen when data was served from cache rather than a fresh network response.

### Path 2: Craving Management Flow

```
┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Craving  │───▶│  Bottom      │───▶│  Select      │───▶│  Activity    │
│  Button   │    │  Sheet       │    │  Coping      │    │  (Breathe/   │
│  Tap      │    │  Opens       │    │  Method      │    │   Game/Coach)│
└──────────┘    └──────────────┘    └──────────────┘    └──────┬───────┘
                                                              │
         ┌──────────────┐    ┌──────────────┐    ┌───────────┘
         │  Confetti +  │◀───│  Check        │◀───│  Log Result│
         │  Achievement │    │  Achievements │    │  + XP      │
         └──────────────┘    └──────────────┘    └────────────┘
```

The craving management flow is Breathy's core engagement loop, designed to transform a moment of weakness into a gamified, supportive experience. When the user taps the prominent craving button on the Home screen, the ViewModel sets `showCravingSheet = true`, triggering a `ModalBottomSheet` composable to slide up with three coping method options: **Breathing Exercise** (a guided 4-7-8 breathing animation), **Distraction Game** (a simple tapping or puzzle mini-game), and **AI Coach** (a ChatGPT-powered conversational support session). After selecting a method, the user completes the activity—for example, finishing a 2-minute breathing exercise. Upon completion, the ViewModel calls `CravingRepository.logCraving(CravingEntry(method, duration, timestamp))`, which writes a document to the `cravings` subcollection in Firestore. The repository then calls `AchievementRepository.checkAndUnlock(userId)`, which evaluates the user's craving history against predefined achievement rules (e.g., "Logged 5 cravings in a row without relapsing" or "Used breathing exercise 10 times"). If an achievement is unlocked, a new document is written to the `achievements` collection, the user's XP is incremented via `UserRepository.addXP(xpAmount)`, and the ViewModel receives a `CravingResult.AchievementUnlocked(achievement, newXP)` response. The Presentation Layer then triggers the Konfetti confetti effect and displays a celebration dialog. If no achievement is unlocked, a simpler encouraging message is shown. The entire flow completes in under 500 milliseconds for the local operations, with Firestore writes proceeding asynchronously and queuing gracefully if the device is offline.

### Path 3: Story Posting Flow

```
┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  FAB Tap  │───▶│  Post Screen │───▶│  Compose     │───▶│  Pick Photo  │
│           │    │  Opens       │    │  Text        │    │  (Gallery)   │
└──────────┘    └──────────────┘    └──────────────┘    └──────┬───────┘
                                                              │
         ┌──────────────┐    ┌──────────────┐    ┌───────────┘
         │  Success +   │◀───│  Create       │◀───│  Upload to  │
         │  Feed Update │    │  Firestore    │    │  Storage    │
         └──────────────┘    └──────────────┘    └────────────┘
                                    ▲
                            ┌──────────────┐
                            │  Get Download │
                            │  URL from     │
                            │  Storage      │
                            └──────────────┘
```

Story posting is the primary content creation flow in Breathy's social community feature. When the user taps the Floating Action Button on the Community screen, the navigation framework routes to the `ComposeStoryScreen`. The screen presents a text input field (maximum 500 characters) and an optional photo picker powered by Android's `ActivityResultContracts.PickVisualMedia`. Once the user has composed their story and optionally attached a photo, they tap the "Share" button, which triggers `StoryViewModel.postStory(text, photoUri)`. If a photo is attached, the ViewModel first calls `StorageRepository.uploadStoryPhoto(userId, photoUri)`, which compresses the image to a maximum of 1080px width using Android's `Bitmap` scaling utilities, converts it to WebP format at 80% quality, and uploads it to Firebase Storage at the path `stories/{userId}/{timestamp}.webp`. Upon successful upload, the Storage SDK returns a download URL, which is captured and passed to the next step. The ViewModel then calls `StoryRepository.createStory(Story(text, photoUrl, authorId, timestamp))`, which writes a new document to the `stories` collection in Firestore with the photo download URL embedded. Firestore's offline persistence ensures the write is queued locally if the device is offline, and the story appears in the user's own feed immediately via optimistic UI updates. The community feed's `StoryRepository.getRecentStories()` listener automatically picks up the new document via Firestore's real-time snapshot listeners, causing all active users' feeds to update within seconds. A success snackbar confirms the post, and the navigation stack pops back to the community feed.

### Path 4: Real-time Chat Flow

```
┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Open     │───▶│  Attach      │───▶│  Display     │───▶│  User Types  │
│  Chat     │    │  Firestore   │    │  Messages    │    │  Message     │
└──────────┘    └──────────────┘    └──────────────┘    └──────┬───────┘
                                                              │
         ┌──────────────┐    ┌──────────────┐    ┌───────────┘
         │  Recipient   │◀───│  Cloud        │◀───│  Write to   │
         │  Notification│    │  Function →   │    │  Firestore  │
         └──────────────┘    │  FCM          │    └────────────┘
                             └──────────────┘
```

Breathy's peer-to-peer chat system leverages Firestore's real-time capabilities as both the message transport and the persistence layer. When a user opens a chat conversation, the `ChatViewModel` attaches a `FirebaseFirestore.collection("chats/{chatId}/messages").orderBy("timestamp").addSnapshotListener()` query that streams message updates in real-time. Messages are rendered in a `LazyColumn` with the sender's messages right-aligned and the recipient's left-aligned, each displaying the text, timestamp, and read receipt status. When the user types and sends a message, the ViewModel calls `ChatRepository.sendMessage(chatId, Message(text, senderId, timestamp))`, which writes a new document to the `messages` subcollection and updates the parent `chat` document's `lastMessage` and `lastMessageTimestamp` fields for efficient listing in the chat inbox. This Firestore write triggers a **Cloud Function** named `onNewMessage`, which is configured with a Firestore trigger on `chats/{chatId}/messages/onCreate`. The Cloud Function retrieves the recipient's FCM token from their user document, constructs a notification payload with the sender's name and a truncated message preview, and sends it via the Firebase Admin Messaging SDK. If the recipient's app is in the foreground, `BreathyMessagingService` intercepts the message and displays an in-app notification banner; if the app is in the background, the system notification tray displays the message with a deep link that opens the specific chat conversation when tapped. Read receipts are updated when the recipient's `ChatViewModel` attaches its snapshot listener, triggering a batch write that marks all unread messages as delivered.

### Path 5: Event Check-in Flow

```
┌──────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Check-in │───▶│  CameraX     │───▶│  Record      │───▶│  Upload to   │
│  Button   │    │  Opens       │    │  Video ≤10s  │    │  Storage     │
└──────────┘    └──────────────┘    └──────────────┘    └──────┬───────┘
                                                              │
         ┌──────────────┐    ┌──────────────┐    ┌───────────┘
         │  Streak &    │◀───│  Admin        │◀───│  Create Check│
         │  Rank Update │    │  Review       │    │  In Document │
         └──────────────┘    └──────────────┘    └────────────┘
```

The event check-in flow combines hardware interaction (camera), file management (video upload), and human-in-the-loop review (admin approval) into a seamless user experience. When a user taps the check-in button on an event detail screen, the `CheckInViewModel` first verifies that the user has not already checked in for this event, then launches the CameraX video recording screen via navigation. The CameraX `VideoCapture` use case is configured with a 10-second maximum duration, 720p resolution, and audio recording enabled. A countdown timer overlay is displayed in the Compose UI to guide the user. When the user stops recording (or the timer expires), the resulting MP4 file URI is returned to the `CheckInViewModel`, which immediately begins the upload process by calling `StorageRepository.uploadCheckInVideo(eventId, userId, videoUri)`. The video is uploaded to Firebase Storage at `checkins/{eventId}/{userId}_{timestamp}.mp4` using the Storage SDK's `uploadFile` method with a progress listener that updates the UI with an upload percentage indicator. For large video files, the upload is resumable—if the network drops mid-upload, the SDK automatically retries from the last successful byte range. Once the upload completes and returns a download URL, the ViewModel calls `EventRepository.createCheckIn(eventId, userId, videoUrl)`, which writes a document to `events/{eventId}/checkins/{userId}` with `status: "pending"` and the video URL. An admin dashboard (built into the app with a hidden admin flag) displays pending check-ins for review. When an admin approves a check-in, a Cloud Function updates the status to `"approved"`, increments the user's streak count in their public profile, and updates their rank on the event leaderboard. The user receives a push notification via the Milestones channel confirming their check-in was approved, and their profile screen reflects the updated streak.

### Path 6: Daily Smoke-Free Update (Cloud Function)

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│  Scheduled   │───▶│  Query All   │───▶│  Calculate   │───▶│  Update      │
│  Function    │    │  Users       │    │  Days Smoke-  │    │  public      │
│  (Midnight)  │    │  (Batched)   │    │  Free         │    │  Profiles    │
└──────────────┘    └──────────────┘    └──────────────┘    └──────┬───────┘
                                                              │
                    ┌──────────────┐    ┌──────────────┐    ┌───────────┘
                    │  Send FCM    │◀───│  Check        │◀───│  Achievement│
                    │  Motivation  │    │  Milestones   │    │  Unlocks    │
                    └──────────────┘    └──────────────┘    └────────────┘
```

Every night at midnight in the user's local timezone (bucketed into 4 timezone groups for efficiency), a **Cloud Function** named `dailySmokeFreeUpdate` is triggered by a Pub/Sub scheduled topic. The function begins by querying all user documents from Firestore in batches of 500 (respecting Firestore's read limits), skipping users who have relapsed or deactivated their accounts. For each user, the function calculates the number of days since their quit date using `ChronoUnit.DAYS.between(quitDate, now)`, and writes the updated `daysSmokeFree` field to both the user's private document and their `publicProfiles` document (which powers the community leaderboard). The function then checks the updated day count against a predefined set of milestone thresholds: 1 day, 3 days, 1 week, 2 weeks, 1 month, 3 months, 6 months, 1 year, and each subsequent year. If a milestone is reached, an achievement document is created in the user's `achievements` subcollection, and XP is awarded according to the milestone's configured reward (e.g., 100 XP for 1 day, 500 XP for 1 month, 5000 XP for 1 year). Finally, the function sends a motivational push notification to each user via FCM, selecting from a rotating pool of encouragement messages tailored to their current smoke-free duration—for example, users in their first week receive messages emphasizing short-term health improvements, while users past 6 months receive messages celebrating long-term freedom. The entire batch process is instrumented with error handling, retry logic, and Firestore write batching to ensure atomicity and minimize billing costs. Execution typically completes in under 60 seconds for the full user base, and detailed logs are written to Cloud Logging for monitoring.

---

## 4. Offline-First Strategy

### Core Philosophy

Breathy is designed as an **offline-first** application, recognizing that users in craving situations may have poor or no network connectivity—exactly when the app is most needed. Every critical user journey, from logging cravings to viewing smoke-free stats, must function without an active network connection. The offline-first strategy is built on three pillars: proactive local caching, transparent write queuing, and graceful degradation of network-dependent features.

### Firestore Persistence

Firebase Firestore's built-in persistence is the backbone of Breathy's offline capability. At app initialization, `BreathyApplication` configures `FirebaseFirestoreSettings` with `setPersistenceEnabled(true)` and `setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)`. This instructs the Firestore SDK to maintain a complete local SQLite database that mirrors all documents the app has previously read. When the app queries Firestore, the SDK returns results from the local cache immediately and then fetches updates from the server in the background, merging them seamlessly. This means the Home screen's smoke-free stats, the community stories feed, and the user's profile data are all available instantly on launch, regardless of network state. The Firestore SDK also automatically queues all write operations when the device is offline—when a user logs a craving or sends a chat message while offline, the write is persisted locally and synced to the server as soon as connectivity is restored, with no additional code required.

### Local Caching Strategy

Beyond Firestore's automatic persistence, Breathy implements an additional layer of explicit local caching for frequently accessed data. The user's own profile, including their quit date, XP, achievements, and streak count, is cached in a `DataStore<Preferences>` instance that provides sub-millisecond read access. Recent stories and community feed items are cached in Firestore's persistence layer via long-lived snapshot listeners that keep the local cache warm. The app displays a subtle "last updated" timestamp at the bottom of each screen when data is served from cache, giving users transparency about data freshness without disrupting the experience. For write operations, the UI optimistically updates the local state before the server response arrives—for example, when a user logs a craving, the UI immediately shows the updated craving count and XP gain, while the Firestore write proceeds in the background. If the write fails (due to a conflict or validation error), the ViewModel rolls back the local state and displays an error message.

### Network Monitoring and Graceful Degradation

A `NetworkMonitor` utility class, powered by Android's `ConnectivityManager` and `NetworkCallback` API, continuously observes network state changes and exposes a `StateFlow<NetworkStatus>` (Connected, Offline, Losing) that ViewModels observe. When the network transitions to Offline, ViewModels proactively switch their repositories to cache-only mode, cancel any in-flight network requests, and update the UI to reflect the offline state. Features are categorized into **offline-capable** and **network-required** groups. Offline-capable features include: viewing smoke-free stats, logging cravings, completing breathing exercises and distraction games, reading cached community stories, and viewing cached chat history. Network-required features that gracefully degrade include: AI Coach conversations (which display a "Connect to the internet to chat with your coach" placeholder), video upload for event check-ins (which queue the upload for when connectivity resumes), and new story posting with photos (which queues both the Storage upload and Firestore write). The UI clearly communicates which features are unavailable offline using informational banners and disabled states, while ensuring that the core quit-smoking support features remain fully functional at all times.

### Retry Logic

For operations that fail due to network conditions, Breathy implements a multi-tier retry strategy. Firestore's built-in SDK handles automatic retries for read and write operations with exponential backoff (starting at 1 second, doubling up to 60 seconds, with jitter). For Firebase Storage uploads, the SDK supports resumable uploads that automatically resume from the last successfully uploaded byte. For AI Coach API calls and other REST-based operations, the app implements a custom `RetryInterceptor` in OkHttp that retries failed requests up to 3 times with exponential backoff. All retry operations are scoped to `viewModelScope` or `lifecycleScope` and are automatically cancelled when the user navigates away from the screen, preventing wasted battery and network resources. The `NetworkMonitor` also triggers immediate retry of queued operations when connectivity is restored, ensuring that offline writes are synced as quickly as possible.

---

## 5. Authentication Flow

### Sign-In Methods

Breathy supports two authentication methods: **Email/Password** and **Google Sign-In**, both implemented through Firebase Authentication. The Email/Password flow uses Firebase Auth's `createUserWithEmailAndPassword()` for registration and `signInWithEmailAndPassword()` for login, with client-side validation enforcing minimum password strength (8+ characters, mixed case, at least one number) and email format verification before any Firebase call is made. The Google Sign-In flow uses the `GoogleSignInClient` API to present the system account picker, retrieves a Google ID token via `ActivityResultContracts.StartActivityForResult()`, and exchanges it for Firebase credentials using `GoogleAuthProvider.getCredential()`. Both methods result in a `FirebaseUser` object that Breathy uses as the canonical identity for all subsequent operations. The sign-in screen also includes a "Forgot Password" flow that calls `FirebaseAuth.sendPasswordResetEmail()` and displays a confirmation dialog.

### Auth State Listener

Authentication state is managed centrally in `BreathyApplication`, where a `FirebaseAuth.AuthStateListener` is registered during `onCreate`. This listener fires whenever the user's sign-in state changes—on app launch, after sign-in, after sign-out, or when the Firebase SDK refreshes the user's token. The listener updates a `StateFlow<AuthState>` (Authenticated, Unauthenticated, Loading) that is observed by the root `BreathyApp` composable to route the user to the correct screen: Unauthenticated users see the onboarding/sign-in flow, while Authenticated users proceed to the main app. This centralized approach ensures that auth state is consistent across all ViewModels and eliminates the common bug of stale auth states in individual screens.

### Onboarding Routing

When a user signs in for the first time, the `AuthStateListener` checks whether a user document exists in Firestore at `users/{uid}`. If no document is found, the navigation framework routes the user to the **OnboardingFlow**, a multi-step wizard that collects the user's quit date, smoking history (cigarettes per day, years smoked), motivational reasons, and display name. The onboarding data is written to Firestore as a batch operation that atomically creates the user document, the public profile document, and the initial smoke-free stats document. Only after the onboarding flow completes does the user reach the Home screen. If a user document already exists (returning user), the app loads their data and proceeds directly to the Home screen. This check is performed on every sign-in, not just the first, to handle edge cases like account deletion and re-registration.

### Token Management

Firebase Authentication SDK handles all token refresh automatically, including the hourly ID token rotation and the seamless refresh of the Google OAuth access token. Breathy does not manually manage tokens—the SDK's internal `GetTokenResult` provider ensures that every authenticated Firestore and Storage request includes a valid, fresh token. If the token refresh fails (e.g., the user's Google account was deleted or their password was changed on another device), the `AuthStateListener` fires with a null user, and the app gracefully transitions to the Unauthenticated state, showing the sign-in screen with an informational message.

### Sign-Out Flow

Sign-out is a critical operation that must cleanly tear down all active resources to prevent data leaks and stale listeners. When the user taps "Sign Out" in the Profile screen, the `AuthViewModel` executes a comprehensive cleanup sequence: (1) all Firestore snapshot listeners are removed by calling `ListenerRegistration.remove()` on each registered listener, (2) the FCM token is deleted from the user's document via `FirebaseMessaging.getInstance().deleteToken()`, (3) all repository caches are cleared, (4) `FirebaseAuth.signOut()` is called, which triggers the `AuthStateListener` and transitions the app to the Unauthenticated state, and (5) the Google Sign-In client is revoked via `GoogleSignInClient.signOut()`. This ensures that after sign-out, no background processes continue to consume resources or receive data intended for the previous user.

---

## 6. Push Notification Architecture

### FCM Token Lifecycle

FCM token management follows a strict lifecycle tied to the user's authentication state. When a user signs in, the `BreathingMessagingService` calls `FirebaseMessaging.getInstance().token.addOnSuccessListener()` to retrieve the current FCM token and writes it to the user's Firestore document at `users/{uid}/fcmToken`. This token is refreshed periodically by the Firebase SDK, and the `onNewToken()` callback in `BreathingMessagingService` handles updates by writing the new token to Firestore. When a user signs out, the token is explicitly deleted from Firestore and the local FCM registration is revoked via `FirebaseMessaging.getInstance().deleteToken()`, ensuring that notifications are never delivered to a device after the user has signed out. For users signed in on multiple devices, each device maintains its own FCM token in a `tokens` subcollection, allowing Cloud Functions to send notifications to all active devices.

### Notification Channels

Breathy defines five notification channels, each with distinct importance levels, sound settings, and visual styles that reflect the urgency and nature of the content:

| Channel | Importance | Sound/Vibration | Use Cases |
|---------|-----------|-----------------|-----------|
| **Motivation** | DEFAULT | Gentle chime | Daily motivational quotes, streak encouragement |
| **Milestones** | HIGH | Custom celebratory tone | Achievement unlocks, smoke-free anniversaries |
| **Social** | DEFAULT | Soft ping | Friend requests, story likes, comments |
| **Chat** | HIGH | Message tone + vibration | New direct messages |
| **Events** | HIGH | Alert tone + vibration | Challenge start, check-in reminders, admin approvals |

Channels are created in `BreathyApplication.onCreate()` using the `NotificationManager` API, with channel descriptions that help users understand what each channel controls. Users can customize each channel's behavior through Android's system notification settings, accessible via a "Notification Settings" button in Breathy's Profile screen.

### Cloud Function Triggers

Four Cloud Functions are responsible for generating push notifications based on Firestore triggers and scheduled events:

- **`onNewMessage`**: Triggered by document creation in `chats/{chatId}/messages`. Retrieves the recipient's FCM token, constructs a notification with the sender's name and message preview (truncated to 100 characters), and sends it via the FCM Admin SDK. The notification includes a deep link to the specific chat conversation.

- **`onFriendRequest`**: Triggered by document creation in `users/{uid}/friendRequests`. Sends a Social-channel notification to the recipient with the requester's name and a deep link to the friend request management screen.

- **`onEventReminder`**: A scheduled function that runs at configurable times before event deadlines. Queries all users who have not yet checked in for an active event and sends Events-channel notifications reminding them to complete their check-in before the deadline.

- **`dailyMotivation`**: Runs as part of the `dailySmokeFreeUpdate` scheduled function. Selects a personalized motivational message based on the user's smoke-free duration and sends it via the Motivation channel. Messages are drawn from a curated pool of 100+ messages organized by milestone stage, ensuring users rarely see the same message twice.

### Foreground vs Background Handling

The `BreathingMessagingService` handles incoming messages differently based on the app's foreground state. When the app is in the **foreground**, the service intercepts the message payload in `onMessageReceived()` and routes it to an in-app notification system—a lightweight composable banner that slides down from the top of the screen with the notification content and a tap action. This prevents disruptive system notification popups while the user is actively using the app. When the app is in the **background**, the service allows the FCM SDK to display a standard system notification in the notification tray. Both paths include deep-link payloads that specify the target screen and any required parameters (e.g., `chatId` for chat notifications, `eventId` for event notifications). When the user taps the notification, the deep link is resolved by the Navigation Component, which routes to the correct screen even if the app was not running.

---

## 7. Security Architecture

### Firebase Security Rules

Firebase Security Rules are the primary defense layer for Breathy's data, enforced at the Firebase server level regardless of client behavior. The rules follow the principle of least privilege, ensuring that users can only read and write data that they own or that has been explicitly shared with them. Detailed rules are maintained in a separate Security Rules document, but key principles include: users can only write to their own profile document (`request.auth.uid == resource.data.uid`), chat messages can only be sent by participants of the chat conversation, stories are publicly readable but only writable by the author, and check-in documents can only be created by the user themselves and only updated (status change) by admin-flagged users. All write operations validate the structure of incoming data, rejecting any document that contains unexpected fields, exceeds size limits, or violates type constraints. Admin-level operations (check-in approval, content moderation) are gated on a custom `isAdmin` claim verified in the security rules.

### Client-Side Input Validation

While server-side security rules are the authoritative enforcement layer, Breathy implements comprehensive client-side validation to provide immediate user feedback and reduce unnecessary network requests. All text inputs are validated before submission: story text is limited to 500 characters and filtered for potentially harmful content, chat messages are limited to 1000 characters, display names are validated against a regex pattern (2-30 characters, alphanumeric and common Unicode characters), and email addresses are validated against standard format patterns. The ViewModel layer performs this validation and returns structured `ValidationError` objects that the UI renders as inline field errors. Numeric inputs (quit date, cigarettes per day) are range-checked to prevent nonsensical values. This defense-in-depth approach ensures that even if a malicious client bypasses the UI validation, the server-side security rules will still reject invalid data.

### Firestore Field-Level Security

Firestore Security Rules support `allow read, write: if` conditions that can reference specific fields, enabling field-level access control. Breathy uses this capability to protect sensitive fields such as `fcmToken`, `isAdmin`, and `email` from unauthorized modification. For example, the rule for the `users/{uid}` document allows authenticated users to write to most fields in their own document, but explicitly blocks writes to `isAdmin` (which can only be set by Cloud Functions) and restricts `fcmToken` updates to the device's own token. The `publicProfiles/{uid}` collection is readable by all authenticated users but only writable by Cloud Functions and the profile owner, with field-level restrictions that prevent users from forging their `daysSmokeFree`, `xp`, or `rank` values. This granular control prevents the most common attack vectors in social applications: profile spoofing, stat inflation, and privilege escalation.

### Storage Path Validation

Firebase Storage security rules enforce strict path validation to prevent unauthorized file access or overwrites. Story photos must be uploaded to `stories/{uid}/{filename}` and can only be written by the authenticated user matching the `{uid}` segment. Check-in videos must be uploaded to `checkins/{eventId}/{uid}_{timestamp}.mp4` and can only be written by the matching `{uid}` user. File size limits are enforced at the Storage rules level: story photos are limited to 5 MB and check-in videos to 50 MB. File type validation ensures that only image/* MIME types are accepted for story photos and only video/mp4 for check-in videos. Read access to Storage objects is governed by the same authentication requirements—only authenticated users can read story photos, and check-in videos are only readable by the uploading user and admin-flagged users.

### Rate Limiting and API Security

Cloud Functions implement rate limiting to prevent abuse of expensive operations like AI Coach calls and notification dispatches. The `callAICoach` Cloud Function checks the user's `lastCoachCall` timestamp and enforces a minimum 5-second interval between calls, returning a `RESOURCE_EXHAUSTED` error if the limit is exceeded. The notification dispatch functions implement per-user daily caps (maximum 5 motivational notifications per day) to prevent notification spam. API keys for OpenAI are stored as Cloud Functions environment variables and are never exposed to the client—AI Coach requests are routed through a Cloud Function that acts as a proxy, adding the API key server-side and forwarding the request to OpenAI. Google Cloud API key restrictions are configured in the Google Cloud Console to limit each key to specific APIs (e.g., the Android API key is restricted to Firebase Installations and FCM) and specific Android app signing certificates, preventing key extraction and misuse.

### Local Data Protection

Breathy adheres to the principle of minimizing sensitive data stored on the device. The Firestore local cache contains user-generated content (stories, chat messages) and profile data, but never stores authentication credentials (the Firebase Auth SDK manages these in the Android Keystore). FCM tokens are stored in Firestore and the Firebase Installations SDK but are not persisted in SharedPreferences or local files. No payment card information is ever stored locally—Google Play Billing handles all payment processing externally. The app does not use `FLAG_SECURE` or encrypted SharedPreferences for the current version, but these protections are planned for a future release to prevent screenshots and local data extraction on rooted devices.

---

## 8. Performance Optimization

### Lazy Loading and List Performance

All list-based screens in Breathy use Jetpack Compose's `LazyColumn` and `LazyRow` with explicit `key` parameters to ensure efficient item recycling and stable recomposition. The community stories feed, chat message list, and event listing all implement Firestore pagination using `Query.limit(20)` and `Query.startAfter(lastDocument)` to load data in pages of 20 items, with a "load more" trigger at the bottom of each list. Each `LazyColumn` item is keyed by its Firestore document ID, enabling Compose to efficiently diff, reorder, and animate list changes without full recomposition. Story cards use `SubcomposeAsyncImage` from Coil to defer image loading until the item is visible on screen, and the `rememberLazyListState()` is preserved across navigation events to maintain scroll position. For the chat screen, the list is configured with `reverseLayout = true` so that new messages appear at the bottom and auto-scroll is managed by `animateScrollToItem(0)` when a new message arrives.

### Image Compression and Upload Optimization

Before uploading any user-provided image, Breathy applies aggressive compression to minimize bandwidth consumption and Storage costs. The `ImageCompressor` utility in the Data Layer decodes the image URI into a `Bitmap`, scales it down to a maximum dimension of 1080 pixels (maintaining aspect ratio), re-encodes it as WebP at 80% quality using `Bitmap.compress(WebP, 80, outputStream)`, and writes the result to a temporary file in the app's cache directory. This typically reduces a 5 MB camera photo to under 300 KB with visually imperceptible quality loss. For check-in videos, the CameraX recorder is pre-configured with a 720p quality target and 2 Mbps bitrate, producing files that average 3-5 MB for a 10-second recording. All uploads use Firebase Storage's resumable upload feature, which breaks large files into chunks and allows the upload to resume from the last successful chunk if the connection drops, avoiding the need to re-upload the entire file.

### Firestore Query Optimization

Firestore query performance is optimized through careful index management, query scoping, and listener lifecycle management. All compound queries (e.g., "stories ordered by timestamp where authorId == currentUserId") have composite indexes defined in `firestore.indexes.json` and deployed automatically via Firebase CLI. The community stories feed uses a `orderBy("timestamp", Descending).limit(20)` query that leverages the automatic single-field index on `timestamp`, requiring no additional composite index. Snapshot listeners are managed with `DisposableEffect` in Compose, ensuring that listeners are attached when a screen becomes visible and detached when the user navigates away, preventing memory leaks and unnecessary Firestore read billing. For the daily stats update, the Cloud Function uses Firestore batch writes (up to 500 operations per batch) to update multiple user documents atomically, reducing write costs and ensuring consistency. The `publicProfiles` collection is denormalized from the `users` collection to allow leaderboard queries without exposing private user data, and it is kept in sync by Cloud Functions triggered on user document writes.

### Memory Management

Breathy follows strict memory management practices to prevent leaks and ensure smooth performance on devices with limited RAM. All Firestore snapshot listeners are registered in `ViewModel.init` and unregistered in `ViewModel.onCleared()`, with Compose's `DisposableEffect` providing an additional safety net for screen-scoped listeners. Coroutine scopes are carefully managed: `viewModelScope` is used for ViewModel-level operations that should be cancelled when the ViewModel is cleared, and `lifecycleScope` is used for Activity/Fragment-level operations that should survive configuration changes. The Coil `ImageLoader` singleton is configured with a memory cache sized at 25% of the available app memory (retrieved via `ActivityManager.getMemoryClass()`), and disk cache is limited to 50 MB. CameraX resources are released in `ProcessCameraProvider.unbindAll()` when the user navigates away from the recording screen. All `Flow` and `StateFlow` collectors use the `collectAsStateWithLifecycle()` extension, which automatically pauses collection when the app is backgrounded, preventing unnecessary CPU usage and battery drain.

### Build Optimization

Release builds of Breathy are optimized through ProGuard/R8 configuration to minimize APK size and obfuscate code. The ProGuard rules retain all classes accessed via reflection (Firebase, Coil, Compose), strip unused resources via `shrinkResources true`, and enable `minifyEnabled true` for code obfuscation. Firebase Crashlytics automatically handles mapping file uploads for deobfuscated stack traces. The app targets the latest Android App Bundle format, which reduces download size by delivering only the resources and native libraries needed for the user's device configuration. Baseline profiles are generated for critical user journeys (app launch, home screen, craving flow) to improve startup time by 20-30% on supported devices. The current release APK size is approximately 8 MB, well within the threshold for instant app installation on slow connections.

---

## 9. Error Handling Strategy

### Network Errors

Network errors are the most common failure mode in a mobile application, and Breathy handles them with a user-friendly, retry-centric approach. When a repository method encounters a network error (detected via Firestore's `FirebaseFirestoreException` with `UNAVAILABLE` or `DEADLINE_EXCEEDED` codes, or OkHttp's `IOException`), the ViewModel catches the exception and updates its UI state to include an error field of type `NetworkError`. The Compose UI renders this as a `Snackbar` with a "Retry" action button that re-triggers the failed operation. For persistent network failures (3+ consecutive retries), the app displays a full-screen offline indicator with a "Check your connection" message and automatically retries when the `NetworkMonitor` detects connectivity restoration. Firestore's built-in offline persistence ensures that read operations never fail due to network issues—they simply return cached data—so network errors primarily affect write operations and AI Coach calls, which are queued and retried automatically.

### Authentication Errors

Firebase Authentication errors are mapped to user-friendly messages that guide the user toward resolution without exposing technical details. The `AuthErrorHandler` utility translates Firebase error codes into specific, actionable messages: `ERROR_USER_NOT_FOUND` becomes "No account found with this email. Would you like to sign up?", `ERROR_WRONG_PASSWORD` becomes "Incorrect password. Try again or reset your password.", `ERROR_EMAIL_ALREADY_IN_USE` becomes "An account already exists with this email. Try signing in instead.", and `ERROR_TOO_MANY_REQUESTS` becomes "Too many attempts. Please wait a few minutes and try again." For Google Sign-In failures, the error handler distinguishes between user-initiated cancellation (which silently returns to the sign-in screen), network errors (which show a retry snackbar), and configuration errors (which show a generic "Sign-in is temporarily unavailable" message). All auth errors are logged to Crashlytics as non-fatal exceptions with custom keys (e.g., `auth_method`, `error_code`) for monitoring and alerting.

### Firestore Errors

Firestore write errors are handled with a retry strategy that balances reliability with user experience. For idempotent write operations (e.g., updating the user's stats), the repository implementation uses Firestore's built-in retry with exponential backoff, starting at 1 second and doubling up to 64 seconds. For non-idempotent operations (e.g., sending a chat message), the repository checks for `ALREADY_EXISTS` errors to detect duplicate writes and gracefully ignores them. Transaction failures (e.g., when updating XP and achievements atomically) are retried up to 5 times before surfacing an error to the user. The `FirestoreErrorHandler` utility maps Firestore exception codes to UI-friendly messages: `PERMISSION_DENIED` becomes "You don't have permission to perform this action", `NOT_FOUND` becomes "The requested data is no longer available", and `RESOURCE_EXHAUSTED` becomes "You're doing that a bit too often. Please wait a moment." All Firestore errors are logged with the collection path, operation type, and error code for debugging.

### Upload Errors

File upload errors require special handling due to their potentially long duration and large size. Firebase Storage uploads use the resumable upload API, which automatically retries interrupted uploads from the last successful byte. If an upload fails with a non-recoverable error (e.g., `OBJECT_NOT_FOUND` or `QUOTA_EXCEEDED`), the ViewModel displays a persistent error notification with a "Retry Upload" button that restarts the upload from the beginning. For very large video uploads (check-in videos), the app monitors upload progress and displays a progress bar with an estimated time remaining. If the app is backgrounded during an upload, a foreground service keeps the upload alive and displays a system notification with progress. The `UploadManager` singleton tracks all active uploads and provides a centralized retry mechanism that prioritizes smaller files (story photos) over larger files (check-in videos) to maximize the user's perceived responsiveness.

### AI Coach Errors

AI Coach conversations rely on an external OpenAI API call proxied through a Cloud Function, introducing multiple failure points: network errors, Cloud Function errors, rate limit errors, and OpenAI API errors. The `AICoachViewModel` handles each scenario gracefully. If the Cloud Function returns a `RESOURCE_EXHAUSTED` error (rate limit), the ViewModel displays a "You're chatting a bit too fast. Take a breath and try again in a moment." message. If the OpenAI API returns a server error or times out, the ViewModel shows a fallback motivational message from a local pool of 20 pre-written responses, along with a "Try Again" button. If the network is entirely unavailable, the AI Coach screen displays a "Connect to the internet to chat with your coach" placeholder with a breathing exercise suggestion as an alternative coping method. All AI Coach errors are logged to Crashlytics with the error type and Cloud Function execution ID for server-side debugging, and frequent failures trigger a PagerDuty alert to the on-call engineer.

### Global Exception Handling

Breathy implements a multi-layered global exception handling strategy to catch and report errors that escape individual ViewModel try-catch blocks. At the top level, `BreathyApplication` registers a `Thread.UncaughtExceptionHandler` that intercepts uncaught exceptions, logs them to Firebase Crashlytics with custom keys (current screen, user ID, app version), and presents a "Something went wrong" dialog before allowing the app to terminate. Within coroutines, a custom `CoroutineExceptionHandler` is installed on all `viewModelScope` and `lifecycleScope` instances, catching unhandled coroutine exceptions and routing them through the same Crashlytics logging pipeline. Compose runtime errors are caught by a `RecyclerView.ItemAnimator`-compatible error boundary that prevents individual composable crashes from crashing the entire app. All logged errors include a rich context payload: the user's authentication state, network connectivity status, the screen they were on, and the last action they took. This comprehensive error reporting has proven invaluable for reproducing and fixing production issues, with Crashlytics-free user rates consistently above 99.5%.

---

## Appendix: Technology Stack Summary

| Category | Technology | Version |
|----------|-----------|---------|
| Language | Kotlin | 1.9.x |
| UI Framework | Jetpack Compose | BOM 2024.x |
| Navigation | Compose Navigation | 2.7.x |
| DI | Manual (AppModule) | — |
| Auth | Firebase Auth | 22.x |
| Database | Firebase Firestore | 24.x |
| Storage | Firebase Storage | 20.x |
| Cloud Functions | Firebase Functions | 2.x |
| Push | Firebase Cloud Messaging | 23.x |
| Analytics | Firebase Analytics | 21.x |
| Crash Reporting | Firebase Crashlytics | 18.x |
| Image Loading | Coil | 3.0 |
| Camera | CameraX | 1.3.x |
| Payments | Google Play Billing | 6.0 |
| Ads | AdMob | 22.x |
| AI | OpenAI API (via Cloud Functions) | GPT-4 |
| Confetti | Konfetti | 2.0 |
| Coroutines | Kotlinx Coroutines | 1.7.x |
| Serialization | Kotlinx Serialization | 1.6.x |

---

*Document generated by the Breathy engineering team. For questions or clarifications, contact the architecture review board.*
