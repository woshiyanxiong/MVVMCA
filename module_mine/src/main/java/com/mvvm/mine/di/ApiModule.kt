package com.mvvm.mine.di

import com.mvvm.mine.api.MineApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Created by yan_x
 * @date 2021/11/19/019 16:27
 * @description
 */
@Module
@InstallIn(SingletonComponent::class)
class ApiModule {
    @Provides
    @Singleton
    fun providesMineApiService(retrofit: Retrofit):MineApiService{
        return retrofit.create(MineApiService::class.java)
    }
}