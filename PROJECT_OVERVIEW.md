# TDC Android — Technical Project Overview & Handover Document

## 📌 Project Summary

**TDC** is a modern Android application built for students to manage classes, announcements, notifications, and quizzes. It follows **MVVM Architecture** with **Jetpack Compose** and **Hilt Dependency Injection**.

---

## 🛠️ Tech Stack & Dependencies

| Component | Library / Framework | Purpose |
|---|---|---|
| **UI Framework** | Jetpack Compose (Material 3) | Declarative UI |
| **Navigation** | Navigation Compose (`2.8.5`) | Auth-gated & screen navigation |
| **DI** | Hilt (`2.52`) | Dependency injection |
| **Networking** | Retrofit (`2.11.0`) + OkHttp (`4.12.0`) | REST API client with Bearer token interceptor |
| **Serialization** | `kotlinx.serialization` (`1.7.3`) | JSON DTO parsing |
| **Local Cache** | Room Database (`2.6.1`) | Offline quiz questions & pending submissions |
| **Session** | DataStore Preferences (`1.1.1`) | JWT auth token & theme settings persistence |
| **Image Loading** | Coil (`2.7.0`) | Async image & avatar rendering |
| **Build Tools** | AGP (`8.8.2`), Kotlin (`2.1.0`), KSP (`2.1.0-1.0.29`) | Build & annotation processing |

---

## 📂 Project Architecture & Directory Structure

The project strictly follows MVVM layering (`View (Composable) → ViewModel → Repository → Remote/Local DataSource`):

```
app/src/main/java/com/bagadbille/tdc/
├── TdcApplication.kt             # Hilt @HiltAndroidApp entry point
├── MainActivity.kt               # @AndroidEntryPoint Activity with Edge-to-Edge
├── navigation/
│   ├── Routes.kt                 # Route definitions & BottomNavItem enum
│   └── NavGraph.kt               # Auth-gated navigation host
├── ui/
│   ├── auth/
│   │   ├── SplashScreen.kt       # Session token check on app launch
│   │   ├── LoginScreen.kt        # Email/password + Google sign-in UI
│   │   ├── SignupScreen.kt       # Registration form
│   │   └── AuthViewModel.kt      # Auth state management
│   ├── main/
│   │   └── MainScreen.kt         # Scaffold with Material 3 NavigationBar (Profile, Home, Notifications)
│   ├── home/
│   │   ├── HomeScreen.kt         # Top tabs: General, Classes, Quiz (HorizontalPager)
│   │   ├── general/              # Announcements feed screen & ViewModel
│   │   ├── classes/              # Enrolled classes list & detail view screen & ViewModel
│   │   └── quiz/                 # Quiz list, full test-taking screen, results screen & ViewModels
│   ├── profile/                  # User profile details, settings, and logout
│   ├── notifications/            # Read/unread notification feed
│   ├── theme/                    # Color.kt (Dark teal palette), Theme.kt, Type.kt
│   └── components/               # TdcButton, TdcTextField, LoadingScreen, ErrorScreen, EmptyStateScreen
├── data/
│   ├── repository/               # Auth, Profile, Announcement, Class, Quiz, Notification Repositories
│   ├── remote/
│   │   ├── api/                  # Retrofit interfaces (AuthApi, ProfileApi, QuizApi, etc.)
│   │   └── dto/                  # Serializable DTOs for server payload matching
│   ├── local/
│   │   ├── TdcDatabase.kt        # Room DB definition
│   │   ├── DataStoreManager.kt   # Preference DataStore wrapper
│   │   ├── entity/               # QuizQuestionEntity & PendingSubmissionEntity
│   │   └── dao/                  # QuizDao for local caching & offline queue
│   └── model/                    # Clean domain models (UserProfile, ClassInfo, Quiz, etc.)
└── di/
    ├── NetworkModule.kt          # Retrofit, OkHttp with Auth Interceptor, API providers
    ├── DatabaseModule.kt         # Room DB & DAO providers
    └── RepositoryModule.kt       # Interface-to-Implementation Hilt bindings
```

---

## 🔐 Auth Flow Strategy (Gate Pattern)

```
Splash Screen
    │
    ├── Token in DataStore? ──► MainScreen (3-Tab Shell)
    │
    └── No Token ───────────► LoginScreen / SignupScreen
                                   │
                                   ▼
                              AuthViewModel
                                   │ (Firebase Auth / Mock)
                                   ▼
                              POST /auth/verify
                                   │
                                   ▼
                              Store App JWT in DataStore ──► MainScreen
```

Auth acts as a **gate**, not a bottom tab. The 3-tab shell (`Profile`, `Home`, `Notifications`) is accessible only after a valid session token is established.

---

## 📝 Features & Screen Details

### 1. Home Tab Shell
- **General Tab**: Feed of school/class announcements with category badges, author names, and timestamps.
- **Classes Tab**: Enrolled class cards (subject, teacher, schedule, room). Tapping a class opens `ClassDetailScreen` displaying course information and downloadable materials (notes, video lectures, practice sets).
- **Quiz Tab**:
  - **Quiz List**: Sections for *Available*, *Upcoming*, and *Past* tests.
  - **Quiz Taking**: Full-screen experience with countdown timer, progress bar, single-choice and multi-select MCQs, answer state tracking, and auto-submit on timer expiration.
  - **Quiz Results**: Handles delayed scoring states (`PENDING` / *"Results Not Out Yet"* vs `RELEASED` breakdown).

### 2. Notifications Tab
- Read vs. unread visual indicators (teal dot + highlighted surface).
- Tapping a notification marks it as read.

### 3. Profile Tab
- Displays avatar initials, full name, email, phone number, class, and section.
- Settings section with Dark Theme toggle and Logout action.

### 4. Room Offline Quiz Engine
- `QuizQuestionEntity`: Caches quiz questions locally when a user opens a quiz.
- `PendingSubmissionEntity`: Queues user answers if network disconnects during a quiz, ready to sync when back online.

---

## 🚀 How to Run & Test

1. Open project in **Android Studio**.
2. Click **Sync Project with Gradle Files** (elephant icon).
3. Connect an Android device or launch an emulator (API 28+).
4. Click **Run ▶ (Shift + F10)**.

The app initially runs with **Mock Repositories** returning pre-populated test data. When the NestJS backend is live, update `BASE_URL` in `app/build.gradle.kts` and replace the mock implementations in `RepositoryModule.kt` with real API calls.
