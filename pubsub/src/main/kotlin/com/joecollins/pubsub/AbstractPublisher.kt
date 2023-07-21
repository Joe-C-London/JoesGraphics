package com.joecollins.pubsub

import com.joecollins.utils.ExecutorUtils
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Flow
import java.util.concurrent.LinkedBlockingQueue

abstract class AbstractPublisher<T> : Flow.Publisher<T> {
    private val subscriptions = ConcurrentHashMap.newKeySet<Subscription<T>>()
    private var value: ItemWrapper<T>? = null
    private var completed = false

    override fun subscribe(subscriber: Flow.Subscriber<in T>) {
        synchronized(this) {
            val subscription = Subscription(this, subscriber)
            subscriber.onSubscribe(subscription)
            subscriptions.add(subscription)
            value?.let { subscription.send(it) }
            if (completed) {
                subscription.send(CompleteWrapper())
                subscriptions.remove(subscription)
            } else {
                afterSubscribe()
            }
        }
    }

    internal open fun submit(item: T) {
        synchronized(this) {
            if (completed) {
                throw IllegalStateException("Cannot submit another value after completion")
            }
            val wrappedItem = ItemWrapper(item)
            value = wrappedItem
            subscriptions.forEach { it.send(wrappedItem) }
        }
    }

    internal open fun complete() {
        synchronized(this) {
            subscriptions.forEach { it.send(CompleteWrapper()) }
            subscriptions.clear()
            completed = true
        }
    }

    internal fun error(throwable: Throwable) {
        throwable.printStackTrace()
        synchronized(this) {
            subscriptions.forEach { it.send(ErrorWrapper(throwable)) }
            subscriptions.clear()
            completed = true
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

    protected fun finalize() {
        try {
            complete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private interface Wrapper<T> {
        fun handle(subscriber: Flow.Subscriber<in T>)
    }

    private data class ItemWrapper<T>(private val item: T) : Wrapper<T> {
        override fun handle(subscriber: Flow.Subscriber<in T>) {
            subscriber.onNext(item)
        }
    }

    private class CompleteWrapper<T> : Wrapper<T> {
        override fun handle(subscriber: Flow.Subscriber<in T>) {
            subscriber.onComplete()
        }
    }

    private data class ErrorWrapper<T>(private val throwable: Throwable) : Wrapper<T> {
        override fun handle(subscriber: Flow.Subscriber<in T>) {
            subscriber.onError(throwable)
        }
    }

    private class Subscription<T>(val publisher: AbstractPublisher<T>, val subscriber: Flow.Subscriber<in T>) : Flow.Subscription {

        private var waitingFor = 0L
        private var cancelled = false
        private val queue = LinkedBlockingQueue<Wrapper<T>>()
        private var future = CompletableFuture.completedFuture<Void>(null)

        fun send(item: Wrapper<T>) {
            synchronized(this) {
                if (!cancelled) {
                    queue.offer(item)
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
                val next = queue.take()
                future = future.thenRunAsync({
                    try {
                        next.handle(subscriber)
                    } catch (e: Exception) {
                        subscriber.onError(e)
                    }
                }, ExecutorUtils.defaultExecutor)
                waitingFor--
            }
        }

        override fun cancel() {
            synchronized(this) {
                queue.offer(CancelWrapper())
                process()
            }
        }

        private inner class CancelWrapper<T> : Wrapper<T> {
            override fun handle(subscriber: Flow.Subscriber<in T>) {
                synchronized(this@Subscription) {
                    cancelled = true
                    queue.clear()
                    waitingFor = 0L
                    publisher.unsubscribe(this@Subscription)
                }
            }
        }
    }
}
