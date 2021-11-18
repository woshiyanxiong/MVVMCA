package com.mvvm.home.di


import com.mvvm.home.api.HomeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Created by yan_x
 * @date 2021/11/11/011 9:14
 * @description
 */
@Module
@InstallIn(SingletonComponent::class)
class HomeApiModule {
    @Singleton
    @Provides
    fun providerHomeService(retrofit: Retrofit): HomeApiService {
        return retrofit.create(HomeApiService::class.java)
    }
}