package com.luckyaf.wifly

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOnSubscribe
import io.reactivex.schedulers.Schedulers
import org.junit.Test

import org.junit.Assert.*
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun flowable(){
        Flowable
            .create(FlowableOnSubscribe<Int> { e ->
                println("发射----> 1")
                e.onNext(1)
                println("发射----> 2")
                e.onNext(2)
                println("发射----> 3")
                e.onNext(3)
                println("发射----> 完成")
                e.onComplete()
            }, BackpressureStrategy.BUFFER) //create方法中多了一个BackpressureStrategy类型的参数
            .subscribeOn(Schedulers.newThread())//为上下游分别指定各自的线程
            .observeOn(Schedulers.newThread())
            .subscribe(object : Subscriber<Int> {
                override fun onSubscribe(s: Subscription) {   //onSubscribe回调的参数不是Disposable而是Subscription
                    s.request(java.lang.Long.MAX_VALUE)            //注意此处，暂时先这么设置
                }

                override fun onNext(integer: Int?) {
                    Thread.sleep(1000)
                    System.out.println("接收----> " + integer!!)
                }

                override fun onError(t: Throwable) {}

                override fun onComplete() {
                    System.out.println("接收----> 完成")
                }
            })
    }
}
