package com.bagadbille.tdc.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bagadbille.tdc.data.model.Schedule

@Entity(tableName = "cached_schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val teacherUid: String,
    val classId: String,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val subject: String,
    val room: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
) {
    fun toDomain(): Schedule = Schedule(
        id = id,
        teacherUid = teacherUid,
        classId = classId,
        dayOfWeek = dayOfWeek,
        startTime = startTime,
        endTime = endTime,
        subject = subject,
        room = room,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(schedule: Schedule): ScheduleEntity = ScheduleEntity(
            id = schedule.id,
            teacherUid = schedule.teacherUid,
            classId = schedule.classId,
            dayOfWeek = schedule.dayOfWeek,
            startTime = schedule.startTime,
            endTime = schedule.endTime,
            subject = schedule.subject,
            room = schedule.room,
            createdAt = schedule.createdAt,
            updatedAt = schedule.updatedAt
        )
    }
}
