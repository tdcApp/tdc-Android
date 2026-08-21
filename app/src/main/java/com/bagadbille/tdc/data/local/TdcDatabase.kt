package com.bagadbille.tdc.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bagadbille.tdc.data.local.dao.QuizDao
import com.bagadbille.tdc.data.local.entity.PendingSubmissionEntity
import com.bagadbille.tdc.data.local.entity.QuizQuestionEntity

@Database(
    entities = [QuizQuestionEntity::class, PendingSubmissionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TdcDatabase : RoomDatabase() {
    abstract fun quizDao(): QuizDao
}
