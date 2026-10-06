package com.bagadbille.tdc.data.mock

import com.bagadbille.tdc.data.model.UserProfile

/**
 * In-memory dev accounts used until the NestJS backend is wired.
 * Password = phone number (matches the real auth rule).
 *
 * TODO: Delete once AuthRepository/ProfileRepository call POST /auth/login and GET /profile/me.
 */
object MockUsers {

    const val DEV_PASSWORD = "1234567890"

    private data class Account(val password: String, val profile: UserProfile)

    private val accounts = mutableMapOf(
        // Mentor side
        "admin@gmail.com" to Account(
            password = DEV_PASSWORD,
            profile = UserProfile(
                id = "mentor_001",
                email = "admin@gmail.com",
                mobileNumber = DEV_PASSWORD,
                name = "TDC Mentor",
                role = "mentor",
                isProfileComplete = true
            )
        ),
        // Student side (profile already set up)
        "student@gmail.com" to Account(
            password = DEV_PASSWORD,
            profile = UserProfile(
                id = "user_001",
                email = "student@gmail.com",
                mobileNumber = DEV_PASSWORD,
                name = "Akash Sharma",
                role = "student",
                isProfileComplete = true,
                enrollmentNumber = "0111CS211045",
                year = "3rd Year",
                languageClassId = "cls_002",
                technologyClassId = "cls_004"
            )
        ),
        // Student who hasn't finished setup — use to test the Setup screen
        "newstudent@gmail.com" to Account(
            password = DEV_PASSWORD,
            profile = UserProfile(
                id = "user_002",
                email = "newstudent@gmail.com",
                mobileNumber = DEV_PASSWORD,
                name = null,
                role = "student",
                isProfileComplete = false,
                languageClassId = "cls_001",
                technologyClassId = "cls_003"
            )
        )
    ).apply {
        // Extra students so mentors have a roster and real submission counts
        listOf(
            rosterStudent("user_003", "Riya Patel", "riya@gmail.com", "0111CS211051", "2nd Year", "cls_001", "cls_003"),
            rosterStudent("user_004", "Arjun Mehta", "arjun@gmail.com", "0111CS211012", "3rd Year", "cls_001", "cls_004"),
            rosterStudent("user_005", "Sneha Gupta", "sneha@gmail.com", "0111CS211078", "2nd Year", "cls_002", "cls_003"),
            rosterStudent("user_006", "Rahul Verma", "rahul@gmail.com", "0111CS211033", "3rd Year", "cls_002", "cls_004"),
            rosterStudent("user_007", "Priya Singh", "priya@gmail.com", "0111CS211064", "1st Year", "cls_005", "cls_003"),
            rosterStudent("user_008", "Karan Joshi", "karan@gmail.com", "0111CS211029", "4th Year", "cls_001", "cls_004"),
            rosterStudent("user_009", "Ananya Rao", "ananya@gmail.com", "0111CS211090", "2nd Year", "cls_002", "cls_003"),
        ).forEach { put(it.profile.email, it) }
    }

    private fun rosterStudent(
        id: String, name: String, email: String, enrollment: String, year: String,
        languageClassId: String, technologyClassId: String
    ) = Account(
        password = DEV_PASSWORD,
        profile = UserProfile(
            id = id, email = email, mobileNumber = DEV_PASSWORD, name = name, role = "student",
            isProfileComplete = true, enrollmentNumber = enrollment, year = year,
            languageClassId = languageClassId, technologyClassId = technologyClassId
        )
    )

    fun findById(id: String): UserProfile? = accounts.values.firstOrNull { it.profile.id == id }?.profile

    fun findByFirebaseUid(uid: String): UserProfile? = findById(uid)

    fun exists(email: String): Boolean = accounts.containsKey(email.trim().lowercase())

    fun register(email: String, password: String, firebaseUid: String = "user_${System.currentTimeMillis()}"): UserProfile {
        val key = email.trim().lowercase()
        val existing = accounts[key]
        if (existing != null) return existing.profile
        val profile = UserProfile(
            id = firebaseUid,
            email = email.trim(),
            name = null,
            role = "student", // Role is always student; mentors are created by admin
            isProfileComplete = false
        )
        accounts[key] = Account(password = password, profile = profile)
        return profile
    }

    fun studentsInClass(classId: String): List<UserProfile> = accounts.values
        .map { it.profile }
        .filter { it.role == "student" && (it.languageClassId == classId || it.technologyClassId == classId) }
        .sortedBy { it.name ?: it.email }

    fun authenticate(email: String, password: String): UserProfile? {
        val account = accounts[email.trim().lowercase()] ?: return null
        return if (account.password == password.trim()) account.profile else null
    }

    fun find(email: String): UserProfile? = accounts[email.trim().lowercase()]?.profile

    fun update(email: String, transform: (UserProfile) -> UserProfile): UserProfile {
        val key = email.trim().lowercase()
        val account = accounts[key]
        val currentProfile = account?.profile ?: UserProfile(
            id = "user_${System.currentTimeMillis()}",
            email = email.trim(),
            name = null,
            role = "student",
            isProfileComplete = false
        )
        val updated = transform(currentProfile)
        accounts[key] = Account(password = account?.password ?: DEV_PASSWORD, profile = updated)
        return updated
    }
}
