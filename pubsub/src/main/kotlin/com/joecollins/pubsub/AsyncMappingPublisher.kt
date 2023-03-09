package com.joecollins.pubsub

import java.util.concurrent.ExecutorService
import java.util.concurrent.Flow
import java.util.concurrent.Future

internal class AsyncMappingPublisher<T, R>(
    private val publisher: Flow.Publisher<T>,
    private val func: (T) -> R,
    private val executor: ExecutorService,
) : AbstractPublisher<R>() {

    private lateinit var subscriber: Subscriber<T>
    private var future: Future<*>? = null

    override fun afterSubscribe() {
        if (numSubscriptions == 1) {
            subscriber = Subscriber {
                future?.cancel(false)
                future = executor.submit {
                    try {
                        submit(func(it))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
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
