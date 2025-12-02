package com.example.realtimepricetracker.di

import com.example.realtimepricetracker.data.remote.PricingService
import com.example.realtimepricetracker.data.repository.PriceRepositoryImpl
import com.example.realtimepricetracker.domain.repository.PriceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlin.random.Random


@Module
@InstallIn(SingletonComponent::class)
object PriceDataModule {

    @Provides
    @Singleton
    fun provideExternalScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideRandom(): Random = Random

    @Provides
    @Singleton
    fun provideTimeProvider(): () -> Long = System::currentTimeMillis

    @Provides
    @Singleton
    fun providePriceRepository(
        pricingService: PricingService,
        externalScope: CoroutineScope,
        random: Random,
        timeProvider: () -> Long
    ): PriceRepository {
        return PriceRepositoryImpl(
            pricingService = pricingService,
            externalScope = externalScope,
            random = random,
            timeProvider = timeProvider
        )
    }
}
