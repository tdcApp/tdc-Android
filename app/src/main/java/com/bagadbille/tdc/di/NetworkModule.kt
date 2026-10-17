package com.bagadbille.tdc.di

import com.bagadbille.tdc.BuildConfig
import com.bagadbille.tdc.data.local.DataStoreManager
import com.bagadbille.tdc.data.remote.api.AnnouncementApi
import com.bagadbille.tdc.data.remote.api.AssignmentApi
import com.bagadbille.tdc.data.remote.api.AuthApi
import com.bagadbille.tdc.data.remote.api.ClassApi
import com.bagadbille.tdc.data.remote.api.FirebaseAuthApi
import com.bagadbille.tdc.data.remote.api.FirebaseTokenApi
import com.bagadbille.tdc.data.remote.api.NotificationApi
import com.bagadbille.tdc.data.remote.api.ProfileApi
import com.bagadbille.tdc.data.remote.api.QuizApi
import com.bagadbille.tdc.data.remote.api.ScheduleApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
        encodeDefaults = true
    }

    @Provides
    @Singleton
    @Named("baseOkHttp")
    fun provideBaseOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                else HttpLoggingInterceptor.Level.NONE
            })
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

    @Provides
    @Singleton
    fun provideFirebaseAuthApi(
        @Named("baseOkHttp") baseClient: OkHttpClient,
        json: Json
    ): FirebaseAuthApi =
        Retrofit.Builder()
            .baseUrl("https://identitytoolkit.googleapis.com/")
            .client(baseClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FirebaseAuthApi::class.java)

    @Provides
    @Singleton
    fun provideFirebaseTokenApi(
        @Named("baseOkHttp") baseClient: OkHttpClient,
        json: Json
    ): FirebaseTokenApi =
        Retrofit.Builder()
            .baseUrl("https://securetoken.googleapis.com/")
            .client(baseClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FirebaseTokenApi::class.java)

    @Provides
    @Singleton
    fun provideAuthInterceptor(dataStoreManager: DataStoreManager): Interceptor =
        Interceptor { chain ->
            val token = runBlocking { dataStoreManager.authToken.firstOrNull() }
            val original = chain.request()
            val existingAuth = original.header("Authorization")
            val request = original.newBuilder().apply {
                if (existingAuth.isNullOrBlank() && !token.isNullOrBlank()) {
                    header("Authorization", "Bearer $token")
                }
                if (original.body != null && original.header("Content-Type").isNullOrBlank()) {
                    header("Content-Type", "application/json")
                }
            }.build()
            chain.proceed(request)
        }

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        dataStoreManager: DataStoreManager,
        firebaseTokenApi: FirebaseTokenApi
    ): Authenticator =
        Authenticator { _, response ->
            if (responseCount(response) >= 2) return@Authenticator null

            val refreshToken = runBlocking { dataStoreManager.refreshToken.firstOrNull() }
                ?: return@Authenticator null

            val refreshedToken = runBlocking {
                try {
                    val tokenResp = firebaseTokenApi.refreshToken(
                        apiKey = BuildConfig.FIREBASE_API_KEY,
                        refreshToken = refreshToken
                    )
                    val newIdToken = tokenResp.idToken ?: tokenResp.accessToken
                    if (!newIdToken.isNullOrBlank()) {
                        dataStoreManager.saveAuthToken(newIdToken)
                        tokenResp.refreshToken?.let { dataStoreManager.saveRefreshToken(it) }
                        newIdToken
                    } else {
                        null
                    }
                } catch (_: Exception) {
                    null
                }
            }

            if (refreshedToken != null) {
                response.request.newBuilder()
                    .header("Authorization", "Bearer $refreshedToken")
                    .build()
            } else {
                null
            }
        }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @Named("baseOkHttp") baseClient: OkHttpClient,
        authInterceptor: Interceptor,
        tokenAuthenticator: Authenticator
    ): OkHttpClient =
        baseClient.newBuilder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi = retrofit.create(ProfileApi::class.java)

    @Provides @Singleton
    fun provideAnnouncementApi(retrofit: Retrofit): AnnouncementApi = retrofit.create(AnnouncementApi::class.java)

    @Provides @Singleton
    fun provideClassApi(retrofit: Retrofit): ClassApi = retrofit.create(ClassApi::class.java)

    @Provides @Singleton
    fun provideQuizApi(retrofit: Retrofit): QuizApi = retrofit.create(QuizApi::class.java)

    @Provides @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Provides @Singleton
    fun provideScheduleApi(retrofit: Retrofit): ScheduleApi = retrofit.create(ScheduleApi::class.java)

    @Provides @Singleton
    fun provideAssignmentApi(retrofit: Retrofit): AssignmentApi = retrofit.create(AssignmentApi::class.java)
}
