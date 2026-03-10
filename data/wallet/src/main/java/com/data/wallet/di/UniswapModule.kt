package com.data.wallet.di

import com.data.wallet.api.UniswapTokenApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UniswapModule {
    @Provides
    @Singleton
    fun providesUniswapTokenApi(retrofit: Retrofit): UniswapTokenApi {
        return retrofit.create(UniswapTokenApi::class.java)
    }
}
