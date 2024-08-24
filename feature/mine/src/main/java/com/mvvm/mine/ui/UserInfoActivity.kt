package com.mvvm.mine.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route
import com.ca.router_annotation.OldRoute

/**
 * 类的作用的简要阐述
 *
 * 类的作用的详细阐述
 * <p>创建时间：2024/8/24/024</p>
 *
 * @author yanxiong
 */
@Route(path = "/mine/user_info")
@OldRoute(path = "/mine/new/user_info")
class UserInfoActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}