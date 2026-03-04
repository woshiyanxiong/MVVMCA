package com.data.wallet.di

import com.data.wallet.repo.IAlchemyRepository
import com.data.wallet.repo.IEthRepository
import com.data.wallet.repo.INetworkRepository
import com.data.wallet.repo.IWalletRepository
import com.data.wallet.repo.impl.AlchemyRepository
import com.data.wallet.repo.impl.EthRepository
import com.data.wallet.repo.impl.NetworkRepository
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
    
    @Binds
    fun providesNetworkRepository(networkRepository: NetworkRepository): INetworkRepository

    @Binds
    fun providesAlchemyRepository(alchemyRepository: AlchemyRepository): IAlchemyRepository

    @Binds
    fun providesEthRepository(ethRepository: EthRepository): IEthRepository
}