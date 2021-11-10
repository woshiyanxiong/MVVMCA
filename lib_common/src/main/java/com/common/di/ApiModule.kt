package com.common.di

import com.common.network.Net
import com.common.network.api.UserApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Created by yan_x
 * @date 2021/11/8/008 10:29
 * @description
 */
@Module
@InstallIn(SingletonComponent::class)
object  ApiModule {

    @Provides
    @JvmStatic
    fun providerRetrofit(): Retrofit {
        return Net.getRetrofit("https://www.wanandroid.com")
    }

    @Provides
    @JvmStatic
    fun providerUserService():UserApiService{
        return providerRetrofit().create(UserApiService::class.java)
    }
}