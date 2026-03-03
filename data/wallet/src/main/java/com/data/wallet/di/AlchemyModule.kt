package com.data.wallet.di

import com.data.wallet.api.AlchemyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AlchemyModule {
    @Provides
    @Singleton
    fun providesAlchemyApi(retrofit: Retrofit): AlchemyApi {
        return retrofit.create(AlchemyApi::class.java)
    }
}
