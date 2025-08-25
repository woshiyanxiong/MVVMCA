package com.data.wallet.di

import com.data.wallet.repo.IWalletRepository
import com.data.wallet.repo.impl.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal interface WalletDataModule {
    @Binds
    fun providesWalletRepository(walletRepository: WalletRepository): IWalletRepository
}