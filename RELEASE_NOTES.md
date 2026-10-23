# Release Notes — TDC (TIT Developers Club)

---

## 🚀 Version 2.9.0 (Build 10)
**Release Date:** October 23, 2026  
**Target SDK:** Android 14 (API 36) | **Min SDK:** Android 9.0 (API 28)

---

### 📱 Google Play Store — "What's New" (Ready to Copy)
```
What's new in v2.9.0:
• Enhanced Exam Lockdown: System back navigation is now securely locked during tests to prevent accidental exits, with clear strike warning alerts.
• Confidential Assessments: Teachers can now conduct private tests where individual scores remain confidential to students until officially published.
• Redesigned Submission Overview: Brand-new CyberCard results screen with glowing status indicators, verification seals, and performance tier statistics.
• Collapsible Quiz History: Easily expand and collapse your completed assessments to keep your dashboard clean.
• Prioritized Assessment Banner: The latest live quizzes and upcoming assessments are now featured directly on the home screen hero banner.
• Smart Notification Routing: Notification clicks navigate to respective screens without automatically launching active tests prematurely.
• Notch & Cutout Clearance: Lowered top proctoring header padding for seamless visibility on camera-notch and punch-hole displays.
• Performance optimizations and stability improvements.
```

---

### 🌟 Key Highlights & Major Features

#### 1. 🛡️ Exam Security & Navigation Lockdown
* **System Back Navigation Interception:** When inside an active quiz, the Android system back gesture and button are strictly intercepted and locked. Students cannot leave or navigate back accidentally, eliminating unintended quits.
* **Informative Strike Alert Dialog:** Pressing back triggers a prominent warning dialog explaining that back navigation is locked and that minimizing the app or leaving the proctored screen records an integrity strike (with automatic disqualification upon reaching 3 strikes).
* **Camera Cutout & Notch Clearance:** Adjusted proctoring header spacing and padding (`statusBarsPadding()`, `displayCutoutPadding()`, top offset) so timer, strikes, and exit controls never get obstructed by punch-hole selfie cameras.

#### 2. 🔒 Quiz Privacy & Result Visibility Controls
* **Public vs. Private Assessment Modes:** Teachers can specify `result_visibility` (`PUBLIC` or `PRIVATE`) when creating quizzes.
* **Zero Score Leaks for Students:** For private tests, scores, point breakdowns, and answer keys are masked across all submission responses and cached models, ensuring complete confidentiality until results are released.
* **Authoritative Confidential Evaluation Seal:** Private tests display a dedicated security lock emblem, integrity verification chips, and reassurance that responses were safely encrypted and submitted to faculty.

#### 3. 📂 Collapsible Quiz History Section
* **Interactive Section Toggle:** Added an interactive card for "Quiz History" with an item count badge and smooth chevron expand/collapse indicators.
* **Decluttered View:** Keeps the primary quiz list focused on active tests while offering one-tap access to review completed assessments and past attempts.

#### 4. ⚡ Dynamic Latest Assessment Hero Banner
* **Priority Sorting:** Quizzes are now sorted newest-first (`createdAt` descending) across both backend APIs and mobile clients.
* **Hero Banner Integration:** The home screen banner dynamically spotlights active attempts or the latest live test with an `⚡ LATEST ASSESSMENT` badge, question counts, and duration metadata.

#### 5. 🎨 Modernized Submission Overview (`QuizResultScreen`)
* **Responsive Status Emblems:** Features custom glowing icons for every state: Emerald Checkmark for evaluated tests, Amber Security Shield for confidential submissions, and Crimson Cancel for proctoring disqualifications.
* **Glassmorphic CyberCard Container:** Displays test metadata, conductor attribution, score ratio, percentage accuracy, and motivational performance tiers.
* **Verified Candidate Identity Card:** Displays candidate name, email, and "Attempt 1/1 Verified" badge.
* **Native Share Sheet:** Students taking public assessments can share their achievements with a single tap.

---

## 🚀 Version 2.7.0 (Build 8)
**Release Date:** October 22, 2026  
**Target SDK:** Android 14 (API 36) | **Min SDK:** Android 9.0 (API 28)

---

### 📱 Google Play Store — "What's New" (Ready to Copy)
```
What's new in v2.7.0:
• TDC Coding Quest Season 3: Brand-new dynamic hero banner with direct registration and live contest entry.
• Strict 3-Strike Exam Security: Monitored test environment with automated warning strikes on window focus loss, home press, or app exit.
• Quiz Resumption Lock: Exiting or minimizing an active test resumes from original remaining time with strike penalty — no restarting allowed.
• Automated 3-Strike Disqualification: 3rd strike immediately terminates, scores 0, and permanently locks the test.
• Quiz Timing & Scheduling: Configure quiz start time, entry window, and custom durations for Teacher & Admin roles.
• Detailed Student Timings: Track student entry time, exit time, and duration taken to identify fastest completions.
• Enhanced CSV Export: Export ranked results with entry time, exit time, and duration taken directly to Excel.
• Admin Special Quizzes: Flagship CyberCard styling with radiant gradient accents and official contest proctoring.
• Enhanced Quiz Interface: Modern option cards with circular letter badges and smooth selection states.
• Ultra-fast Quiz & Assignment loading: Optimized backend queries and eliminated redundant requests.
• Performance optimizations and security enhancements.
```

---

## 🚀 Version 2.6.0 (Build 7)
**Release Date:** October 21, 2026  
**Target SDK:** Android 14 (API 36) | **Min SDK:** Android 9.0 (API 28)

---

### 📱 Google Play Store — "What's New" (Ready to Copy)
```
What's new in v2.6.0:
• Major UI Changes: Modern glassmorphic dashboard, glowing floating navigation bar, ambient backdrop glow, and brand-new Play Store identity.
• Class Roster: Mentors and Admins can now view and search students in each class with phone, enrollment number, and year.
• Profile: View and edit your university enrollment number directly from your profile.
• Creator Badges: Clear creator attribution for assignments and quizzes (ADMIN or Teacher Name).
• Smoother Navigation: Pressing Back now seamlessly returns you to Home/Overview.
• Notification Permission: Notification requests now prompt only after logging in.
• Bug fixes & security improvements.
```

---

### 🌟 Key Highlights & Major Features

#### 1. ✨ Major UI/UX Overhaul & Modern Design System
* **Floating Frosted Glass Bottom Navigation:** Redesigned bottom navigation bar with specular glass highlights, translucent frosted backdrop, and an animated luminous pill capsule.
* **Ambient Glow Lighting:** Implemented theme-reactive ambient lighting across dashboard and auth views for a sleek, immersive visual experience.
* **Modernized Hero Card:** Completely refreshed the **"TDC Coding Quest 2026"** card with glowing dual-tone cyan/orange gradient borders, tag capsules, and clean typography.
* **Adaptive App Launcher Icon:** Replaced default placeholder assets with high-resolution, rounded adaptive launcher icons matching the official TDC branding for Google Play Store and modern Android launchers.
* **Dynamic Header Branding:** The top-bar "TDC" brand wordmark now dynamically renders in solid crisp white in Dark Theme and solid deep black in Light Theme for optimal contrast and readability.
* **Unified Terminology:** Renamed "Batches" to "Classes" across all user and mentor screens.

#### 2. 🎓 Class Roster & Student Search (Admin & Mentor Portal)
* **Dedicated Class Roster Screen:** Transformed the previous inline row view into a full-screen dedicated Class Roster view for each enrolled batch/class.
* **Instant Student Directory:** Mentors and Admins can view complete student information for every class member, including **Full Name**, **Email Address**, **Phone Number**, **Enrollment Number**, and **Academic Year**.
* **Live Search Bar:** Real-time search instantly filters students as you type across name, email, phone number, enrollment number, and year.
* **Direct Quick Actions:** Integrated one-tap direct calling (`tel:`) and direct emailing (`mailto:`) shortcuts for each student on the roster.

#### 3. 🆔 Enrollment Number in Profile & Registration
* **Database & API Integration:** Added `enrollment_number` support to the Neon PostgreSQL database schema and NestJS backend API.
* **Profile Display:** Student enrollment numbers are prominently displayed in the Profile details card.
* **Editable Profile:** Students can now update their university enrollment number directly via the **Edit Profile** dialog.

#### 4. 🏷️ Creator Attribution Badges (Assignments & Quizzes)
* **Transparent Creator Credits:** Added author badges to all assignment and quiz cards.
* **Smart Role Detection:** Displays **"By ADMIN"** in bold accent when created by an administrator, or **"By [Teacher Name]"** when published by a specific mentor/faculty member.
* **Consistent Across Views:** Available on student assignment and quiz lists, assignment detail screens, and mentor dashboards.

#### 5. 🔔 Thoughtful Permission Timing
* **No Premature Prompts:** Removed Android 13+ (`POST_NOTIFICATIONS`) permission dialog from cold app launch.
* **Prompt on Login:** The notification permission request now appears only after the user logs in and enters the main app, adhering to Android UX best practices.

#### 6. 🔙 Intuitive System Back Navigation
* **Predictable Hierarchy:** Pressing the Android system Back button or swipe gesture now navigates hierarchically instead of abruptly closing the app:
  * From sub-tabs (**Classes** or **Quiz**) $\rightarrow$ returns to **Overview** (Home).
  * From bottom navigation (**Assignments** or **Profile**) $\rightarrow$ returns to **Home**.
  * From class student rosters $\rightarrow$ returns to **Classes List**, then to **Overview**.
  * From **Past Assignments** $\rightarrow$ returns to **Ongoing Assignments**.

---

### 🛡️ Critical Bug Fixes & Stability

* **Cross-Account Session & Quiz State Leak:** Fixed a critical bug where logging out of an account and logging into another account showed stale submitted quiz status or cached data.
  * **Room Database Sanitization:** Logout now completely clears local Room tables (`quizzes`, `assignments`, `classes`, `users`).
  * **DataStore Session Flush:** Clears cached auth tokens, user email, and role preferences.
  * **Non-Cancellable Logout with FCM Timeout:** Ensured FCM token unregistration runs within a non-cancellable coroutine with a 4-second timeout so network delays never block local logout.
  * **User-Scoped Quiz Cache:** Local submitted quiz cache is now scoped by user `firebaseUid`.

---

### 🔧 Technical Specifications & Migration Details

| Component | Changes / Details |
| :--- | :--- |
| **Android App** | Version Code `7`, Version Name `2.6.0`, Target SDK `36` (Android 14/15) |
| **Backend API** | Deployed on Render: `https://tdc-backend-api-1.onrender.com/` |
| **Database** | Neon PostgreSQL: `users` table altered with `enrollment_number VARCHAR(100)` |
| **Authentication** | Firebase Auth REST / SDK integration + JWT session validation |
| **Navigation** | Jetpack Compose Navigation + `BackHandler` state interceptors |
