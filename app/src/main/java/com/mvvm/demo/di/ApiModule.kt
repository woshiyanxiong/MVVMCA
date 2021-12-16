package com.mvvm.demo.di

import com.common.network.Net
import com.mvvm.demo.api.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

/**
 * Created by yan_x
 * @date 2021/11/8/008 10:29
 * @description
 */
@InstallIn(SingletonComponent::class)
@Module
object ApiModule {
    @Provides
    fun providerRetrofit(): Retrofit {
        return Net.getRetrofit("https://www.wanandroid.com")
    }
    @Provides
    fun providerUserService(): UserApiService {
        return providerRetrofit().create(UserApiService::class.java)
    }

}