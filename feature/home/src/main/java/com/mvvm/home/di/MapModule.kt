package com.mvvm.home.di



import com.mvvm.home.api.MapNavigation
import com.mvvm.home.widget.MapBuilderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Created by yan_x
 * @date 2021/11/18/018 11:04
 * @description
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MapModule {
    @Binds
    abstract fun providesMap(impl: MapBuilderImpl): MapNavigation
}