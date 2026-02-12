package com.yupao.feature.ui_template.activity_template.ui_state

import com.yupao.feature.ui_template.activity_template.data.UserSubscribeEntity
import com.yupao.kit.kotlin.nonNullStateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * US是UIState的简称，提供给View使用的封装了UIState数据的类，既包含了UIState属性，也包含了业务数据转换成UIState的过程。
 * 也就是说这个类充当了UIState的角色，同时内部负责将业务数据转换成对应的视图属性
 * 1.通过构造器讲所需的业务数据注入进来，本类严禁修改业务数据
 * 2.将业务数据转换成对应的UIState
 * 3.需要使用scope的话使用构造器注入的scope
 *
 * 创建时间：2025/8/27
 *
 * @author fc
 */
internal class ActivityUITemplateUS constructor(
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