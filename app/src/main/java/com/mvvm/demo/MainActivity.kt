package com.mvvm.demo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.component.base.BaseActivity
import com.mvvm.demo.databinding.ActivityMainBinding
import com.mvvm.home.ui.HomeFragment
import com.mvvm.mine.ui.MineFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
/**
 * Created by yan_x
 * @date 2021/11/5/005 15:59
 * @description
 */
@AndroidEntryPoint
class MainActivity: BaseActivity<ActivityMainBinding>(){
    @Inject
    lateinit var repository: UserRepository

    private val fragments= arrayListOf<Fragment>()

    override fun getLayout(): Int = R.layout.activity_main

    override fun initView() {
//        lifecycleScope.launch{
//            repository.login("yanxiong","123456")
//        }
        initTab()
    }

    private fun initTab(){
        fragments.add(HomeFragment())
        fragments.add(MineFragment())
        selectPosition(0)
        binding?.bvNavigation?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    selectPosition(0)
                }
                else -> {
                    selectPosition(1)
                }
            }
            true
        }
    }

    private fun selectPosition(position: Int) {
        switchFragment(fragments[position])
    }

    private fun switchFragment(targetFragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        detachFragments(transaction)
        if (!targetFragment.isAdded) {
            // 如果没有添加则添加后显示
            transaction.add(R.id.fragment, targetFragment)
                .show(targetFragment)
        } else {
            transaction.show(targetFragment)
        }
        transaction
            .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        try {
            transaction.commitAllowingStateLoss()

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun detachFragments(transaction: FragmentTransaction) {
        for (i in fragments.indices) {
            try {
                transaction.hide(fragments[i])
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



}