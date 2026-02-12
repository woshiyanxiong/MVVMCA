package com.yupao.feature.ui_template.activity_template.data

import com.yupao.data.protocol.Resource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * IUserSubscribeRep的实现类，这个类只是为了辅助UI层模板，在实际变成中仓库应放在对应业务的data模块中
 *
 * 类的作用的详细阐述
 *
 * 创建时间：2025/8/27
 *
 * @author fc
 */
internal class UserSubscribeRepImpl @Inject constructor() : IUserSubscribeRep{
    override fun getUserSubscribeData(): Flow<Resource<UserSubscribeEntity>> {
        TODO("Not yet implemented")
    }

    override fun updateDesiredCity(cities: List<CityEntity>?): Flow<Resource<Unit>> {
        TODO("Not yet implemented")
    }

    override fun updateFullTimeDesiredOccupation(occupation: List<OccupationEntity>?): Flow<Resource<Unit>> {
        TODO("Not yet implemented")
    }

    override fun updatePartTimeDesiredOccupation(occupation: List<OccupationEntity>?): Flow<Resource<Unit>> {
        TODO("Not yet implemented")
    }

    override fun updateSubscribeSwitch(isSubscribe: Boolean): Flow<Resource<Unit>> {
        TODO("Not yet implemented")
    }
}