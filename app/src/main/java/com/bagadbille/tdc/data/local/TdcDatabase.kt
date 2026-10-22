package com.bagadbille.tdc.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bagadbille.tdc.data.local.dao.AnnouncementDao
import com.bagadbille.tdc.data.local.dao.AssignmentDao
import com.bagadbille.tdc.data.local.dao.ClassDao
import com.bagadbille.tdc.data.local.dao.QuizDao
import com.bagadbille.tdc.data.local.dao.ScheduleDao
import com.bagadbille.tdc.data.local.entity.AnnouncementEntity
import com.bagadbille.tdc.data.local.entity.AssignmentEntity
import com.bagadbille.tdc.data.local.entity.ClassEntity
import com.bagadbille.tdc.data.local.entity.PendingSubmissionEntity
import com.bagadbille.tdc.data.local.entity.QuizQuestionEntity
import com.bagadbille.tdc.data.local.entity.ScheduleEntity

@Database(
    entities = [
        QuizQuestionEntity::class,
        PendingSubmissionEntity::class,
        ScheduleEntity::class,
        AnnouncementEntity::class,
        AssignmentEntity::class,
        ClassEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class TdcDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun classDao(): ClassDao
}

