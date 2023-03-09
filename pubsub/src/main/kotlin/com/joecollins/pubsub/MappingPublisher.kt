package com.joecollins.pubsub

import java.util.concurrent.Flow

internal class MappingPublisher<T, R>(
    private val publisher: Flow.Publisher<T>,
    private val func: (T) -> R,
) : AbstractPublisher<R>() {

    private lateinit var subscriber: Subscriber<T>

    override fun afterSubscribe() {
        if (numSubscriptions == 1) {
            subscriber = Subscriber {
                try {
                    submit(func(it))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            publisher.subscribe(subscriber)
        }
    }

    override fun afterUnsubscribe() {
        if (numSubscriptions == 0) {
            subscriber.unsubscribe()
        }
    }
}
