package com.luckyaf.wifly.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.luckyaf.wifly.server.WebServer

/**
 * 类描述：
 * @author Created by luckyAF on 2019-02-15
 *
 */
class WebService : Service() {

    companion object {
        private const val ACTION_START_WEB_SERVICE =
            "com.luckyaf.wifly.action.START_WEB_SERVICE"
        private  const val ACTION_STOP_WEB_SERVICE = "com.luckyaf.wifly.action.STOP_WEB_SERVICE"
        fun start(context: Context) {
            val intent = Intent(context, WebService::class.java)
            intent.action = ACTION_START_WEB_SERVICE
            context.startService(intent)
        }
        fun stop(context: Context) {
            val intent = Intent(context, WebService::class.java)
            intent.action = ACTION_STOP_WEB_SERVICE
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (ACTION_START_WEB_SERVICE == action) {
                WebServer.getInstance(this).run()
            } else if (ACTION_STOP_WEB_SERVICE == action) {
                WebServer.getInstance(this).stop()

            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        WebServer.getInstance(this).stop()
        super.onDestroy()
    }
}