package com.yupao.feature.ui_template.activity_template.data

import com.yupao.data.protocol.Resource
import kotlinx.coroutines.flow.Flow

/**
 * 推送设置仓库
 *
 * 创建时间：2025/9/17
 *
 * @author fc
 */
internal interface IPushSettingRep {
    /**
     * 查询是否开启了语音播报
     */
    fun queryIsEnableVoiceBroadcast(): Flow<Resource<Boolean>>

    /**
     * 更新语音播报开关
     */
    fun updateIsEnableVoiceBroadcast(isEnable: Boolean): Flow<Resource<Unit>>
}