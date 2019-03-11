package com.luckyaf.wifly.ui

import android.os.Bundle
import android.view.View
import com.luckyaf.kommon.base.BaseFragment
import com.luckyaf.wifly.R

/**
 * 类描述：设置页面
 * @author Created by luckyAF on 2019-02-15
 *
 */
class SettingsFragment :BaseFragment(){

    companion object {
        fun newInstance() = SettingsFragment()
    }

    override fun getLayoutId() = R.layout.fragment_settings

    override fun initData(bundle: Bundle?) {
    }
    override fun initView(savedInstanceState: Bundle?, contentView: View) {
    }
    override fun start() {
    }
}