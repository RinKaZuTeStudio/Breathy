# Breathy — Firestore Data Model

> **Version:** 1.0  
> **Last Updated:** 2025-03-04  
> **Status:** Canonical Reference  

---

## Table of Contents

1. [Overview](#1-overview)  
2. [ER Diagram (ASCII)](#2-er-diagram-ascii)  
3. [Collection Definitions](#3-collection-definitions)  
   3.1 [users](#31-usersuserid-private)  
   3.2 [publicProfiles](#32-publicprofilesuserid-public)  
   3.3 [stories](#33-storiesstoryid)  
   3.4 [replies (subcollection)](#34-repliesreplyid--storiesstoryidreplies)  
   3.5 [friendRequests](#35-friendrequestsrequestid)  
   3.6 [friendships](#36-friendshipsfriendshipid)  
   3.7 [chats](#37-chatschatid-deterministic)  
   3.8 [messages (subcollection)](#38-messagesmessageid--chatschatidmessages)  
   3.9 [events](#39-eventseventid)  
   3.10 [eventParticipants](#310-eventparticipantsuserideventid)  
   3.11 [eventCheckins](#311-eventcheckinscheckinid)  
   3.12 [craving_logs](#312-craving_logslogid)  
   3.13 [subscriptions](#313-subscriptionsuserid)  
   3.14 [coach_chats (subcollection)](#314-coat_chatschatid--usersuseridcoach_chats)  
4. [Index Requirements](#4-index-requirements)  
5. [Data Integrity Rules](#5-data-integrity-rules)  
6. [Cloud Function Triggers](#6-cloud-function-triggers)  
7. [Appendix — Naming Conventions & ID Strategy](#7-appendix--naming-conventions--id-strategy)

---

## 1. Overview

Breathy uses **Cloud Firestore** (Native mode) as its sole persistent data store. The data model follows these principles:

| Principle | Description |
|---|---|
| **Denormalization for reads** | Frequently-read data (nickname, photoURL, daysSmokeFree) is duplicated into publicProfiles and stories so that feed screens never need fan-out reads. |
| **Subcollections for ownership** | Replies live under `stories/{storyId}/replies/`; messages live under `chats/{chatId}/messages/`; coach_chats live under `users/{userId}/coach_chats/`. This guarantees O(1) look-up by parent ID and enforces access via path. |
| **Deterministic IDs** | Chat documents use `_`-joined sorted UIDs; event-participant docs use `{userId}_{eventId}`. This prevents duplicates without transactions. |
| **Counter consistency** | Likes, replyCount, and coins are maintained by Cloud Functions with distributed counter patterns where write-hotting is possible. |
| **Security-rule alignment** | `users` is private (owner-only); `publicProfiles` is world-readable / owner-writable; stories are world-readable / owner-writable; subcollections inherit parent access. |

---

## 2. ER Diagram (ASCII)

```
┌──────────────────────┐          ┌─────────────────────────┐
│       users          │ 1      1 │    publicProfiles       │
│──────────────────────│──────────│─────────────────────────│
│ userId (PK)          │          │ userId (PK)             │
│ email                │          │ nickname                │
│ nickname             │          │ photoURL                │
│ quitDate             │          │ daysSmokeFree           │
│ xp, coins, level     │          │ xp                      │
│ achievements[]       │          │ location                │
│ givenLikes[]         │          │ quitDate                │
│ fcmToken             │          └─────────────────────────┘
│ …                    │
└──────┬───────────────┘
       │
       │ 1
       ├─────────────────────────────┐
       │                             │
       │ N                           │ N
┌──────┴──────────────┐   ┌─────────┴──────────────────┐
│   craving_logs      │   │   stories                  │
│─────────────────────│   │────────────────────────────│
│ logId (PK)          │   │ storyId (PK)               │
│ userId (FK→users)   │   │ userId (FK→users)          │
│ timestamp           │   │ nickname (denorm)          │
│ copingMethod        │   │ photoURL  (denorm)         │
│ success             │   │ content                    │
└─────────────────────│   │ lifeChanges[]              │
                      │   │ daysSmokeFree              │
                      │   │ likes (counter)            │
                      │   │ likedBy[]                  │
                      │   │ replyCount (counter)       │
                      │   │ createdAt                  │
                      │   └──────────┬─────────────────┘
                      │              │
                      │              │ 1
                      │              │
                      │              │ N
                      │   ┌──────────┴─────────────────┐
                      │   │   replies (subcollection)   │
                      │   │────────────────────────────│
                      │   │ replyId (PK)               │
                      │   │ storyId (FK→stories)       │
                      │   │ userId (FK→users)          │
                      │   │ nickname (denorm)          │
                      │   │ photoURL  (denorm)         │
                      │   │ content                    │
                      │   │ parentReplyId (nullable)   │
                      │   │ createdAt                  │
                      │   └────────────────────────────┘
                      │
       ┌──────────────┴──────────────────────────────┐
       │                                             │
       │ N                                           │ N
┌──────┴──────────────┐                    ┌─────────┴───────────────┐
│  friendRequests     │                    │   friendships           │
│─────────────────────│                    │─────────────────────────│
│ requestId (PK)      │     ┌─────┐        │ friendshipId (PK)       │
│ fromUserId (FK)     │────→│users│←───────│ userIds[] (exactly 2)   │
│ toUserId   (FK)     │     └─────┘        │ createdAt               │
│ status              │                    └─────────────────────────┘
│ timestamp           │
└─────────────────────┘

┌───────┐        ┌──────────────────────┐        ┌───────────────────────────┐
│ users │ 1    N │   chats              │ 1    N │ messages (subcollection)  │
│───────│────────│──────────────────────│────────│───────────────────────────│
│       │        │ chatId (PK, determ.) │        │ messageId (PK)            │
│       │        │ participants[] (FK)  │        │ senderId (FK→users)       │
│       │        │ lastMessage          │        │ text                      │
│       │        │ lastUpdated          │        │ timestamp                 │
│       │        │ typing Map<uid,ts>   │        │ read                      │
│       │        └──────────────────────┘        └───────────────────────────┘

┌──────────────────┐          ┌──────────────────────────────┐
│     events       │ 1     N  │   eventParticipants          │
│──────────────────│──────────│──────────────────────────────│
│ eventId (PK)     │          │ participantId (PK, determ.)  │
│ title            │          │ userId (FK→users)            │
│ description      │          │ eventId (FK→events)          │
│ startDate        │          │ currentStreak                │
│ endDate          │          │ totalApprovedDays             │
│ active           │          │ completed                    │
│ prizes Map       │          │ completionTimestamp          │
│ dailyRequired    │          │ joinedAt                     │
└──────────────────┘          │ rank (computed)              │
                              └──────────┬───────────────────┘
                                         │
                                         │ 1
                                         │
                                         │ N
                              ┌──────────┴───────────────────┐
                              │   eventCheckins              │
                              │──────────────────────────────│
                              │ checkinId (PK)               │
                              │ userId (FK→users)            │
                              │ eventId (FK→events)          │
                              │ dayNumber                    │
                              │ videoURL                     │
                              │ status (pending/approved/…)  │
                              │ submittedAt                  │
                              │ reviewedAt (optional)        │
                              │ reviewComment (optional)     │
                              └──────────────────────────────┘

┌───────┐        ┌───────────────────────────┐
│ users │ 1   1  │   subscriptions           │
│───────│────────│───────────────────────────│
│       │        │ userId (PK)                │
│       │        │ active                     │
│       │        │ plan                       │
│       │        │ expiresAt                  │
│       │        │ purchaseToken              │
│       │        └───────────────────────────┘
│       │
│       │ 1   N  ┌───────────────────────────┐
│       │────────│ coach_chats (subcollection)│
│              ││───────────────────────────│
│               │ chatId (PK, auto)          │
│               │ role (user/assistant)      │
│               │ content                    │
│               │ timestamp                  │
│               └───────────────────────────┘
```

---

## 3. Collection Definitions

---

### 3.1 `users/{userId}` (Private)

> **Access:** Owner read/write only (via security rules). Document ID = Firebase Auth UID.

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `email` | String | ✅ | User's email from auth provider |
| `nickname` | String | ✅ | Display name (3-20 chars) |
| `age` | Int | ❌ | Age at sign-up; optional for privacy |
| `quitDate` | Timestamp | ✅ | The date the user committed to quit smoking |
| `quitType` | String | ✅ | `"instant"` — stopped immediately; `"gradual"` — tapering plan |
| `cigarettesPerDay` | Int | ✅ | Baseline cigarettes/day at quit time |
| `pricePerPack` | Double | ✅ | Local currency price of one pack (used for savings calc) |
| `cigarettesPerPack` | Int | ✅ | Number of cigarettes in one pack |
| `xp` | Int | ✅ | Total experience points earned; never decrements |
| `coins` | Int | ✅ | Spendable virtual currency |
| `level` | Int | ✅ | **Computed from XP** — `level = floor(xp / 100) + 1` |
| `lastDailyClaim` | Timestamp | ❌ | Timestamp of last daily coin claim; `null` if never claimed |
| `createdAt` | Timestamp | ✅ | Account creation time |
| `achievements` | Array\<String\> | ✅ | List of achievement IDs the user has unlocked (e.g., `["first_day", "first_week"]`) |
| `givenLikes` | Array\<String\> | ✅ | Story IDs the user has liked — used to toggle like state without extra reads |
| `fcmToken` | String | ❌ | Firebase Cloud Messaging token for push notifications |
| `photoURL` | String | ❌ | Profile photo URL (Firebase Storage or default avatar) |
| `location` | String | ❌ | City or region string for community filtering |

#### Example Document

```json
{
  "email": "alex@example.com",
  "nickname": "AlexSmokeFree",
  "age": 32,
  "quitDate": "2025-01-15T00:00:00Z",
  "quitType": "instant",
  "cigarettesPerDay": 15,
  "pricePerPack": 8.50,
  "cigarettesPerPack": 20,
  "xp": 2450,
  "coins": 380,
  "level": 25,
  "lastDailyClaim": "2025-03-03T08:12:00Z",
  "createdAt": "2025-01-15T10:30:00Z",
  "achievements": ["first_day", "first_week", "first_month", "craving_master"],
  "givenLikes": ["story_abc123", "story_def456"],
  "fcmToken": "dF7k9...long_token...xR2",
  "photoURL": "https://storage.googleapis.com/breathy-avatars/uid123.jpg",
  "location": "Berlin, Germany"
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `publicProfiles/{userId}` | 1:1 mirror | Same document ID; denormalized public subset |
| `stories` | 1:N | `stories.userId` → `users/{userId}` |
| `craving_logs` | 1:N | `craving_logs.userId` → `users/{userId}` |
| `friendRequests` | 1:N | `fromUserId` / `toUserId` → `users/{userId}` |
| `friendships` | 1:N | `userIds` contains `userId` |
| `chats` | 1:N | `participants` contains `userId` |
| `subscriptions/{userId}` | 1:1 | Same document ID |
| `coach_chats` (sub) | 1:N | Subcollection under `users/{userId}` |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `doc(userId).get()` | Profile load | — (single doc) |
| `where("email", "==", e)` | Admin lookup | Single-field index on `email` |

---

### 3.2 `publicProfiles/{userId}` (Public)

> **Access:** World-readable; owner-writable only. Document ID = same Firebase Auth UID as `users`.

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `nickname` | String | ✅ | Denormalized from `users` |
| `photoURL` | String | ❌ | Denormalized from `users` |
| `daysSmokeFree` | Int | ✅ | Updated daily by scheduled Cloud Function |
| `xp` | Int | ✅ | Denormalized from `users`; used for leaderboards |
| `location` | String | ❌ | Denormalized from `users` |
| `quitDate` | Timestamp | ✅ | Denormalized from `users` |

#### Example Document

```json
{
  "nickname": "AlexSmokeFree",
  "photoURL": "https://storage.googleapis.com/breathy-avatars/uid123.jpg",
  "daysSmokeFree": 48,
  "xp": 2450,
  "location": "Berlin, Germany",
  "quitDate": "2025-01-15T00:00:00Z"
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `users/{userId}` | 1:1 mirror | Same ID; denormalized from private doc |
| `stories` | Referenced | Story feed shows `publicProfile` data inline |
| `friendships` | Referenced | Friends list queries publicProfiles for display |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `doc(userId).get()` | Friend profile display | — (single doc) |
| `orderBy("xp", desc).limit(50)` | Leaderboard | Composite index: `xp DESC` |
| `orderBy("daysSmokeFree", desc).limit(50)` | Smoke-free leaderboard | Composite index: `daysSmokeFree DESC` |

---

### 3.3 `stories/{storyId}`

> **Access:** World-readable; owner-writable (content, lifeChanges); anyone can increment `likes` via Cloud Function.

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `userId` | String | ✅ | Author's UID (FK → `users`) |
| `nickname` | String | ✅ | Denormalized author display name |
| `photoURL` | String | ❌ | Denormalized author avatar |
| `content` | String | ✅ | Story body text (max 1 000 chars) |
| `lifeChanges` | Array\<String\> | ✅ | Tags describing life improvements (e.g., `["better_sleep", "more_energy"]`) |
| `daysSmokeFree` | Int | ✅ | Author's smoke-free day count at post time (snapshot) |
| `likes` | Int | ✅ | Like counter; incremented atomically |
| `likedBy` | Array\<String\> | ✅ | User IDs who liked — used to detect duplicates and toggle |
| `replyCount` | Int | ✅ | Counter of top-level replies; maintained by Cloud Function |
| `createdAt` | Timestamp | ✅ | Server timestamp at creation |

#### Example Document

```json
{
  "userId": "uid123",
  "nickname": "AlexSmokeFree",
  "photoURL": "https://storage.googleapis.com/breathy-avatars/uid123.jpg",
  "content": "Day 48 — I can finally run up the stairs without wheezing. The breathing exercises really do help when cravings hit hard.",
  "lifeChanges": ["better_sleep", "more_energy", "better_breathing"],
  "daysSmokeFree": 48,
  "likes": 27,
  "likedBy": ["uid456", "uid789", "uid012"],
  "replyCount": 5,
  "createdAt": "2025-03-04T14:22:00Z"
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `users/{userId}` | N:1 | `stories.userId` → `users` |
| `publicProfiles/{userId}` | Denorm source | `nickname`, `photoURL` copied at write time |
| `replies` (sub) | 1:N | Subcollection `stories/{storyId}/replies/` |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `orderBy("createdAt", desc).limit(20)` | Main feed | Composite index: `createdAt DESC` |
| `where("userId", "==", uid).orderBy("createdAt", desc)` | User's own stories | Composite index: `userId ASC, createdAt DESC` |
| `where("likedBy", "array-contains", uid)` | Stories I liked | Single-field index (auto) |
| `where("lifeChanges", "array-contains", tag)` | Filter by tag | Composite index: `lifeChanges ASC, createdAt DESC` |

---

### 3.4 `replies/{replyId}` — `stories/{storyId}/replies/`

> **Access:** World-readable; owner-writable. Subcollection under `stories/{storyId}`.

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `storyId` | String | ✅ | Parent story ID (redundant for convenience in Cloud Functions) |
| `userId` | String | ✅ | Author's UID |
| `nickname` | String | ✅ | Denormalized author display name |
| `photoURL` | String | ❌ | Denormalized author avatar |
| `content` | String | ✅ | Reply body (max 500 chars) |
| `parentReplyId` | String | ❌ | ID of parent reply for nested threads; `null` for top-level |
| `createdAt` | Timestamp | ✅ | Server timestamp |

#### Example Document

```json
{
  "storyId": "story_abc123",
  "userId": "uid456",
  "nickname": "QuitterJen",
  "photoURL": null,
  "content": "That's awesome! I'm on day 12 and already noticing better sleep.",
  "parentReplyId": null,
  "createdAt": "2025-03-04T15:01:00Z"
}
```

#### Nested reply example

```json
{
  "storyId": "story_abc123",
  "userId": "uid123",
  "nickname": "AlexSmokeFree",
  "photoURL": "https://storage.googleapis.com/breathy-avatars/uid123.jpg",
  "content": "Thanks Jen! Day 12 is huge — keep going!",
  "parentReplyId": "reply_xyz789",
  "createdAt": "2025-03-04T15:10:00Z"
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `stories/{storyId}` | N:1 (parent) | Subcollection path |
| `users/{userId}` | N:1 | `replies.userId` → `users` |
| `replies/{parentReplyId}` | Self-referential | `parentReplyId` → another reply in same subcollection |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `orderBy("createdAt", asc).limit(50)` | Flat reply list | Composite index: `createdAt ASC` |
| `where("parentReplyId", "==", rid).orderBy("createdAt", asc)` | Threaded replies | Composite index: `parentReplyId ASC, createdAt ASC` |

---

### 3.5 `friendRequests/{requestId}`

> **Access:** Sender can create; recipient can read & update status. Auto-ID document.

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `fromUserId` | String | ✅ | UID of the requester |
| `toUserId` | String | ✅ | UID of the recipient |
| `status` | String | ✅ | `"pending"` / `"accepted"` / `"rejected"` |
| `timestamp` | Timestamp | ✅ | Server timestamp when created |

#### Example Document

```json
{
  "fromUserId": "uid123",
  "toUserId": "uid456",
  "status": "pending",
  "timestamp": "2025-03-04T10:00:00Z"
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `users/{fromUserId}` | N:1 | Requester profile |
| `users/{toUserId}` | N:1 | Recipient profile |
| `friendships` | 1:1 (on accept) | Accepting creates a `friendships` doc |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `where("toUserId", "==", uid).where("status", "==", "pending")` | Incoming requests | Composite index: `toUserId ASC, status ASC, timestamp DESC` |
| `where("fromUserId", "==", uid).where("status", "==", "pending")` | Outgoing requests | Composite index: `fromUserId ASC, status ASC, timestamp DESC` |
| `where("fromUserId", "==", a).where("toUserId", "==", b)` | Check existing request | Composite index: `fromUserId ASC, toUserId ASC` |

---

### 3.6 `friendships/{friendshipId}`

> **Access:** Both users in `userIds` can read; system writes. Auto-ID document.

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `userIds` | Array\<String\> | ✅ | Exactly 2 UIDs — the two friends |
| `createdAt` | Timestamp | ✅ | When the friendship was established |

#### Example Document

```json
{
  "userIds": ["uid123", "uid456"],
  "createdAt": "2025-03-04T10:05:00Z"
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `users` | N:2 | Each entry in `userIds` references a user |
| `publicProfiles` | Denorm source | Friend list UI reads publicProfiles for display |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `where("userIds", "array-contains", uid)` | Get all friendships for a user | Single-field index (auto for array-contains) |

---

### 3.7 `chats/{chatId}` (Deterministic)

> **Access:** Only participants can read/write. Document ID = sorted UIDs joined by `"_"` (e.g., `uid123_uid456`).

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `participants` | Array\<String\> | ✅ | Exactly 2 UIDs |
| `lastMessage` | String | ✅ | Preview text of the most recent message (truncated to 100 chars) |
| `lastUpdated` | Timestamp | ✅ | Timestamp of most recent message; used for chat list ordering |
| `typing` | Map\<String, Timestamp\> | ✅ | `userId → expiry timestamp` indicating who is typing; cleared on message send |

#### Example Document

```json
{
  "participants": ["uid123", "uid456"],
  "lastMessage": "Keep going! Day 30 is a huge milestone 🎉",
  "lastUpdated": "2025-03-04T16:45:00Z",
  "typing": {
    "uid123": "2025-03-04T16:46:00Z"
  }
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `users` | N:2 | `participants` references |
| `messages` (sub) | 1:N | Subcollection `chats/{chatId}/messages/` |
| `friendships` | Precondition | Chat can only exist between friends (enforced by security rules) |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `where("participants", "array-contains", uid).orderBy("lastUpdated", desc)` | Chat list | Composite index: `participants ASC, lastUpdated DESC` |
| `doc(sortedId).get()` | Open specific chat | — (single doc, deterministic ID) |

---

### 3.8 `messages/{messageId}` — `chats/{chatId}/messages/`

> **Access:** Same as parent `chats` doc — only participants. Auto-ID subcollection.

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `senderId` | String | ✅ | UID of the sender |
| `text` | String | ✅ | Message body (max 2 000 chars) |
| `timestamp` | Timestamp | ✅ | Server timestamp |
| `read` | Boolean | ✅ | `false` until recipient opens the chat; updated by recipient's client |

#### Example Document

```json
{
  "senderId": "uid456",
  "text": "Keep going! Day 30 is a huge milestone 🎉",
  "timestamp": "2025-03-04T16:45:00Z",
  "read": false
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `chats/{chatId}` | N:1 (parent) | Subcollection path |
| `users/{senderId}` | N:1 | Sender reference |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `orderBy("timestamp", asc).limitToLast(50)` | Load recent messages | Composite index: `timestamp ASC` |
| `where("read", "==", false).where("senderId", "!=", myUid)` | Unread count | Composite index: `read ASC, senderId ASC` |

---

### 3.9 `events/{eventId}`

> **Access:** World-readable; admin-writable. Named document IDs (e.g., `pushup_challenge_2025`).

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `title` | String | ✅ | Display name of the event |
| `description` | String | ✅ | Full description / rules |
| `startDate` | Timestamp | ✅ | When the event begins |
| `endDate` | Timestamp | ✅ | When the event ends |
| `active` | Boolean | ✅ | Whether the event is currently accepting participants |
| `prizes` | Map\<String, String\> | ✅ | Rank → prize description (e.g., `"1st": "100 coins"`) |
| `dailyRequired` | Int | ✅ | Number of push-ups required per day |

#### Example Document

```json
{
  "title": "Push-up Challenge 2025",
  "description": "Complete 20 push-ups every day for 30 days to build discipline and earn exclusive badges. Record a short video each day as proof!",
  "startDate": "2025-04-01T00:00:00Z",
  "endDate": "2025-04-30T23:59:59Z",
  "active": true,
  "prizes": {
    "1st": "100 coins",
    "2nd": "50 coins",
    "3rd": "25 coins"
  },
  "dailyRequired": 20
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `eventParticipants` | 1:N | `eventParticipants.eventId` → `events/{eventId}` |
| `eventCheckins` | 1:N | `eventCheckins.eventId` → `events/{eventId}` |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `where("active", "==", true)` | List active events | Single-field index on `active` |
| `doc("pushup_challenge_2025").get()` | Load specific event | — (single doc, named ID) |

---

### 3.10 `eventParticipants/{userId_eventId}`

> **Access:** Participant can read/write own doc; event reviewers can read all. Document ID = `{userId}_{eventId}` (deterministic, prevents double-join).

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `userId` | String | ✅ | FK → `users` |
| `eventId` | String | ✅ | FK → `events` |
| `currentStreak` | Int | ✅ | Consecutive approved days (resets on miss) |
| `totalApprovedDays` | Int | ✅ | Total days with approved check-ins |
| `completed` | Boolean | ✅ | `true` when all days are approved |
| `completionTimestamp` | Timestamp | ❌ | When the event was fully completed |
| `joinedAt` | Timestamp | ✅ | When the user joined the event |
| `rank` | Int | ✅ | **Computed** — position on the leaderboard; updated by Cloud Function |

#### Example Document

```json
{
  "userId": "uid123",
  "eventId": "pushup_challenge_2025",
  "currentStreak": 12,
  "totalApprovedDays": 18,
  "completed": false,
  "completionTimestamp": null,
  "joinedAt": "2025-03-28T09:00:00Z",
  "rank": 3
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `events/{eventId}` | N:1 | `eventParticipants.eventId` |
| `users/{userId}` | N:1 | `eventParticipants.userId` |
| `eventCheckins` | 1:N | Check-ins for this participant + event combination |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `where("eventId", "==", eid).orderBy("rank", asc).limit(50)` | Event leaderboard | Composite index: `eventId ASC, rank ASC` |
| `where("userId", "==", uid).orderBy("joinedAt", desc)` | User's event history | Composite index: `userId ASC, joinedAt DESC` |
| `doc("uid123_pushup_challenge_2025").get()` | Check if already joined | — (single doc, deterministic ID) |

---

### 3.11 `eventCheckins/{checkinId}`

> **Access:** Submitter can create; reviewers can update status. Auto-ID document.

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `userId` | String | ✅ | FK → `users` |
| `eventId` | String | ✅ | FK → `events` |
| `dayNumber` | Int | ✅ | 1-based day number within the event |
| `videoURL` | String | ✅ | Firebase Storage URL of the check-in video |
| `status` | String | ✅ | `"pending"` / `"approved"` / `"rejected"` |
| `submittedAt` | Timestamp | ✅ | When the user submitted the check-in |
| `reviewedAt` | Timestamp | ❌ | When a reviewer processed the check-in |
| `reviewComment` | String | ❌ | Optional feedback from reviewer (e.g., rejection reason) |

#### Example Document

```json
{
  "userId": "uid123",
  "eventId": "pushup_challenge_2025",
  "dayNumber": 12,
  "videoURL": "https://storage.googleapis.com/breathy-checkins/uid123_day12.mp4",
  "status": "approved",
  "submittedAt": "2025-04-12T07:30:00Z",
  "reviewedAt": "2025-04-12T08:15:00Z",
  "reviewComment": null
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `events/{eventId}` | N:1 | `eventCheckins.eventId` |
| `users/{userId}` | N:1 | `eventCheckins.userId` |
| `eventParticipants` | N:1 (logical) | Composite key `{userId}_{eventId}` |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `where("status", "==", "pending").orderBy("submittedAt", asc)` | Review queue | Composite index: `status ASC, submittedAt ASC` |
| `where("userId", "==", uid).where("eventId", "==", eid).orderBy("dayNumber", asc)` | My check-in history | Composite index: `userId ASC, eventId ASC, dayNumber ASC` |
| `where("eventId", "==", eid).where("dayNumber", "==", n)` | All check-ins for a day | Composite index: `eventId ASC, dayNumber ASC` |

---

### 3.12 `craving_logs/{logId}`

> **Access:** Owner-only read/write. Auto-ID document.

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `userId` | String | ✅ | FK → `users` |
| `timestamp` | Timestamp | ✅ | When the craving occurred |
| `copingMethod` | String | ✅ | `"breathing"` / `"game"` / `"ai"` — which tool the user chose |
| `success` | Boolean | ✅ | Whether the craving was overcome |

#### Example Document

```json
{
  "userId": "uid123",
  "timestamp": "2025-03-04T11:23:00Z",
  "copingMethod": "breathing",
  "success": true
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `users/{userId}` | N:1 | `craving_logs.userId` → `users` |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `where("userId", "==", uid).orderBy("timestamp", desc).limit(50)` | Craving history | Composite index: `userId ASC, timestamp DESC` |
| `where("userId", "==", uid).where("success", "==", false)` | Failed cravings (insights) | Composite index: `userId ASC, success ASC, timestamp DESC` |
| `where("userId", "==", uid).where("copingMethod", "==", "breathing")` | Method analytics | Composite index: `userId ASC, copingMethod ASC` |
| Aggregation (client-side) | Daily/weekly stats | No special index — use `timestamp` range queries |

---

### 3.13 `subscriptions/{userId}`

> **Access:** Owner read-only; server-side (Cloud Function) write. Document ID = same UID as `users`.

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `active` | Boolean | ✅ | Whether the subscription is currently active |
| `plan` | String | ✅ | `"support_me"` — the only plan in v1 |
| `expiresAt` | Timestamp | ✅ | Far-future timestamp for lifetime-style subscription; set to year 2099 for practical purposes |
| `purchaseToken` | String | ✅ | Platform purchase token (Google Play / App Store) for server-side validation |

#### Example Document

```json
{
  "active": true,
  "plan": "support_me",
  "expiresAt": "2099-12-31T23:59:59Z",
  "purchaseToken": "google_play_purchase_token_abc123..."
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `users/{userId}` | 1:1 | Same document ID |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `doc(userId).get()` | Check subscription status | — (single doc) |

---

### 3.14 `coach_chats/{chatId}` — `users/{userId}/coach_chats/`

> **Access:** Owner-only read/write. Subcollection under `users/{userId}`. Auto-ID documents.

#### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| `role` | String | ✅ | `"user"` or `"assistant"` — who authored the message |
| `content` | String | ✅ | Message text (user input or AI response) |
| `timestamp` | Timestamp | ✅ | Server timestamp |

#### Example Document

```json
{
  "role": "user",
  "content": "I'm really struggling today. The cravings won't stop.",
  "timestamp": "2025-03-04T18:30:00Z"
}
```

```json
{
  "role": "assistant",
  "content": "I hear you — day 48 can be tough. Let's try a 4-7-8 breathing exercise together. Breathe in for 4 seconds…",
  "timestamp": "2025-03-04T18:30:05Z"
}
```

#### Relationships

| Related Collection | Relationship | Mechanism |
|---|---|---|
| `users/{userId}` | N:1 (parent) | Subcollection path |

#### Query Patterns

| Query | Purpose | Index |
|---|---|---|
| `orderBy("timestamp", asc).limitToLast(50)` | Load conversation history | Composite index: `timestamp ASC` |
| `orderBy("timestamp", desc).limit(1)` | Get latest message (context) | Composite index: `timestamp DESC` |

---

## 4. Index Requirements

### 4.1 Single-Field Indexes (Auto-Managed)

Firestore automatically creates single-field indexes for all fields. The following fields benefit from **disabling** auto-indexing to reduce write costs (they are never queried independently):

| Collection | Field | Action |
|---|---|---|
| `stories` | `likedBy` | Disable auto-index (array-contains handled by config) |
| `chats` | `typing` | Disable auto-index (map, never queried) |
| `events` | `prizes` | Disable auto-index (map, never queried) |

### 4.2 Composite Indexes (Must Configure)

| # | Collection | Fields | Direction | Purpose |
|---|---|---|---|---|
| 1 | `publicProfiles` | `xp` | DESC | XP leaderboard |
| 2 | `publicProfiles` | `daysSmokeFree` | DESC | Smoke-free leaderboard |
| 3 | `stories` | `createdAt` | DESC | Main feed |
| 4 | `stories` | `userId` ASC, `createdAt` DESC | — | User's stories |
| 5 | `stories` | `lifeChanges` ASC, `createdAt` DESC | — | Tag filter feed |
| 6 | `replies` | `parentReplyId` ASC, `createdAt` ASC | — | Threaded replies |
| 7 | `friendRequests` | `toUserId` ASC, `status` ASC, `timestamp` DESC | — | Incoming requests |
| 8 | `friendRequests` | `fromUserId` ASC, `status` ASC, `timestamp` DESC | — | Outgoing requests |
| 9 | `friendRequests` | `fromUserId` ASC, `toUserId` ASC | — | Check existing request |
| 10 | `chats` | `participants` ASC, `lastUpdated` DESC | — | Chat list |
| 11 | `messages` | `read` ASC, `senderId` ASC | — | Unread count |
| 12 | `eventParticipants` | `eventId` ASC, `rank` ASC | — | Event leaderboard |
| 13 | `eventParticipants` | `userId` ASC, `joinedAt` DESC | — | User's event history |
| 14 | `eventCheckins` | `status` ASC, `submittedAt` ASC | — | Review queue |
| 15 | `eventCheckins` | `userId` ASC, `eventId` ASC, `dayNumber` ASC | — | My check-in history |
| 16 | `eventCheckins` | `eventId` ASC, `dayNumber` ASC | — | Day-level check-ins |
| 17 | `craving_logs` | `userId` ASC, `timestamp` DESC | — | Craving history |
| 18 | `craving_logs` | `userId` ASC, `success` ASC, `timestamp` DESC | — | Failed cravings |
| 19 | `craving_logs` | `userId` ASC, `copingMethod` ASC | — | Method analytics |
| 20 | `coach_chats` | `timestamp` ASC | — | Conversation history |

### 4.3 Firestore Index Configuration (`indexes.json`)

```json
{
  "indexes": [
    { "collectionGroup": "publicProfiles", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "xp", "order": "DESCENDING" }] },
    { "collectionGroup": "publicProfiles", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "daysSmokeFree", "order": "DESCENDING" }] },
    { "collectionGroup": "stories", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "createdAt", "order": "DESCENDING" }] },
    { "collectionGroup": "stories", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "userId", "order": "ASCENDING" }, { "fieldPath": "createdAt", "order": "DESCENDING" }] },
    { "collectionGroup": "stories", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "lifeChanges", "order": "ASCENDING" }, { "fieldPath": "createdAt", "order": "DESCENDING" }] },
    { "collectionGroup": "replies", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "parentReplyId", "order": "ASCENDING" }, { "fieldPath": "createdAt", "order": "ASCENDING" }] },
    { "collectionGroup": "friendRequests", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "toUserId", "order": "ASCENDING" }, { "fieldPath": "status", "order": "ASCENDING" }, { "fieldPath": "timestamp", "order": "DESCENDING" }] },
    { "collectionGroup": "friendRequests", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "fromUserId", "order": "ASCENDING" }, { "fieldPath": "status", "order": "ASCENDING" }, { "fieldPath": "timestamp", "order": "DESCENDING" }] },
    { "collectionGroup": "friendRequests", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "fromUserId", "order": "ASCENDING" }, { "fieldPath": "toUserId", "order": "ASCENDING" }] },
    { "collectionGroup": "chats", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "participants", "order": "ASCENDING" }, { "fieldPath": "lastUpdated", "order": "DESCENDING" }] },
    { "collectionGroup": "messages", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "read", "order": "ASCENDING" }, { "fieldPath": "senderId", "order": "ASCENDING" }] },
    { "collectionGroup": "eventParticipants", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "eventId", "order": "ASCENDING" }, { "fieldPath": "rank", "order": "ASCENDING" }] },
    { "collectionGroup": "eventParticipants", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "userId", "order": "ASCENDING" }, { "fieldPath": "joinedAt", "order": "DESCENDING" }] },
    { "collectionGroup": "eventCheckins", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "status", "order": "ASCENDING" }, { "fieldPath": "submittedAt", "order": "ASCENDING" }] },
    { "collectionGroup": "eventCheckins", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "userId", "order": "ASCENDING" }, { "fieldPath": "eventId", "order": "ASCENDING" }, { "fieldPath": "dayNumber", "order": "ASCENDING" }] },
    { "collectionGroup": "eventCheckins", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "eventId", "order": "ASCENDING" }, { "fieldPath": "dayNumber", "order": "ASCENDING" }] },
    { "collectionGroup": "craving_logs", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "userId", "order": "ASCENDING" }, { "fieldPath": "timestamp", "order": "DESCENDING" }] },
    { "collectionGroup": "craving_logs", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "userId", "order": "ASCENDING" }, { "fieldPath": "success", "order": "ASCENDING" }, { "fieldPath": "timestamp", "order": "DESCENDING" }] },
    { "collectionGroup": "craving_logs", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "userId", "order": "ASCENDING" }, { "fieldPath": "copingMethod", "order": "ASCENDING" }] },
    { "collectionGroup": "coach_chats", "queryScope": "COLLECTION", "fields": [{ "fieldPath": "timestamp", "order": "ASCENDING" }] }
  ],
  "fieldOverrides": [
    { "collectionGroup": "stories", "fieldPath": "likedBy", "indexes": [] },
    { "collectionGroup": "chats", "fieldPath": "typing", "indexes": [] },
    { "collectionGroup": "events", "fieldPath": "prizes", "indexes": [] }
  ]
}
```

---

## 5. Data Integrity Rules

### 5.1 Denormalization Patterns

Breathy deliberately denormalizes data to optimize for read-heavy workloads (feed scrolling, profile viewing). The following table captures every denormalized field and its source of truth.

| Target Collection | Denormalized Field | Source of Truth | Update Trigger |
|---|---|---|---|
| `publicProfiles` | `nickname` | `users.nickname` | Cloud Function on user profile update |
| `publicProfiles` | `photoURL` | `users.photoURL` | Cloud Function on user profile update |
| `publicProfiles` | `xp` | `users.xp` | Cloud Function on XP change |
| `publicProfiles` | `location` | `users.location` | Cloud Function on user profile update |
| `publicProfiles` | `quitDate` | `users.quitDate` | Cloud Function on user profile update |
| `publicProfiles` | `daysSmokeFree` | Computed from `users.quitDate` | Scheduled Cloud Function (daily) |
| `stories` | `nickname` | `users.nickname` | Copied at story creation time (snapshot) |
| `stories` | `photoURL` | `users.photoURL` | Copied at story creation time (snapshot) |
| `stories` | `daysSmokeFree` | `publicProfiles.daysSmokeFree` | Snapshot at creation time |
| `replies` | `nickname` | `users.nickname` | Snapshot at reply creation time |
| `replies` | `photoURL` | `users.photoURL` | Snapshot at reply creation time |

> **Design decision:** Stories and replies snapshot user data at creation time. This means nickname/avatar changes do **not** retroactively update old stories. This is intentional — it preserves the context of "who I was when I posted this." `publicProfiles` is kept in sync because it represents the *current* user state.

### 5.2 Counter Consistency

| Counter | Location | Update Mechanism | Contention Risk |
|---|---|---|---|
| `stories.likes` | `stories/{storyId}.likes` | `FieldValue.increment(1)` on like; `FieldValue.increment(-1)` on unlike | **Medium** — viral stories. Use distributed counter pattern if > 1 000 writes/sec. |
| `stories.replyCount` | `stories/{storyId}.replyCount` | Cloud Function increments on reply creation; decrements on reply deletion | **Low** |
| `users.xp` | `users/{userId}.xp` | `FieldValue.increment(n)` from Cloud Function on achievement/craving event | **Low** — single-user writes |
| `users.coins` | `users/{userId}.coins` | `FieldValue.increment(n)` from Cloud Function on claim/reward | **Low** — single-user writes |
| `eventParticipants.currentStreak` | `eventParticipants/{id}.currentStreak` | Cloud Function on check-in approval | **Low** |
| `eventParticipants.totalApprovedDays` | `eventParticipants/{id}.totalApprovedDays` | Cloud Function on check-in approval | **Low** |
| `eventParticipants.rank` | `eventParticipants/{id}.rank` | Scheduled Cloud Function recalculates ranks | **Low** — batch update |

#### Distributed Counter Pattern (if needed for `likes`)

If a story goes viral (> 1 000 likes/sec), split the counter into shards:

```
stories/{storyId}/counters/shard_{0..N}
  └─ count: Int
```

Aggregate on read: `sum(shard_0..shard_N.count)`. Start with N = 10 shards; scale N proportionally to expected write rate.

### 5.3 Uniqueness Constraints

Firestore has no native unique constraints. The following patterns enforce uniqueness:

| Constraint | Enforcement Mechanism |
|---|---|
| One chat per user pair | Deterministic chat ID = `sorted([uid1, uid2]).join("_")` — upsert semantics |
| One participation per event per user | Deterministic doc ID = `{userId}_{eventId}` in `eventParticipants` |
| One like per user per story | `likedBy` array + security rule check; `givenLikes` array in `users` |
| One active subscription per user | Document ID = `userId` in `subscriptions` (single-doc upsert) |
| One friend request per pair | Security rule checks no existing `friendRequests` between same pair with `status == "pending"`; client also checks before creating |
| One friendship per pair | `friendships.userIds` + security rule preventing duplicate entry |

### 5.4 Referential Integrity

Firestore does not support foreign keys. The following rules must be enforced by Cloud Functions and security rules:

| Rule | Enforcement |
|---|---|
| A `story` cannot reference a non-existent `userId` | Cloud Function validates user exists before allowing write |
| A `reply` must have a valid `storyId` matching its subcollection parent | Enforced by path — `replies` are under `stories/{storyId}/replies/` |
| A `message` must have a `senderId` that is in the parent `chat.participants` | Security rule: `request.auth.uid in resource.data.participants` |
| `friendRequests.toUserId` must exist | Cloud Function validates |
| `eventCheckins` must reference valid `userId` + `eventId` | Cloud Function validates before creation |
| Deleting a user should cascade-delete or orphan their stories, cravings, etc. | Cloud Function on Auth user deletion triggers cleanup |

### 5.5 Data Validation Rules (Security Rules)

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // ─── users (private) ───
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      allow update: if request.auth.uid == userId
        && request.resource.data.email == resource.data.email  // email immutable via client
        && request.resource.data.xp >= resource.data.xp;      // XP never decrements via client
    }

    // ─── publicProfiles (public read, owner write) ───
    match /publicProfiles/{userId} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == userId;
    }

    // ─── stories (public read, owner write) ───
    match /stories/{storyId} {
      allow read: if true;
      allow create: if request.auth != null
        && request.resource.data.userId == request.auth.uid
        && request.resource.data.content.size() > 0
        && request.resource.data.content.size() <= 1000;
      allow update: if request.auth != null
        && resource.data.userId == request.auth.uid
        && request.resource.data.userId == request.auth.uid;  // cannot reassign
      allow delete: if request.auth != null && resource.data.userId == request.auth.uid;
    }

    // ─── replies (subcollection) ───
    match /stories/{storyId}/replies/{replyId} {
      allow read: if true;
      allow create: if request.auth != null
        && request.resource.data.userId == request.auth.uid
        && request.resource.data.content.size() > 0
        && request.resource.data.content.size() <= 500;
      allow update, delete: if request.auth != null
        && resource.data.userId == request.auth.uid;
    }

    // ─── friendRequests ───
    match /friendRequests/{requestId} {
      allow create: if request.auth != null
        && request.resource.data.fromUserId == request.auth.uid;
      allow read: if request.auth != null
        && (resource.data.fromUserId == request.auth.uid
            || resource.data.toUserId == request.auth.uid);
      allow update: if request.auth != null
        && resource.data.toUserId == request.auth.uid;  // only recipient can accept/reject
    }

    // ─── friendships ───
    match /friendships/{friendshipId} {
      allow read: if request.auth != null
        && request.auth.uid in resource.data.userIds;
      allow create: if false;  // only Cloud Functions can create
      allow delete: if false;  // only Cloud Functions can delete
    }

    // ─── chats ───
    match /chats/{chatId} {
      allow read, write: if request.auth != null
        && request.auth.uid in resource.data.participants;
      allow create: if request.auth != null
        && request.auth.uid in request.resource.data.participants;

      match /messages/{messageId} {
        allow read: if request.auth != null
          && request.auth.uid in get(/databases/$(database)/documents/chats/$(chatId)).data.participants;
        allow create: if request.auth != null
          && request.resource.data.senderId == request.auth.uid;
        allow update: if request.auth != null
          && request.auth.uid != resource.data.senderId  // only recipient can mark as read
          && request.resource.data.read == true;         // can only set read=true
      }
    }

    // ─── events ───
    match /events/{eventId} {
      allow read: if true;
      allow write: if false;  // admin-only via Cloud Functions
    }

    // ─── eventParticipants ───
    match /eventParticipants/{participantId} {
      allow read: if true;
      allow create: if request.auth != null
        && request.resource.data.userId == request.auth.uid;
      allow update: if false;  // Cloud Function only (streak, rank, etc.)
    }

    // ─── eventCheckins ───
    match /eventCheckins/{checkinId} {
      allow create: if request.auth != null
        && request.resource.data.userId == request.auth.uid;
      allow read: if request.auth != null
        && (resource.data.userId == request.auth.uid
            || isReviewer());  // custom helper for admin reviewers
      allow update: if isReviewer();  // only reviewers can change status
    }

    // ─── craving_logs (private) ───
    match /craving_logs/{logId} {
      allow read, write: if request.auth != null
        && resource.data.userId == request.auth.uid;
      allow create: if request.auth != null
        && request.resource.data.userId == request.auth.uid;
    }

    // ─── subscriptions ───
    match /subscriptions/{userId} {
      allow read: if request.auth != null && request.auth.uid == userId;
      allow write: if false;  // Cloud Function only (validated via purchase token)
    }

    // ─── coach_chats (private subcollection) ───
    match /users/{userId}/coach_chats/{chatId} {
      allow read, write: if request.auth != null
        && request.auth.uid == userId;
    }
  }
}
```

---

## 6. Cloud Function Triggers

### 6.1 Trigger Summary Table

| # | Trigger Type | Source | Function Name | Purpose |
|---|---|---|---|---|
| 1 | Firestore `onCreate` | `craving_logs` | `onCravingLogCreate` | Award XP for logging craving; update `users.xp` |
| 2 | Firestore `onCreate` | `stories/{storyId}/replies/{replyId}` | `onReplyCreate` | Increment `stories.replyCount` |
| 3 | Firestore `onDelete` | `stories/{storyId}/replies/{replyId}` | `onReplyDelete` | Decrement `stories.replyCount` |
| 4 | Firestore `onUpdate` | `users/{userId}` | `onUserProfileUpdate` | Sync denormalized fields to `publicProfiles` |
| 5 | Firestore `onUpdate` | `eventCheckins/{checkinId}` | `onCheckinStatusChange` | When status → approved: increment `eventParticipants.totalApprovedDays`, update `currentStreak`, check completion |
| 6 | Firestore `onCreate` | `friendRequests/{requestId}` | `onFriendRequestCreate` | Send push notification to recipient |
| 7 | Firestore `onUpdate` | `friendRequests/{requestId}` | `onFriendRequestAccept` | When status → accepted: create `friendships` doc, create `chats` doc, update both users' FCM notifications |
| 8 | Firestore `onCreate` | `chats/{chatId}/messages/{messageId}` | `onMessageCreate` | Update `chats.lastMessage` and `chats.lastUpdated`; send push notification to non-sender; clear typing indicator |
| 9 | Scheduled (daily) | — | `dailySmokeFreeUpdate` | Recalculate `publicProfiles.daysSmokeFree` for all users |
| 10 | Scheduled (hourly) | — | `recalculateEventRanks` | Recompute `eventParticipants.rank` for all active events |
| 11 | Scheduled (daily) | — | `checkEventCompletion` | Mark events as `active: false` when past `endDate`; award prizes to top-ranked participants |
| 12 | Auth `onDelete` | Firebase Auth | `onUserDelete` | Cascade-delete user data: `users`, `publicProfiles`, `craving_logs`, `stories`, `friendships`, `chats`, `subscriptions`, `coach_chats` |
| 13 | Firestore `onCreate` | `stories/{storyId}` | `onStoryCreate` | Award XP for sharing a story |

### 6.2 Detailed Trigger Specifications

#### 6.2.1 `onCravingLogCreate`

```
Trigger:  Firestore onCreate — craving_logs/{logId}
Logic:
  1. Read the new log document.
  2. If log.success == true:
       users/{log.userId}.xp  → FieldValue.increment(10)
       users/{log.userId}.coins → FieldValue.increment(5)
  3. If log.success == false:
       users/{log.userId}.xp  → FieldValue.increment(5)  // still reward effort
  4. Check and unlock achievements (e.g., "craving_master" after 50 successes).
  5. Update publicProfiles.xp to match users.xp.
Idempotency: Use log document ID as dedup key in a lightweight store (or check if XP already awarded).
```

#### 6.2.2 `onReplyCreate`

```
Trigger:  Firestore onCreate — stories/{storyId}/replies/{replyId}
Logic:
  1. stories/{storyId}.replyCount → FieldValue.increment(1)
  2. Award XP to reply author: users/{reply.userId}.xp → FieldValue.increment(5)
  3. Update publicProfiles.xp.
Idempotency: Check if reply document was already processed (event ID dedup).
```

#### 6.2.3 `onReplyDelete`

```
Trigger:  Firestore onDelete — stories/{storyId}/replies/{replyId}
Logic:
  1. stories/{storyId}.replyCount → FieldValue.increment(-1)
  2. Decrement XP from reply author: users/{reply.userId}.xp → FieldValue.increment(-5)
  3. Update publicProfiles.xp.
```

#### 6.2.4 `onUserProfileUpdate`

```
Trigger:  Firestore onUpdate — users/{userId}
Logic:
  1. Compare before/after for: nickname, photoURL, xp, location, quitDate.
  2. For each changed field, write the new value to publicProfiles/{userId}.
  3. If quitDate changed, recalculate daysSmokeFree and write to publicProfiles.
Idempotency: N/A — update trigger is inherently idempotent (last-write-wins).
```

#### 6.2.5 `onCheckinStatusChange`

```
Trigger:  Firestore onUpdate — eventCheckins/{checkinId}
Precondition: before.status != after.status AND after.status == "approved"
Logic:
  1. Construct participantDocId = "{after.userId}_{after.eventId}".
  2. Read event to get dailyRequired and calculate expected day count.
  3. Increment eventParticipants/{participantDocId}.totalApprovedDays by 1.
  4. Recalculate currentStreak:
       - Query previous day's check-in for same user+event.
       - If previous day approved → currentStreak += 1.
       - If previous day missing/rejected → currentStreak = 1.
  5. If totalApprovedDays == event duration in days:
       - Set completed = true, completionTimestamp = now.
       - Award completion XP and coins.
  6. If after.status == "rejected":
       - Reset currentStreak to 0.
Transactions: Use Firestore transaction for steps 3-5 to prevent race conditions.
```

#### 6.2.6 `onFriendRequestCreate`

```
Trigger:  Firestore onCreate — friendRequests/{requestId}
Logic:
  1. Read toUserId's FCM token from users/{toUserId}.fcmToken.
  2. Send push notification: "AlexSmokeFree wants to be your quit buddy!"
  3. If no FCM token, skip silently.
```

#### 6.2.7 `onFriendRequestAccept`

```
Trigger:  Firestore onUpdate — friendRequests/{requestId}
Precondition: before.status == "pending" AND after.status == "accepted"
Logic:
  1. Create friendships doc: { userIds: [sorted(after.fromUserId, after.toUserId)], createdAt: now }.
  2. Create chats doc (if not exists):
       - chatId = sorted([from, to]).join("_")
       - participants: [from, to]
       - lastMessage: ""
       - lastUpdated: now
       - typing: {}
  3. Send push notification to fromUserId: "QuitterJen accepted your friend request!"
Transactions: Use Firestore transaction to create both docs atomically.
```

#### 6.2.8 `onMessageCreate`

```
Trigger:  Firestore onCreate — chats/{chatId}/messages/{messageId}
Logic:
  1. Update parent chat document:
       - lastMessage = message.text (truncated to 100 chars)
       - lastUpdated = message.timestamp
       - Remove sender from typing map: delete typing[message.senderId]
  2. Find recipient ID from chats/{chatId}.participants (the one != senderId).
  3. Read recipient's FCM token.
  4. Send push notification with message preview.
  5. Set message.read = false (already set at creation).
```

#### 6.2.9 `dailySmokeFreeUpdate`

```
Trigger:  Scheduled — every day at 00:05 UTC
Logic:
  1. Query all publicProfiles.
  2. For each profile:
       - Calculate daysSmokeFree = floor((now - quitDate) / (24 * 60 * 60 * 1000))
       - If daysSmokeFree has changed, update the document.
  3. Batch writes (max 500 per batch; use multiple batches).
Optimization: Only update profiles where quitDate is within the last 365 days (active quitters).
              Older profiles are updated weekly instead.
```

#### 6.2.10 `recalculateEventRanks`

```
Trigger:  Scheduled — every hour
Logic:
  1. Query events where active == true.
  2. For each active event:
       - Query eventParticipants where eventId == event.id, orderBy totalApprovedDays DESC.
       - Assign rank (1, 2, 3, …) based on totalApprovedDays.
       - Tiebreak: earlier completionTimestamp gets higher rank.
       - Batch update rank field for all participants.
Transactions: Use batch writes (max 500 per batch).
```

#### 6.2.11 `checkEventCompletion`

```
Trigger:  Scheduled — every day at 01:00 UTC
Logic:
  1. Query events where active == true AND endDate < now.
  2. For each expired event:
       - Set active = false.
       - Query top-ranked eventParticipants.
       - Award prizes (coins) according to event.prizes map.
       - Update users.coins via FieldValue.increment.
       - Unlock "event_champion" achievement for top 3.
  3. Send push notifications to winners.
```

#### 6.2.12 `onUserDelete`

```
Trigger:  Firebase Auth onDelete
Logic:
  1. Delete users/{userId}.
  2. Delete publicProfiles/{userId}.
  3. Delete subscriptions/{userId}.
  4. Batch-delete craving_logs where userId == uid.
  5. Batch-delete stories where userId == uid; for each story, also delete its replies subcollection.
  6. Delete coach_chats subcollection under users/{userId}.
  7. Delete friendRequests where fromUserId or toUserId == uid.
  8. Delete friendships where userIds array-contains uid.
  9. Delete chats where participants array-contains uid; for each chat, delete messages subcollection.
  10. Delete eventParticipants with userId == uid.
  11. Delete eventCheckins with userId == uid.
  12. Remove uid from likedBy arrays in any stories they liked.
Note: This is a heavy operation. Implement with queued tasks (Cloud Tasks) to avoid timeouts.
      Each deletion step should be a separate Cloud Task for reliability.
```

#### 6.2.13 `onStoryCreate`

```
Trigger:  Firestore onCreate — stories/{storyId}
Logic:
  1. Award XP: users/{story.userId}.xp → FieldValue.increment(20)
  2. Award coins: users/{story.userId}.coins → FieldValue.increment(10)
  3. Update publicProfiles.xp.
  4. Check achievement unlocks (e.g., "first_story", "storyteller" after 10 stories).
Idempotency: Use story document ID as dedup key.
```

### 6.3 Trigger Dependency Graph

```
craving_logs CREATE ──→ onCravingLogCreate ──→ users.xp ↑, users.coins ↑
                                                       │
                                                       ▼
                                              onUserProfileUpdate ──→ publicProfiles sync

stories CREATE ──→ onStoryCreate ──→ users.xp ↑, users.coins ↑
                                        │
                                        ▼
                                  onUserProfileUpdate ──→ publicProfiles sync

replies CREATE ──→ onReplyCreate ──→ stories.replyCount ↑, users.xp ↑
                                        │
                                        ▼
                                  onUserProfileUpdate ──→ publicProfiles sync

replies DELETE ──→ onReplyDelete ──→ stories.replyCount ↓, users.xp ↓

eventCheckins UPDATE (approved) ──→ onCheckinStatusChange ──→ eventParticipants update
                                                                    │
                                                                    ▼
                                                          recalculateEventRanks (scheduled)

friendRequests CREATE ──→ onFriendRequestCreate ──→ Push notification
friendRequests UPDATE (accepted) ──→ onFriendRequestAccept ──→ friendships CREATE + chats CREATE

messages CREATE ──→ onMessageCreate ──→ chats.lastMessage/lastUpdated update + Push notification

[Daily 00:05 UTC] ──→ dailySmokeFreeUpdate ──→ publicProfiles.daysSmokeFree
[Hourly] ──→ recalculateEventRanks ──→ eventParticipants.rank
[Daily 01:00 UTC] ──→ checkEventCompletion ──→ events.active=false, coins awarded
Auth DELETE ──→ onUserDelete ──→ Cascade delete all user data
```

---

## 7. Appendix — Naming Conventions & ID Strategy

### 7.1 Document ID Strategies

| Collection | ID Strategy | Format | Rationale |
|---|---|---|---|
| `users` | Firebase Auth UID | `a1b2c3d4e5` | 1:1 with auth identity |
| `publicProfiles` | Same as `users` | `a1b2c3d4e5` | Easy join in client |
| `stories` | Auto-ID | `Oz1x2y3z4a5` | No natural key; high write rate |
| `replies` | Auto-ID | `Bc6d7e8f9g0` | No natural key |
| `friendRequests` | Auto-ID | `Hi1j2k3l4m5` | No natural key |
| `friendships` | Auto-ID | `Nn6o7p8q9r0` | No natural key (sorted UIDs not used to avoid rewrite on re-sort edge case) |
| `chats` | **Deterministic** | `uid123_uid456` | Prevents duplicate chats between same pair |
| `messages` | Auto-ID | `Ss1t2u3v4w5` | No natural key |
| `events` | **Named** | `pushup_challenge_2025` | Human-readable; admin-controlled |
| `eventParticipants` | **Deterministic** | `uid123_pushup_challenge_2025` | Prevents double-join |
| `eventCheckins` | Auto-ID | `Xx6y7z8a9b0` | Multiple check-ins possible (resubmission) |
| `craving_logs` | Auto-ID | `Cc1d2e3f4g5` | No natural key |
| `subscriptions` | Same as `users` | `a1b2c3d4e5` | 1:1 with user |
| `coach_chats` | Auto-ID | `Hh6i7j8k9l0` | Sequential messages |

### 7.2 Field Naming Conventions

| Convention | Example | Notes |
|---|---|---|
| camelCase for all field names | `daysSmokeFree`, `copingMethod` | Consistent with JS/TS codebase |
| `Id` suffix for reference fields | `userId`, `storyId`, `parentReplyId` | Not `userID` or `user_id` |
| `URL` suffix for URL fields | `photoURL`, `videoURL` | Standard abbreviation, capitalized |
| `At` suffix for timestamps | `createdAt`, `submittedAt` | Distinguishes from date-only fields |
| `Per` for rates | `cigarettesPerDay`, `pricePerPack` | Natural language readability |
| Array fields are plural nouns | `achievements`, `givenLikes`, `lifeChanges` | Indicates multiplicity |
| Boolean fields are adjectives/past participles | `active`, `completed`, `read`, `success` | Reads as natural language |

### 7.3 Timestamp Strategy

All `Timestamp` fields use **Firestore Server Timestamps** (`FieldValue.serverTimestamp()`) on creation to ensure clock consistency. The only exception is `quitDate`, which is set to midnight UTC of the user's chosen quit date.

### 7.4 Currency & Numbers

| Field | Unit | Precision |
|---|---|---|
| `pricePerPack` | Local currency (Double) | 2 decimal places |
| `xp` | Points (Int) | Integer only |
| `coins` | Virtual currency (Int) | Integer only |
| `cigarettesPerDay` | Count (Int) | Integer only |
| `cigarettesPerPack` | Count (Int) | Integer only |
| `daysSmokeFree` | Days (Int) | Integer only; `floor()` of time difference |

---

> **End of Document** — Breathy Firestore Data Model v1.0
