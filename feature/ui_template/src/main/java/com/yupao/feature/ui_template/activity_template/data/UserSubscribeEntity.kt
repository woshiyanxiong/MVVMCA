package com.yupao.feature.ui_template.activity_template.data

import androidx.annotation.Keep

/**
 * 用户订阅数据的业务数据实体类
 *
 * 该实体类定义在这只是为了方便模板类的编写，实际代码中，业务数据实体类需要定义在对应业务的model模块下，而不是像这样定义在UI层
 *
 * 创建时间：2025/8/27
 *
 * @author fc
 */
@Keep
internal data class UserSubscribeEntity(
    val city: List<CityEntity>?,
    val fullTimeOccupationEntity: List<OccupationEntity>?,
    val partTimeOccupationEntity: List<OccupationEntity>?,
    val isEnable: Boolean?,
)

@Keep
internal data class CityEntity(
    val id: String?,
    val name: String?,
)

@Keep
internal data class OccupationEntity(
    val id: String?,
    val name: String?,
)
