package com.yupao.feature.ui_template.activity_template.data

import com.yupao.data.protocol.Resource
import kotlinx.coroutines.flow.Flow

/**
 *
 * 创建时间：2025/9/17
 *
 * @author fc
 */
internal class PushSettingRepImpl : IPushSettingRep {
    override fun queryIsEnableVoiceBroadcast(): Flow<Resource<Boolean>> {
        TODO("Not yet implemented")
    }

    override fun updateIsEnableVoiceBroadcast(isEnable: Boolean): Flow<Resource<Unit>> {
        TODO("Not yet implemented")
    }
}