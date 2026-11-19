package com.bagadbille.tdc.data.repository

import com.bagadbille.tdc.data.model.HeroBanner
import com.bagadbille.tdc.data.remote.api.HeroBannerApi
import com.bagadbille.tdc.data.remote.dto.UpdateHeroBannerRequest
import javax.inject.Inject
import javax.inject.Singleton

interface HeroBannerRepository {
    suspend fun getHeroBanner(): Result<HeroBanner>
    suspend fun updateHeroBanner(request: UpdateHeroBannerRequest): Result<HeroBanner>
}

@Singleton
class HeroBannerRepositoryImpl @Inject constructor(
    private val heroBannerApi: HeroBannerApi
) : HeroBannerRepository {

    override suspend fun getHeroBanner(): Result<HeroBanner> = runCatching {
        heroBannerApi.getHeroBanner().toDomain()
    }

    override suspend fun updateHeroBanner(request: UpdateHeroBannerRequest): Result<HeroBanner> = runCatching {
        heroBannerApi.updateHeroBanner(request).toDomain()
    }
}
