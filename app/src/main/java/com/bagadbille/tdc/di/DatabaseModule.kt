package com.bagadbille.tdc.di

import android.content.Context
import androidx.room.Room
import com.bagadbille.tdc.data.local.TdcDatabase
import com.bagadbille.tdc.data.local.dao.QuizDao
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
}
