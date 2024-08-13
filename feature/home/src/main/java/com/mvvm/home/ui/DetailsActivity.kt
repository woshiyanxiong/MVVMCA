package com.mvvm.home.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.alibaba.android.arouter.facade.annotation.Route
import com.ca.router_annotation.OldRoute
import dagger.hilt.android.AndroidEntryPoint

/**
 * 类的作用的简要阐述
 *
 * 类的作用的详细阐述
 * <p>创建时间：2024/8/12/012</p>
 *
 * @author yanxiong
 */
@OldRoute(path = "/feature/home/details/old")
@Route(path = "/feature/home/details")
@AndroidEntryPoint
class DetailsActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}