package com.bagadbille.tdc.di

import android.content.Context
import androidx.room.Room
import com.bagadbille.tdc.data.local.TdcDatabase
import com.bagadbille.tdc.data.local.dao.AnnouncementDao
import com.bagadbille.tdc.data.local.dao.AssignmentDao
import com.bagadbille.tdc.data.local.dao.ClassDao
import com.bagadbille.tdc.data.local.dao.QuizDao
import com.bagadbille.tdc.data.local.dao.ScheduleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTdcDatabase(@ApplicationContext context: Context): TdcDatabase =
        Room.databaseBuilder(context, TdcDatabase::class.java, "tdc_database")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideQuizDao(database: TdcDatabase): QuizDao = database.quizDao()

    @Provides
    fun provideScheduleDao(database: TdcDatabase): ScheduleDao = database.scheduleDao()

    @Provides
    fun provideAnnouncementDao(database: TdcDatabase): AnnouncementDao = database.announcementDao()

    @Provides
    fun provideAssignmentDao(database: TdcDatabase): AssignmentDao = database.assignmentDao()

    @Provides
    fun provideClassDao(database: TdcDatabase): ClassDao = database.classDao()
}
