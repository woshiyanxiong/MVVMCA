package com.data.wallet.di

import com.data.wallet.api.CoinGeckoApi
import com.data.wallet.api.NetConfigApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoinGeckoModule {
    
    @Provides
    @Singleton
    fun provideCoinGeckoApi(retrofit: Retrofit): CoinGeckoApi {
        return retrofit.create(CoinGeckoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideConfigApi(retrofit: Retrofit): NetConfigApi {
        return retrofit.create(NetConfigApi::class.java)
    }
}