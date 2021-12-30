package com.joecollins.pubsub

import java.util.concurrent.Flow
import java.util.concurrent.LinkedBlockingQueue

class Publisher<T> : Flow.Publisher<T>, AutoCloseable {

    private val subscriptions = HashSet<Subscription>()
    private var value: Wrapper<T>? = null

    override fun subscribe(subscriber: Flow.Subscriber<in T>) {
        synchronized(this) {
            val subscription = Subscription(subscriber)
            subscriber.onSubscribe(subscription)
            subscriptions.add(subscription)
            @Suppress("UNCHECKED_CAST")
            value?.let { subscription.send(it.item) }
        }
    }

    fun submit(item: T) {
        synchronized(this) {
            value = Wrapper(item)
            subscriptions.forEach { it.send(item) }
        }
    }

    override fun close() {
        synchronized(this) {
            subscriptions.forEach {
                it.cancel()
                it.subscriber.onComplete()
            }
        }
    }

    private data class Wrapper<T>(val item: T)

    private inner class Subscription(val subscriber: Flow.Subscriber<in T>) : Flow.Subscription {

        private var waitingFor = 0L
        private var cancelled = false
        private val queue = LinkedBlockingQueue<Wrapper<T>>()

        fun send(item: T) {
            synchronized(this) {
                if (!cancelled) {
                    queue.offer(Wrapper(item))
                    process()
                }
            }
        }

        override fun request(n: Long) {
            synchronized(this) {
                waitingFor += n
                process()
            }
        }

        private fun process() {
            while (waitingFor > 0 && !queue.isEmpty()) {
                try {
                    subscriber.onNext(queue.take().item)
                } catch (e: Exception) {
                    subscriber.onError(e)
                }
                waitingFor--
            }
        }

        override fun cancel() {
            synchronized(this) {
                cancelled = true
            }
        }
    }
}

fun <T> T.asOneTimePublisher(): Flow.Publisher<T> {
    val publisher = Publisher<T>()
    publisher.submit(this)
    return publisher
}

fun <T, R> Flow.Publisher<T>.map(func: (T) -> R): Flow.Publisher<R> {
    val me = this
    return object : Flow.Publisher<R> {
        override fun subscribe(subscriber: Flow.Subscriber<in R>) {
            me.subscribe(object : Flow.Subscriber<T> {
                override fun onSubscribe(subscription: Flow.Subscription) {
                    subscriber.onSubscribe(subscription)
                }

                override fun onNext(item: T) {
                    try {
                        subscriber.onNext(func(item))
                    } catch (e: Exception) {
                        subscriber.onError(e)
                    }
                }

                override fun onError(throwable: Throwable?) {
                    subscriber.onError(throwable)
                }

                override fun onComplete() {
                    subscriber.onComplete()
                }
            })
        }
    }
}
