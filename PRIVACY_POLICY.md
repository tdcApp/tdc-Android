# Privacy Policy — TDC (Technocrats Developer Community)

**Effective Date:** September 1, 2026  
**Last Updated:** September 1, 2026  
**App Name:** TDC (Technocrats Developer Community)  
**Developer:** BagadBille  
**Package Name:** `com.bagadbille.tdc`  
**Contact Email:** aman2005mishra@gmail.com  

---

## 1. Introduction

Welcome to **TDC (Technocrats Developer Community)** ("TDC", "the App", "we", "us", or "our"). TDC is an android mobile application designed to help students manage their academic activities, including class schedules, announcements, quizzes, assignments, and notifications.

We are committed to protecting the privacy of all our users, including students and their families. This Privacy Policy explains what information we collect, how we use and protect it, who we share it with, and what choices you have regarding your data.

By downloading, installing, or using TDC, you agree to the collection and use of information in accordance with this Privacy Policy. If you do not agree to this policy, please do not use the App.

---

## 2. Information We Collect

### 2.1 Information You Provide Directly

When you register for an account or use TDC, we collect the following personal information that you voluntarily provide:

| Data Type | When Collected | Purpose |
|---|---|---|
| **Full Name** | Account registration | Display on your profile; identify you within the classroom |
| **Email Address** | Account registration and login | Authentication; account identification; account recovery |
| **Password** | Account registration and login | Authentication (transmitted securely; stored as a hash on the server, never in plaintext) |
| **Phone Number** (optional) | Profile settings | Displayed on your profile for teacher/peer contact; not required |
| **Class / Grade** (optional) | Profile settings | Associate you with your enrolled academic class |
| **Section** (optional) | Profile settings | Associate you with your class section |
| **Quiz Answers** | During quiz/test sessions | Record your assessment submissions for grading and academic evaluation |

### 2.2 Information Collected Automatically

When you use TDC, certain technical information may be collected automatically:

| Data Type | Purpose |
|---|---|
| **Authentication Token (JWT)** | Stored locally on your device to maintain your signed-in session. This token is transmitted to our servers with each authenticated API request to verify your identity. |
| **App Preferences** (e.g., dark/light theme) | Stored locally on your device to remember your display settings. This data is not transmitted to any server. |
| **Cached Quiz Data** | Quiz questions and pending answer submissions are temporarily stored in a local database on your device to allow uninterrupted test-taking even during network outages. This data is synced to the server when connectivity is restored and then cleared locally. |

### 2.3 Information We Do NOT Collect

TDC is designed with data minimization in mind. We want to be explicit about what we **do not** collect:

- ❌ **Location data** (GPS, network-based, or approximate)
- ❌ **Device identifiers** (Android Advertising ID, IMEI, hardware serial numbers)
- ❌ **Contacts, call logs, or SMS messages**
- ❌ **Camera or microphone recordings**
- ❌ **Browsing history or app usage analytics**
- ❌ **Financial or payment information**
- ❌ **Biometric data**
- ❌ **Files or photos from your device** (the assignment attachment feature is not yet active in this version)

The only Android permission the App requests is **`INTERNET`**, which is required to communicate with our backend servers for fetching class data, announcements, quiz content, and notifications.

---

## 3. How We Use Your Information

We use the information we collect solely for the following purposes:

1. **Account Creation & Authentication:** To create and manage your student account, verify your identity, and maintain your signed-in session.
2. **Providing Core App Features:** To display your enrolled classes, deliver announcements, serve quizzes and assessments, show assignments, and deliver notifications relevant to your academic activities.
3. **Profile Display:** To show your name, class, and section within the App (e.g., on your profile screen).
4. **Academic Assessment:** To record, submit, and grade quiz answers as part of your coursework.
5. **Offline Functionality:** To temporarily cache quiz content on your device so you can complete tests even if your internet connection is interrupted.
6. **User Preferences:** To remember your chosen display theme (dark or light mode) on your device.

**We do NOT use your data for:**
- ❌ Advertising, ad targeting, or ad personalization
- ❌ Selling or renting to third parties
- ❌ User profiling for non-educational purposes
- ❌ Marketing emails or promotional campaigns (unless you explicitly opt in to a separate service)

---

## 4. How We Share Your Information

We treat your data with strict confidentiality. Your personal information is shared only in the following limited circumstances:

### 4.1 With Your Educational Institution
Your name, class, section, quiz submissions, and assignment-related data may be accessible to authorized personnel at your educational institution (e.g., teachers and administrators) through the backend system that TDC connects to. This is necessary to deliver the educational services the App provides.

### 4.2 Service Providers
We may use trusted third-party service providers to operate our backend infrastructure (e.g., cloud hosting providers). These providers process data on our behalf and are contractually obligated to protect your information and use it only for the services they provide to us.

### 4.3 Legal Requirements
We may disclose your information if required to do so by law, regulation, legal process, or governmental request, or if we believe in good faith that disclosure is necessary to protect our rights, protect your safety or the safety of others, investigate fraud, or respond to a government request.

### 4.4 No Sale of Data
**We do not sell, trade, or rent your personal information to any third party, under any circumstances.**

---

## 5. Third-Party Services & SDKs

TDC uses the following third-party libraries and services. These operate within the App but do not independently collect or transmit your personal data to third parties:

| Library / Service | Purpose | Data Transmitted to Third Party? |
|---|---|---|
| **Retrofit + OkHttp** | Network communication with our own backend server | No — communicates only with our server |
| **Room Database** | Local on-device data caching (quiz questions, pending submissions) | No — data stays on your device |
| **Jetpack DataStore** | Local on-device preference storage (auth token, theme setting) | No — data stays on your device |
| **Coil** | Image loading for avatars and class materials | No personal data transmitted; loads images from URLs provided by our server |
| **Dagger Hilt** | In-app dependency injection framework | No — operates entirely on-device |
| **kotlinx.serialization** | JSON data parsing | No — operates entirely on-device |

**Note:** TDC does not integrate any third-party analytics SDKs (e.g., Google Analytics, Firebase Analytics), advertising SDKs, crash-reporting services, or social media SDKs in the current version. If this changes in a future update, this Privacy Policy will be updated accordingly and users will be notified.

---

## 6. Data Storage & Security

We take the security of your personal information seriously and implement appropriate technical and organizational measures to protect it:

### 6.1 Data in Transit
All network communication between the App and our backend servers is conducted over **HTTPS (TLS/SSL encryption)**, ensuring your data is encrypted during transmission.

### 6.2 Data on Your Device
- **Authentication tokens** are stored in Android Jetpack DataStore (encrypted app-private storage), accessible only to the TDC application.
- **Cached quiz data** is stored in a local Room (SQLite) database within the App's private storage sandbox, inaccessible to other apps.
- **Theme preferences** are stored in DataStore and contain no personal information.

### 6.3 Data on Our Servers
- Passwords are **hashed** (never stored in plaintext) on our server.
- Server access is restricted to authorized personnel only.
- We employ industry-standard security practices including access controls and regular security reviews.

### 6.4 Data Breach Notification
In the unlikely event of a data breach affecting your personal information, we will notify affected users and relevant authorities as required by applicable law within 72 hours of becoming aware of the breach.

---

## 7. Data Retention

We retain your personal information only for as long as is necessary to fulfill the purposes described in this Privacy Policy:

| Data Type | Retention Period |
|---|---|
| **Account data** (name, email, class, section) | Retained while your account is active. Deleted within 30 days of account deletion request. |
| **Quiz submissions and results** | Retained for the duration of the academic term or as required by your educational institution's policies. |
| **Authentication tokens** (on device) | Cleared immediately upon logout or account deletion. |
| **Cached quiz questions** (on device) | Cleared after successful submission sync or upon app data clearing. |
| **App preferences** (on device) | Cleared when you uninstall the App or clear app data. |

---

## 8. Your Rights & Choices

Depending on your location and applicable law, you may have the following rights regarding your personal data:

### 8.1 All Users
- **Access:** You can view your personal information at any time through the Profile screen in the App.
- **Correction:** You can update your name, phone number, class, and section through the App's profile settings.
- **Logout:** You can sign out at any time, which clears your authentication token from the device.
- **Deletion:** You may request complete deletion of your account and all associated data by contacting us at **privacy@bagadbille.com**. We will process deletion requests within 30 days.

### 8.2 European Economic Area (EEA) / UK Users — GDPR Rights
If you are located in the EEA or UK, you have additional rights under the General Data Protection Regulation (GDPR):
- **Right of Access** — Request a copy of all personal data we hold about you.
- **Right to Rectification** — Request correction of inaccurate or incomplete data.
- **Right to Erasure ("Right to be Forgotten")** — Request deletion of your personal data.
- **Right to Restriction of Processing** — Request that we limit how we use your data.
- **Right to Data Portability** — Request your data in a structured, machine-readable format.
- **Right to Object** — Object to processing of your data for certain purposes.
- **Right to Withdraw Consent** — Where processing is based on consent, withdraw it at any time.

**Legal Basis for Processing (GDPR):**
- **Contract Performance:** Processing your account data and quiz submissions is necessary to provide you with the App's educational services.
- **Legitimate Interest:** Maintaining application security, preventing fraud, and improving the App.
- **Consent:** Where required by law, we obtain your consent before processing (e.g., optional profile fields).

To exercise any of these rights, contact us at **privacy@bagadbille.com**.

### 8.3 California Users — CCPA Rights
If you are a California resident, the California Consumer Privacy Act (CCPA) grants you:
- The right to know what personal information we collect and how it is used.
- The right to request deletion of your personal information.
- The right to opt out of the sale of personal information. **We do not sell your personal information.**
- The right to non-discrimination for exercising your CCPA rights.

---

## 9. Children's Privacy

### 9.1 Age Requirement
TDC is designed for use by students in educational settings. **The App is intended for users aged 13 and older.** We do not knowingly collect personal information from children under the age of 13 without verifiable parental consent.

### 9.2 COPPA Compliance (United States)
In compliance with the Children's Online Privacy Protection Act (COPPA):
- If we become aware that we have collected personal information from a child under 13 without parental consent, we will take immediate steps to delete that information from our servers.
- Parents or guardians who believe their child under 13 has provided us with personal information may contact us at **privacy@bagadbille.com** to request review and deletion of that data.

### 9.3 Parental Rights
Parents and guardians have the right to:
- Review the personal information we have collected from their child.
- Request deletion of their child's personal information.
- Refuse to permit any further collection of their child's information.
- Contact us at any time regarding their child's privacy.

### 9.4 Educational Institution Authorization
Where TDC is deployed by a school or educational institution, the institution may act as an agent of the parent for purposes of providing consent for the collection of student information, in accordance with applicable educational privacy laws (e.g., FERPA in the United States).

---

## 10. Data Safety Declaration (Google Play)

In accordance with Google Play's Data Safety requirements, the following is a summary of our data practices for the Play Store listing:

### Data Collected

| Data Category | Data Type | Collected? | Shared with Third Parties? | Purpose |
|---|---|---|---|---|
| **Personal Info** | Name | Yes | No | Account management, profile display |
| **Personal Info** | Email address | Yes | No | Authentication, account identification |
| **Personal Info** | Phone number | Optional | No | Profile contact information |
| **Account Info** | Password | Yes (hashed) | No | Authentication |
| **Education Info** | Class/Grade, Section | Optional | No | Academic group association |
| **App Activity** | Quiz responses | Yes | No | Academic assessment and grading |

### Data NOT Collected

| Data Category | Collected? |
|---|---|
| Location (precise or approximate) | ❌ No |
| Device or other identifiers | ❌ No |
| Financial info | ❌ No |
| Photos or videos | ❌ No |
| Audio | ❌ No |
| Files and docs | ❌ No |
| Contacts | ❌ No |
| Calendar | ❌ No |
| Web browsing history | ❌ No |
| App usage analytics | ❌ No |
| Diagnostics / Crash logs | ❌ No |

### Security Practices
- ✅ Data is encrypted in transit (HTTPS/TLS)
- ✅ Users can request data deletion
- ✅ Data is stored in secure, access-controlled environments

---

## 11. Changes to This Privacy Policy

We may update this Privacy Policy from time to time to reflect changes in our practices, technology, legal requirements, or other factors. When we make material changes:

1. We will update the **"Last Updated"** date at the top of this policy.
2. We will notify users through an in-app notification or announcement.
3. Continued use of the App after the effective date of the revised policy constitutes acceptance of the changes.

We encourage you to review this Privacy Policy periodically for any updates.

---

## 12. Contact Us

If you have any questions, concerns, or requests regarding this Privacy Policy, your personal data, or our data practices, please contact us:

- **Email:** privacy@bagadbille.com
- **Developer Name:** BagadBille
- **Subject Line:** "TDC Privacy Inquiry"

We will respond to all privacy-related inquiries within **30 days**.

---

## 13. Governing Law

This Privacy Policy is governed by and construed in accordance with the laws of India. For users in other jurisdictions, local privacy laws (including GDPR for EEA/UK users and CCPA for California users) will apply to the extent they provide additional protections.

---

*This privacy policy was last reviewed and updated on September 1, 2026.*
