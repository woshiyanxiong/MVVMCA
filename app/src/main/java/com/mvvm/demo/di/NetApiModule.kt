package com.mvvm.demo.di

import com.mvvm.net.network.Net
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit

/**
 * Created by yan_x
 * @date 2023/3/21/021 16:05
 * @description
 */
@InstallIn(SingletonComponent::class)
@Module
object ApiModule {
    @Provides
    fun providerRetrofit(): Retrofit {
        return Net.getRetrofit("https://www.wanandroid.com")
    }

}