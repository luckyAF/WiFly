package com.luckyaf.wifly

import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.reactivestreams.Subscription
import org.reactivestreams.Subscriber
import io.reactivex.schedulers.Schedulers
import io.reactivex.BackpressureStrategy
import io.reactivex.FlowableEmitter
import io.reactivex.FlowableOnSubscribe
import io.reactivex.Flowable



/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("com.luckyaf.wifly", appContext.packageName)
    }

    @Test
    fun flowable(){

    }
}
