package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.Schedule
import kotlinx.coroutines.delay
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

interface ScheduleRepository {
    suspend fun getSchedules(): Result<List<Schedule>>
    // ─── Mentor ───
    suspend fun createSchedule(
        classId: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        subject: String,
        room: String?
    ): Result<Schedule>
    suspend fun updateSchedule(schedule: Schedule): Result<Schedule>
    suspend fun deleteSchedule(id: String): Result<Unit>
}

@Singleton
class ScheduleRepositoryImpl @Inject constructor() : ScheduleRepository {

    private val schedules = mutableListOf(
        Schedule("sch_001", "mentor_001", "cls_001", 1, "20:00", "21:00", "C++ Fundamentals", "Google Meet"),
        Schedule("sch_002", "mentor_002", "cls_002", 1, "21:00", "22:00", "Python DSA", "Discord"),
        Schedule("sch_003", "mentor_003", "cls_003", 2, "19:00", "20:00", "Web Dev (React)", "Google Meet"),
        Schedule("sch_004", "mentor_004", "cls_004", 2, "20:00", "21:30", "Android Dev (Kotlin)", "Discord"),
        Schedule("sch_005", "mentor_001", "cls_001", 3, "20:00", "21:00", "C++ Fundamentals", "Google Meet"),
        Schedule("sch_006", "mentor_005", "cls_005", 3, "21:00", "22:00", "Java OOP", "Zoom"),
        Schedule("sch_007", "mentor_002", "cls_002", 4, "21:00", "22:00", "Python DSA", "Discord"),
        Schedule("sch_008", "mentor_003", "cls_003", 5, "19:00", "20:00", "Web Dev (React)", "Google Meet"),
        Schedule("sch_009", "mentor_004", "cls_004", 5, "20:00", "21:30", "Android Dev (Kotlin)", "Discord"),
        Schedule("sch_010", "mentor_001", "cls_001", 6, "20:00", "21:00", "C++ Fundamentals", "Google Meet"),
    )

    override suspend fun getSchedules(): Result<List<Schedule>> {
        delay(400)
        // TODO: Replace with GET /schedules from NestJS backend
        return Result.success(schedules.toList())
    }

    override suspend fun createSchedule(
        classId: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        subject: String,
        room: String?
    ): Result<Schedule> = runCatching {
        delay(500)
        // TODO: Replace with POST /schedules to NestJS backend
        val now = Instant.now().toString()
        val schedule = Schedule(
            id = "sch_${System.currentTimeMillis()}",
            teacherUid = "mentor_001",
            classId = classId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            subject = subject,
            room = room,
            createdAt = now,
            updatedAt = now
        )
        schedules.add(schedule)
        schedule
    }

    override suspend fun updateSchedule(schedule: Schedule): Result<Schedule> = runCatching {
        delay(500)
        // TODO: Replace with PATCH /schedules/{id} to NestJS backend
        val index = schedules.indexOfFirst { it.id == schedule.id }
        if (index == -1) throw IllegalArgumentException("Schedule not found")
        val updated = schedule.copy(updatedAt = Instant.now().toString())
        schedules[index] = updated
        updated
    }

    override suspend fun deleteSchedule(id: String): Result<Unit> = runCatching {
        delay(400)
        // TODO: Replace with DELETE /schedules/{id} to NestJS backend
        if (!schedules.removeAll { it.id == id }) throw IllegalArgumentException("Schedule not found")
    }
}
