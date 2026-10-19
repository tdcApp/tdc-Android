package com.bagadbille.tdc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bagadbille.tdc.data.local.entity.ScheduleEntity

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM cached_schedules ORDER BY dayOfWeek ASC, startTime ASC")
    suspend fun getAllSchedules(): List<ScheduleEntity>

    @Query("SELECT * FROM cached_schedules WHERE id = :id LIMIT 1")
    suspend fun getScheduleById(id: String): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<ScheduleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity)

    @Query("DELETE FROM cached_schedules WHERE id = :id")
    suspend fun deleteSchedule(id: String)

    @Query("DELETE FROM cached_schedules")
    suspend fun clearAll()

    @Transaction
    suspend fun replaceSchedules(schedules: List<ScheduleEntity>) {
        clearAll()
        insertSchedules(schedules)
    }
}
