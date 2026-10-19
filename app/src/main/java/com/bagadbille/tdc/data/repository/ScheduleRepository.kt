package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.local.dao.ScheduleDao
import com.bagadbille.tdc.data.local.entity.ScheduleEntity
import com.bagadbille.tdc.data.model.Schedule
import com.bagadbille.tdc.data.remote.api.ScheduleApi
import com.bagadbille.tdc.data.remote.dto.CreateScheduleRequest
import com.bagadbille.tdc.data.remote.dto.UpdateScheduleRequest
import retrofit2.HttpException
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
    private val scheduleApi: ScheduleApi,
    private val scheduleDao: ScheduleDao
) : ScheduleRepository {

    override suspend fun getSchedules(): Result<List<Schedule>> = runCatching {
        try {
            val remote = scheduleApi.getSchedules().map { it.toDomain() }
            scheduleDao.replaceSchedules(remote.map { ScheduleEntity.fromDomain(it) })
            remote
        } catch (e: Exception) {
            val cached = scheduleDao.getAllSchedules()
            if (cached.isNotEmpty()) {
                cached.map { it.toDomain() }
            } else {
                throw e
            }
        }
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
        val created = scheduleApi.createSchedule(request).toDomain()
        scheduleDao.insertSchedule(ScheduleEntity.fromDomain(created))
        created
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
        val updated = scheduleApi.updateSchedule(schedule.id, request).toDomain()
        scheduleDao.insertSchedule(ScheduleEntity.fromDomain(updated))
        updated
    }

    override suspend fun deleteSchedule(id: String): Result<Unit> = runCatching {
        try {
            scheduleApi.deleteSchedule(id)
        } catch (e: HttpException) {
            if (e.code() != 404) throw e
        }
        scheduleDao.deleteSchedule(id)
    }
}
