package com.bagadbille.tdc.di

import com.bagadbille.tdc.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository
    @Binds @Singleton abstract fun bindAnnouncementRepository(impl: AnnouncementRepositoryImpl): AnnouncementRepository
    @Binds @Singleton abstract fun bindClassRepository(impl: ClassRepositoryImpl): ClassRepository
    @Binds @Singleton abstract fun bindQuizRepository(impl: QuizRepositoryImpl): QuizRepository
    @Binds @Singleton abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
    @Binds @Singleton abstract fun bindAssignmentRepository(impl: AssignmentRepositoryImpl): AssignmentRepository
}
