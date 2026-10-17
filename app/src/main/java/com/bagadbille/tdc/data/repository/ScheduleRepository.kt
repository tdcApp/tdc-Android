package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.Schedule
import com.bagadbille.tdc.data.remote.api.ScheduleApi
import com.bagadbille.tdc.data.remote.dto.CreateScheduleRequest
import com.bagadbille.tdc.data.remote.dto.UpdateScheduleRequest
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
class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleApi: ScheduleApi
) : ScheduleRepository {

    override suspend fun getSchedules(): Result<List<Schedule>> = runCatching {
        scheduleApi.getSchedules().map { it.toDomain() }
    }

    override suspend fun createSchedule(
        classId: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        subject: String,
        room: String?
    ): Result<Schedule> = runCatching {
        val request = CreateScheduleRequest(
            classId = classId,
            dayOfWeek = dayOfWeek,
            startTime = startTime,
            endTime = endTime,
            subject = subject,
            room = room
        )
        scheduleApi.createSchedule(request).toDomain()
    }

    override suspend fun updateSchedule(schedule: Schedule): Result<Schedule> = runCatching {
        val request = UpdateScheduleRequest(
            classId = schedule.classId,
            dayOfWeek = schedule.dayOfWeek,
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            subject = schedule.subject,
            room = schedule.room
        )
        scheduleApi.updateSchedule(schedule.id, request).toDomain()
    }

    override suspend fun deleteSchedule(id: String): Result<Unit> = runCatching {
        scheduleApi.deleteSchedule(id)
    }
}
