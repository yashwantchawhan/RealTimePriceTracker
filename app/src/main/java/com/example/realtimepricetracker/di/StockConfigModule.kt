package com.example.realtimepricetracker.di

import com.example.realtimepricetracker.core.utils.STOCK_SYMBOLS
import com.example.realtimepricetracker.core.utils.StockConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StockConfigModule {
    @Provides
    @Singleton
    fun provideStockConfig() = StockConfig(STOCK_SYMBOLS)
}