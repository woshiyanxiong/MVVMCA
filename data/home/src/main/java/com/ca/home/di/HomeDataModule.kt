package com.ca.home.di

import com.ca.home.repository.HomeRepository
import com.ca.home.repository.impl.RemotelyHomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Created by yan_x
 * @date 2023/3/11/011 15:53
 * @description
 */
@Module
@InstallIn(SingletonComponent::class)
internal interface HomeDataModule {
    @Binds
    fun providesMineRepository(remotelyHomeRepository: RemotelyHomeRepository): HomeRepository
}