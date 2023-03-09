package com.joecollins.pubsub

import com.joecollins.utils.ExecutorUtils
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Flow
import java.util.concurrent.LinkedBlockingQueue

abstract class AbstractPublisher<T> : Flow.Publisher<T> {
    private val subscriptions = ConcurrentHashMap.newKeySet<Subscription<T>>()
    private var value: Wrapper<T>? = null

    override fun subscribe(subscriber: Flow.Subscriber<in T>) {
        synchronized(this) {
            val subscription = Subscription(this, subscriber)
            subscriber.onSubscribe(subscription)
            subscriptions.add(subscription)
            @Suppress("UNCHECKED_CAST")
            value?.let { subscription.send(it.item) }
            afterSubscribe()
        }
    }

    internal open fun submit(item: T) {
        synchronized(this) {
            value = Wrapper(item)
            subscriptions.forEach { it.send(item) }
        }
    }

    private fun unsubscribe(subscription: Subscription<T>) {
        synchronized(this) {
            subscriptions.remove(subscription)
            afterUnsubscribe()
        }
    }

    internal val numSubscriptions get() = subscriptions.size

    internal abstract fun afterSubscribe()
    internal abstract fun afterUnsubscribe()

    private data class Wrapper<T>(val item: T)

    private class Subscription<T>(val publisher: AbstractPublisher<T>, val subscriber: Flow.Subscriber<in T>) : Flow.Subscription {

        private var waitingFor = 0L
        private var cancelled = false
        private val queue = LinkedBlockingQueue<Wrapper<T>>()
        private var future = CompletableFuture.completedFuture<Void>(null)

        fun send(item: T) {
            synchronized(this) {
                if (!cancelled) {
                    queue.offer(Wrapper(item))
                    process()
                }
            }
        }

        override fun request(n: Long) {
            if (n <= 0) {
                subscriber.onError(IllegalArgumentException("Cannot request $n items, as this must be greater than zero"))
            }
            synchronized(this) {
                waitingFor += n
                process()
            }
        }

        private fun process() {
            while (waitingFor > 0 && !queue.isEmpty()) {
                val next = queue.take().item
                future = future.thenRunAsync({
                    try {
                        subscriber.onNext(next)
                    } catch (e: Exception) {
                        subscriber.onError(e)
                    }
                }, ExecutorUtils.defaultExecutor)
                waitingFor--
            }
        }

        override fun cancel() {
            synchronized(this) {
                cancelled = true
                queue.clear()
                waitingFor = 0L
                publisher.unsubscribe(this)
            }
        }
    }
}
