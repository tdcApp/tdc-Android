# Release Notes — TDC (TIT Developers Club)

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
