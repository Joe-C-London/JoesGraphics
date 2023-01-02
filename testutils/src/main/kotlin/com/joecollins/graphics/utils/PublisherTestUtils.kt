package com.joecollins.graphics.utils

import org.awaitility.Awaitility
import org.hamcrest.Matchers
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit

object PublisherTestUtils {

    fun <T> assertPublishes(
        publisher: Flow.Publisher<T>,
        expected: T,
        timeoutSeconds: Long = 1,
    ) {
        data class Wrapper<T>(val item: T)
        var result: Wrapper<T>? = null
        publisher.subscribe(object : Flow.Subscriber<T> {
            private lateinit var subscription: Flow.Subscription

            override fun onSubscribe(subscription: Flow.Subscription) {
                this.subscription = subscription
                this.subscription.request(1)
            }

            override fun onError(throwable: Throwable) {
                throwable.printStackTrace()
            }

            override fun onComplete() {
            }

            override fun onNext(item: T) {
                result = Wrapper(item)
                this.subscription.request(1)
            }
        })
        Awaitility.await().atMost(timeoutSeconds, TimeUnit.SECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ result!!.item }, Matchers.equalTo(expected))
    }
}
