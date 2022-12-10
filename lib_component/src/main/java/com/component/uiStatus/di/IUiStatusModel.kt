package com.component.uiStatus.di

import com.component.uiStatus.IUiLoadStatus
import com.component.uiStatus.impl.IUiLoadStatusImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

/**
 * Created by yan_x
 * @date 2022/12/9/009 17:13
 * @description
 */
@Module
@InstallIn(ActivityComponent::class)
abstract class IUiStatusModel {
    @Binds
    @ActivityScoped
    abstract fun bindUiStatus(load: IUiLoadStatusImpl): IUiLoadStatus
}