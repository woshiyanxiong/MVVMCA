package com.yupao.feature.ui_template.activity_template.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class UITemplateModule {
    @Binds
    abstract fun bindUserSubscribeRep(impl: UserSubscribeRepImpl): IUserSubscribeRep

    @Binds
    abstract fun bindResumeRep(impl: ResumeRepImpl): IResumeRep

    @Binds
    abstract fun bindPushSettingRep(impl: PushSettingRepImpl): IPushSettingRep
}