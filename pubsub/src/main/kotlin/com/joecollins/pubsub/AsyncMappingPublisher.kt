package com.joecollins.pubsub

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Flow

internal class AsyncMappingPublisher<T, R>(
    private val publisher: Flow.Publisher<T>,
    private val func: (T) -> R,
    private val executor: ExecutorService,
) : AbstractPublisher<R>() {

    private lateinit var subscriber: Subscriber<T>
    private var future: CompletableFuture<*>? = null

    override fun afterSubscribe() {
        if (numSubscriptions == 1) {
            subscriber = Subscriber({
                future?.cancel(false)
                future = CompletableFuture.supplyAsync({
                    submit(func(it))
                }, executor).exceptionally { error(it) }
            }, {
                future?.thenRun { complete() } ?: complete()
            }, {
                error(it)
            })
            publisher.subscribe(subscriber)
        }
    }

    override fun afterUnsubscribe() {
        if (numSubscriptions == 0) {
            subscriber.unsubscribe()
        }
    }
}
