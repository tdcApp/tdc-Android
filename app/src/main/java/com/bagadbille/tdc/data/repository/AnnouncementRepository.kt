package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.Announcement
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface AnnouncementRepository {
    suspend fun getAnnouncements(): Result<List<Announcement>>
}

@Singleton
class AnnouncementRepositoryImpl @Inject constructor() : AnnouncementRepository {
    override suspend fun getAnnouncements(): Result<List<Announcement>> {
        delay(600)
        return Result.success(listOf(
            Announcement("ann_001", "Mid-Term Exams Schedule Released", "The mid-term examination schedule for all classes has been released. Please check the Classes tab for your specific dates and timings.", "Admin", null, "2026-08-20T09:00:00Z", "Exam"),
            Announcement("ann_002", "New Physics Study Material Available", "Chapter 5 - Wave Optics study material and practice problems have been uploaded.", "Dr. Sharma", null, "2026-08-19T14:30:00Z", "Study Material"),
            Announcement("ann_003", "Science Fair Registration Open", "Registrations for the Annual Science Fair 2026 are now open. Teams of 2-4 students can register. Last date: September 5th.", "Science Dept.", null, "2026-08-18T11:00:00Z", "Event"),
            Announcement("ann_004", "Holiday Notice - Independence Day", "School will remain closed on August 15th on account of Independence Day.", "Admin", null, "2026-08-13T08:00:00Z", "Holiday"),
        ))
    }
}
