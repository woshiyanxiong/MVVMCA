package com.data.wallet.di

import com.data.wallet.repo.IAccountRepository
import com.data.wallet.repo.impl.AccountRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal interface  AccountModule {
    
    @Binds
    fun bindAccountRepository(
        accountRepository: AccountRepository
    ): IAccountRepository
}