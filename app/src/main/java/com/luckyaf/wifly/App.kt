package com.luckyaf.wifly

import android.app.Application
import com.luckyaf.kommon.Kommon
import com.luckyaf.kommon.base.BaseApp
import com.luckyaf.kommon.manager.netstate.NetStateManager
import com.luckyaf.kommon.utils.Logger
import com.tencent.mmkv.MMKV

/**
 * 类描述：
 * @author Created by luckyAF on 2019-02-25
 *
 */
class App : Application(){


    companion object {
        val instance by lazy {
            this
        }
    }



    override fun onCreate() {
        super.onCreate()
        Kommon.init(this)
        MMKV.initialize(this)
        Logger.debug(BuildConfig.DEBUG)
        NetStateManager.registerNetworkStateReceiver(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        NetStateManager.unRegisterNetworkStateReceiver(this)
    }
    override fun onLowMemory() {
        super.onLowMemory()
        NetStateManager.unRegisterNetworkStateReceiver(this)
        android.os.Process.killProcess(android.os.Process.myPid())
    }
}