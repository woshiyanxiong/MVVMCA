package com.component.uiStatus.di

import com.component.uiStatus.IStatusView
import com.component.uiStatus.impl.IUiStatusResourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

/**
 * Created by yan_x
 * @date 2022/12/9/009 17:14
 * @description
 */
@Module
@InstallIn(ViewModelComponent::class)
abstract class IUiStatusResourceModel {
    @Binds
    @ViewModelScoped
    abstract fun bindUiStatusResource(resourceImpl: IUiStatusResourceImpl): IStatusView
}