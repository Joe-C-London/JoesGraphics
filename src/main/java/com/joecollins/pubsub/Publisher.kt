package com.joecollins.pubsub

import org.apache.commons.lang3.tuple.MutablePair
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Flow
import java.util.concurrent.LinkedBlockingQueue

class Publisher<T>() : Flow.Publisher<T>, AutoCloseable {

    private val subscriptions = HashSet<Subscription>()
    private var value: Wrapper<T>? = null

    constructor(firstPublication: T) : this() {
        submit(firstPublication)
    }

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
                future = future.thenRunAsync {
                    try {
                        subscriber.onNext(next)
                    } catch (e: Exception) {
                        subscriber.onError(e)
                    }
                }
                waitingFor--
            }
        }

        override fun cancel() {
            synchronized(this) {
                cancelled = true
                queue.clear()
                waitingFor = 0L
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
    val publisher = Publisher<R>()
    subscribe(Subscriber { publisher.submit(func(it)) })
    return publisher
}

fun <T, R> Flow.Publisher<out List<T>>.mapElements(func: (T) -> R): Flow.Publisher<List<R>> = map { it.map(func) }

fun <T, U, R> Flow.Publisher<T>.merge(other: Flow.Publisher<U>, func: (T, U) -> R): Flow.Publisher<R> {
    data class Wrapper<T>(val item: T)
    val publisher = Publisher<R>()
    val pair = MutablePair<Wrapper<T>?, Wrapper<U>?>(null, null)
    val onUpdate = {
        val left = pair.left
        val right = pair.right
        if (left != null && right != null)
            publisher.submit(func(left.item, right.item))
    }
    this.subscribe(
        Subscriber {
            synchronized(pair) {
                pair.left = Wrapper(it)
                onUpdate()
            }
        }
    )
    other.subscribe(
        Subscriber {
            synchronized(pair) {
                pair.right = Wrapper(it)
                onUpdate()
            }
        }
    )
    return publisher
}
