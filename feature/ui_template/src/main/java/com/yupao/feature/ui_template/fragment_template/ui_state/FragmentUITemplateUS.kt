package com.yupao.feature.ui_template.fragment_template.ui_state

import com.yupao.feature.ui_template.activity_template.data.UserSubscribeEntity
import com.yupao.kit.kotlin.nonNullStateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Fragment使用的US和Activity使用的US没有区别，这里是直接拷贝的ActivityUITemplateUS的代码。实际生成中就按ActivityUITemplateUS的写法来就行
 *
 * 创建时间：2025/9/28
 *
 * @author fc
 */
internal class FragmentUITemplateUS constructor(
    userSubscribeData: Flow<UserSubscribeEntity?>,
    voiceBroadcastSwitchData: Flow<Boolean?>,
    scope: CoroutineScope
) {
    /**
     * 期望城市
     */
    val city: StateFlow<String> = userSubscribeData.map { subscribeData ->
        subscribeData?.city?.mapNotNull { it.name }?.joinToString("、").orEmpty()
    }.nonNullStateIn(scope, "")

    /**
     * 是否展示全职职位
     */
    val isShowFullTimeOcc: StateFlow<Boolean> = TODO()

    /**
     * 全职职位
     */
    val fullTimeOcc: StateFlow<String> = TODO()

    /**
     * 是否展示兼职职位
     */
    val isShowPartTimeOcc: StateFlow<Boolean> = TODO()

    /**
     * 兼职职位
     */
    val partTimeOcc: StateFlow<String> = TODO()

    /**
     * 订阅开关文案
     */
    val subscribeSwitchText: StateFlow<String> = TODO()

    /**
     * 语音播报开关是否开启
     */
    val voiceBroadcastSwitch: StateFlow<Boolean> = voiceBroadcastSwitchData.map {
        it ?: false
    }.nonNullStateIn(scope, false)
}