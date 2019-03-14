package com.luckyaf.wifly

import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.view.View
import com.luckyaf.kommon.base.BaseActivity
import com.luckyaf.kommon.extension.DEBUG
import com.luckyaf.kommon.extension.replaceFragmentInActivity
import com.luckyaf.kommon.utils.Logger
import com.luckyaf.kommon.utils.PermissionUtil
import com.luckyaf.wifly.ui.FileFragment
import com.luckyaf.wifly.ui.ServerFragment
import com.luckyaf.wifly.ui.SettingsFragment
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    private lateinit var fileFragment: FileFragment
    private lateinit var serverFragment: ServerFragment
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var lastFragment:Fragment

    override fun getLayoutId() = R.layout.activity_main

    override fun initData(bundle: Bundle?) {
        serverFragment = ServerFragment.newInstance()
        fileFragment = FileFragment.newInstance()
        settingsFragment = SettingsFragment.newInstance()
    }

    override fun initView(savedInstanceState: Bundle?, contentView: View) {
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        replaceFragmentInActivity(fileFragment,R.id.contentFrame)
        lastFragment = fileFragment
    }

    override fun start() {
        PermissionUtil.externalStorage(object :PermissionUtil.RequestPermission{
            override fun onRequestPermissionFailure(permissions: List<String>) {
            }

            override fun onRequestPermissionFailureWithAskNeverAgain(permissions: List<String>) {
            }

            override fun onRequestPermissionSuccess() {

            }
        }, RxPermissions(instance))

        Logger.debug(true)


    }

    private fun replaceTo(new: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.hide(lastFragment)
        if(!new.isAdded){
            transaction.add(R.id.contentFrame,new)
        }
        transaction.show(new).commitNowAllowingStateLoss()
        lastFragment = new
    }

    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_server -> {
                    replaceTo(serverFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_file -> {
                    replaceTo(fileFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_settings -> {
                    replaceTo(settingsFragment)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }


}
