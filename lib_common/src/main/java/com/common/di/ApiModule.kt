package com.common.di

import com.common.network.Net
import com.common.network.api.UserApiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import retrofit2.Retrofit
import javax.inject.Singleton

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
//
//    @Provides
//    @JvmStatic
//    fun providerHomeService():UserApiService{
//        return providerRetrofit().create(UserApiService::class.java)
//    }
}