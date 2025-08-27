package com.data.wallet.di

import com.data.wallet.api.NodeRealApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
class EtherscanModule {
    @Provides
    @Singleton
    fun providesHomeApiService(retrofit: Retrofit): NodeRealApi {
        return retrofit.create(NodeRealApi::class.java)
    }
}