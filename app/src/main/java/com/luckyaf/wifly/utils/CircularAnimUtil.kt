package com.luckyaf.wifly.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.R.attr.path



/**
 * 类描述：
 *  * 对 ViewAnimationUtils.createCircularReveal() 方法的封装.
 * <p/>
 * Created on 16/7/20.
 * GitHub: https://github.com/XunMengWinter
 * @author Created by luckyAF on 2019-02-25
 *
 */
object CircularAnimUtil {
    val PERFECT_MILLS: Long = 618
    val MINI_RADIUS = 0

    /**
     * 向四周伸张，直到完成显示。
     */
    @SuppressLint("NewApi")
    fun show(myView: View, startRadius: Float, durationMills: Long) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            myView.visibility = View.VISIBLE
            return
        }

        val cx = (myView.left + myView.right) / 2
        val cy = (myView.top + myView.bottom) / 2

        val w = myView.width
        val h = myView.height

        // 勾股定理 & 进一法
        val finalRadius = Math.sqrt((w * w + h * h).toDouble()).toInt() + 1

        val anim = ViewAnimationUtils.createCircularReveal(
            myView,
            cx,
            cy,
            startRadius,
            finalRadius.toFloat()
        )
        myView.visibility = View.VISIBLE
        anim.duration = durationMills
        anim.start()
    }

    /**
     * 由满向中间收缩，直到隐藏。
     */
    @SuppressLint("NewApi")
    fun hide(myView: View, endRadius: Float, durationMills: Long) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            myView.visibility = View.INVISIBLE
            return
        }

        val cx = (myView.left + myView.right) / 2
        val cy = (myView.top + myView.bottom) / 2
        val w = myView.width
        val h = myView.height

        // 勾股定理 & 进一法
        val initialRadius = Math.sqrt((w * w + h * h).toDouble()).toInt() + 1

        val anim = ViewAnimationUtils.createCircularReveal(
            myView,
            cx,
            cy,
            initialRadius.toFloat(),
            endRadius
        )
        anim.duration = durationMills
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                myView.visibility = View.INVISIBLE
            }
        })

        anim.start()
    }


    fun expland(activity:Activity,triggerView: View, colorOrImageRes: Int,durationMills: Long, block:()->(Unit)){

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            return
        }
        var durationMills = durationMills
        val location = IntArray(2)
        triggerView.getLocationInWindow(location)
        val cx = location[0] + triggerView.width / 2
        val cy = location[1] + triggerView.height / 2
        val view = ImageView(triggerView.context)
        view.scaleType = ImageView.ScaleType.CENTER_CROP
        view.setImageResource(colorOrImageRes)

        val decorView = activity.window.decorView as ViewGroup
        val w = decorView.width
        val h = decorView.height
        val index = decorView.indexOfChild(triggerView)
        val params = ViewGroup.LayoutParams(w ,h)
        decorView.addView(view,index+1,  params)
        //decorView.addView(view,w,  h)


        // 计算中心点至view边界的最大距离
        val maxW = Math.max(cx, w - cx)
        val maxH = Math.max(cy, h - cy)
        val startRadius = Math.sqrt((triggerView.width  * triggerView.width  + triggerView.height  * triggerView.height).toDouble()).toInt() - 1
        val finalRadius = Math.sqrt((maxW * maxW + maxH * maxH).toDouble()).toInt() + 1
        val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, startRadius.toFloat(), finalRadius.toFloat())
        val maxRadius = Math.sqrt((w * w + h * h).toDouble()).toInt() + 1
        // 若使用默认时长，则需要根据水波扩散的距离来计算实际时间
        if (durationMills == PERFECT_MILLS) {
            // 算出实际边距与最大边距的比率
            val rate = 1.0 * finalRadius / maxRadius
            // 水波扩散的距离与扩散时间成正比
            durationMills = (PERFECT_MILLS * rate).toLong()
        }
        val finalDuration = durationMills
        anim.duration = finalDuration
        anim.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationCancel(animation: Animator?) {
                super.onAnimationCancel(animation)

            }
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                block()
                val anim = ViewAnimationUtils.createCircularReveal(
                    view,
                    cx,
                    cy,
                    finalRadius.toFloat(),
                    0f
                )
                anim.duration = finalDuration
                anim.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        super.onAnimationEnd(animation)
                        try {
                            decorView.removeView(view)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                })
                anim.start()
            }
        })
        anim.start()

    }



    /**
     * 从指定View开始向四周伸张(伸张颜色或图片为colorOrImageRes), 然后进入另一个Activity,
     * 返回至 @thisActivity 后显示收缩动画。
     */
    @SuppressLint("NewApi")
    fun startActivityForResult(
        thisActivity: Activity, intent: Intent, requestCode: Int?, bundle: Bundle?,
        triggerView: View, colorOrImageRes: Int, durationMills: Long
    ) {
        var durationMills = durationMills

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            thisActivity.startActivity(intent)
            return
        }

        val location = IntArray(2)
        triggerView.getLocationInWindow(location)
        val cx = location[0] + triggerView.width / 2
        val cy = location[1] + triggerView.height / 2
        val view = ImageView(thisActivity)
        view.scaleType = ImageView.ScaleType.CENTER_CROP
        view.setImageResource(colorOrImageRes)
        val decorView = thisActivity.window.decorView as ViewGroup
        val w = decorView.width
        val h = decorView.height
        decorView.addView(view, w, h)

        // 计算中心点至view边界的最大距离
        val maxW = Math.max(cx, w - cx)
        val maxH = Math.max(cy, h - cy)
        val finalRadius = Math.sqrt((maxW * maxW + maxH * maxH).toDouble()).toInt() + 1
        val anim = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0f, finalRadius.toFloat())
        val maxRadius = Math.sqrt((w * w + h * h).toDouble()).toInt() + 1
        // 若使用默认时长，则需要根据水波扩散的距离来计算实际时间
        if (durationMills == PERFECT_MILLS) {
            // 算出实际边距与最大边距的比率
            val rate = 1.0 * finalRadius / maxRadius
            // 水波扩散的距离与扩散时间成正比
            durationMills = (PERFECT_MILLS * rate).toLong()
        }
        val finalDuration = durationMills
        anim.duration = finalDuration
        anim.addListener(object : AnimatorListenerAdapter() {



            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                // 默认渐隐过渡动画.
                thisActivity.overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )

                // 默认显示返回至当前Activity的动画.
                triggerView.postDelayed({
                    val anim = ViewAnimationUtils.createCircularReveal(
                        view,
                        cx,
                        cy,
                        finalRadius.toFloat(),
                        0f
                    )
                    anim.duration = finalDuration
                    anim.addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            try {
                                decorView.removeView(view)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                    })
                    anim.start()
                }, 1000)

            }
        })
        anim.start()
    }


    /*下面的方法全是重载，用简化上面方法的构建*/


    fun startActivityForResult(
        thisActivity: Activity,
        intent: Intent,
        requestCode: Int?,
        triggerView: View,
        colorOrImageRes: Int
    ) {
        startActivityForResult(
            thisActivity,
            intent,
            requestCode,
            null,
            triggerView,
            colorOrImageRes,
            PERFECT_MILLS
        )
    }

    fun startActivity(
        thisActivity: Activity,
        intent: Intent,
        triggerView: View,
        colorOrImageRes: Int,
        durationMills: Long
    ) {
        startActivityForResult(
            thisActivity,
            intent,
            null,
            null,
            triggerView,
            colorOrImageRes,
            durationMills
        )
    }

    fun startActivity(
        thisActivity: Activity, intent: Intent, triggerView: View, colorOrImageRes: Int
    ) {
        startActivity(thisActivity, intent, triggerView, colorOrImageRes, PERFECT_MILLS)
    }

    fun startActivity(
        thisActivity: Activity,
        targetClass: Class<*>,
        triggerView: View,
        colorOrImageRes: Int
    ) {
        startActivity(
            thisActivity,
            Intent(thisActivity, targetClass),
            triggerView,
            colorOrImageRes,
            PERFECT_MILLS
        )
    }

    fun show(myView: View) {
        show(myView, MINI_RADIUS.toFloat(), PERFECT_MILLS)
    }

    fun hide(myView: View) {
        hide(myView, MINI_RADIUS.toFloat(), PERFECT_MILLS)
    }

}