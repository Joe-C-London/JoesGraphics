package com.joecollins.graphics.utils

import org.junit.jupiter.api.Assertions.assertEquals
import java.util.concurrent.Flow

object PublisherTestUtils {

    fun <T> assertPublishes(
        publisher: Flow.Publisher<out T>,
        expected: T,
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
        assertEquals(expected, result!!.item)
    }
}
