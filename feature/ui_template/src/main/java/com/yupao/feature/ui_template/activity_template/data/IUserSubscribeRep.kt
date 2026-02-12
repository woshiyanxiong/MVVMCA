package com.yupao.feature.ui_template.activity_template.data

import com.yupao.data.protocol.Resource
import kotlinx.coroutines.flow.Flow

/**
 * 用于获取和操作用户的职位订阅数据的仓库，这个接口类只是为了示例才放在UI层，实际开发中，这个接口应该放在对应业务的data模块里，
 *
 * 创建时间：2025/8/27
 *
 * @author fc
 */
internal interface IUserSubscribeRep {
    /**
     * 获取用户职位订阅数据
     */
    fun getUserSubscribeData(): Flow<Resource<UserSubscribeEntity>>

    /**
     * 更新期望城市
     */
    fun updateDesiredCity(cities: List<CityEntity>?): Flow<Resource<Unit>>

    /**
     * 更新全职期望职位
     */
    fun updateFullTimeDesiredOccupation(occupation: List<OccupationEntity>?): Flow<Resource<Unit>>

    /**
     * 更新兼职期望职位
     */
    fun updatePartTimeDesiredOccupation(occupation: List<OccupationEntity>?): Flow<Resource<Unit>>

    /**
     * 更新是否开启职位订阅
     */
    fun updateSubscribeSwitch(isSubscribe: Boolean): Flow<Resource<Unit>>
}