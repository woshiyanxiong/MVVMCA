package com.ca.home.di

import com.ca.home.service.HomeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Created by yan_x
 * @date 2023/3/11/011 15:20
 * @description
 */
@Module
@InstallIn(SingletonComponent::class)
class HomeApiModule {
    @Provides
    @Singleton
    fun providesHomeApiService(retrofit: Retrofit): HomeApiService {
        return retrofit.create(HomeApiService::class.java)
    }


}