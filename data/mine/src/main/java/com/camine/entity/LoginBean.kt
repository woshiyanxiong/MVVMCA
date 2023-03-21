package com.camine.entity

/**
 * Created by yan_x
 * @date 2021/11/10/010 18:20
 * @description
 */
data class LoginBeanData(
    val admin: Boolean,
    val chapterTops: List<Any>,
    val coinCount: Int,
    val collectIds: List<Any>,
    val email: String,
    val icon: String,
    val id: Int,
    val nickname: String,
    val password: String,
    val publicName: String,
    val token: String,
    val type: Int,
    val username: String
)