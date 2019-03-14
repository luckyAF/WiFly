package com.luckyaf.wifly.utils

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.subscribers.SafeSubscriber
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * 类描述：
 * @author Created by luckyAF on 2019-03-13
 *
 */
abstract class FlowableWork<T:Any> : FlowableOnSubscribe<T> {

    private var mEmitter:FlowableEmitter<T>?=null

    override fun subscribe(emitter: FlowableEmitter<T>) {
        mEmitter = emitter
    }

    open fun start(){
        Flowable.create(this@FlowableWork, BackpressureStrategy.BUFFER)
            .subscribe(object:Subscriber<T>{
                override fun onComplete() {
                }

                override fun onSubscribe(s: Subscription?) {
                    s?.request(Long.MAX_VALUE)
                }

                override fun onNext(t: T) {
                    handleData(t)
                }

                override fun onError(t: Throwable?) {
                }
            })

    }

    abstract fun handleData(data:T)

    fun post(data:T){
        mEmitter?.onNext(data)
    }

    open fun finishWork(){
        mEmitter?.onComplete()
    }
}