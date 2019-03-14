package com.luckyaf.wifly.ui

import android.os.Bundle
import android.view.View
import com.luckyaf.kommon.base.BaseFragment
import com.luckyaf.kommon.constant.NetworkType
import com.luckyaf.kommon.extension.clickWithTrigger
import com.luckyaf.kommon.manager.AppExecutors
import com.luckyaf.kommon.manager.netstate.NetChangeObserver
import com.luckyaf.kommon.utils.NetworkUtils
import com.luckyaf.wifly.R
import com.luckyaf.wifly.constant.Constants
import com.luckyaf.wifly.service.WebService
import com.luckyaf.wifly.utils.QrCodeUtil
import kotlinx.android.synthetic.main.fragment_server.*
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import com.luckyaf.kommon.manager.netstate.NetStateManager
import com.luckyaf.kommon.widget.dialog.Alert


/**
 * 类描述： 服务器开启页面
 * @author Created by luckyAF on 2019-02-15
 *
 */
class ServerFragment : BaseFragment() {

    companion object {
        fun newInstance() = ServerFragment()
    }

    private var serverOn = false
    private var wifiOn = false

    private val mAppExecutors = AppExecutors()

    private var netChangeObserver: NetChangeObserver? = null

    override fun getLayoutId() = R.layout.fragment_server

    override fun initData(bundle: Bundle?) {
        // NetworkUtils.getIpAddressByWifi()
    }

    override fun initView(savedInstanceState: Bundle?, contentView: View) {
        netChangeObserver = object : NetChangeObserver {
            override fun onNetChanged(state: NetworkType) {
                initWifiState(state)
            }
        }
        NetStateManager.registerObserver(netChangeObserver)
        initWifiState(NetworkUtils.getNetworkType(mActivity))
        switchServer.clickWithTrigger {
            if(serverOn){
                closeServer()
            }else{
                openServer()
            }
        }


    }

    override fun start() {

    }

    private fun initWifiState(state: NetworkType){
        if (state == NetworkType.NETWORK_WIFI) {
            wifiOn = true
            val wifiName = NetworkUtils.getConnectWifiName(mActivity)
            txtServerState.text = "已连接至 $wifiName"
            imgServerState.setImageResource(R.drawable.shared_wifi_enable)
        } else {
            wifiOn = false
            txtServerState.text = "Wi-Fi 未连接"
            imgServerState.setImageResource(R.drawable.shared_wifi_shut_down)
            if(serverOn){
                closeServer()
            }
        }
    }

    override fun onDetach() {
        WebService.stop(mActivity)
        NetStateManager.removeRegisterObserver(netChangeObserver)
        super.onDetach()
    }



    private fun openServer(){
        if(!wifiOn){
            Alert.confirm(mActivity,"Wi-Fi 未连接","请先连接Wi-Fi")
            return
        }
        WebService.start(mActivity)
        serverOn = true
        switchServer.text = "关闭"
        txtAddressTip.visibility = View.VISIBLE
        txtServerAddress.visibility = View.VISIBLE
        imgCodeAddress.visibility = View.VISIBLE
        val host = NetworkUtils.getIpAddressByWifi(mActivity)
        val address = String.format(
            mActivity.getString(R.string.http_address),
            host,
            Constants.serverPort
        )
        txtServerAddress.text = address
        mAppExecutors.runOnIoThread {
            val bitmap = QrCodeUtil.createBarcode(address, 80, 80, false)
            mAppExecutors.runOnMainThread {
                bitmap?.let {
                    imgCodeAddress.setImageBitmap(bitmap)
                }
            }
        }
    }
    private fun closeServer(){
        WebService.stop(mActivity)
        serverOn = false
        switchServer.text = "开启"
        txtAddressTip.visibility = View.INVISIBLE
        txtServerAddress.visibility = View.INVISIBLE
        imgCodeAddress.visibility = View.INVISIBLE
    }

}