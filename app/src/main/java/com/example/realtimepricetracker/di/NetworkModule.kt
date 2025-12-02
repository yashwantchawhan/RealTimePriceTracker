package com.example.realtimepricetracker.di

import com.example.realtimepricetracker.data.remote.PricingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("pricing_ws_url")
    fun providePricingWebSocketUrl(): String =
        "wss://ws.postman-echo.com/raw"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient()

    @Provides
    @Singleton
    fun providePricingService(
        client: OkHttpClient,
        @Named("pricing_ws_url") url: String
    ): PricingService {
        return PricingService(
            client = client,
            url = url
        )
    }
}