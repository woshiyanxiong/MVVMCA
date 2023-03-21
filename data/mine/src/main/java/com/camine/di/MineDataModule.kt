package com.camine.di

import com.camine.repository.UserRepository
import com.camine.repository.impl.RemotelyUserRepository
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
internal interface MineDataModule {
    @Binds
    fun providesMineRepository(remotelyMineRepository: RemotelyUserRepository): UserRepository
}