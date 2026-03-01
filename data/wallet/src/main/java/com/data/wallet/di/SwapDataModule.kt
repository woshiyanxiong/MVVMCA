package com.data.wallet.di

import com.data.wallet.repo.ISwapRepository
import com.data.wallet.repo.impl.SwapRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 兑换数据模块
 * 提供兑换相关的依赖注入
 */
@Module
@InstallIn(SingletonComponent::class)
internal interface SwapDataModule {
    
    @Binds
    @Singleton
    fun providesSwapRepository(swapRepository: SwapRepository): ISwapRepository
}
