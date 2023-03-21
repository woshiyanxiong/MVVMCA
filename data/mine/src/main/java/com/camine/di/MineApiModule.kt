package com.camine.di


import com.camine.service.MineApiService
import dagger.Binds
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
class MineApiModule {
    @Provides
    @Singleton
    fun providesMineApiService(retrofit: Retrofit): MineApiService {
        return retrofit.create(MineApiService::class.java)
    }


}