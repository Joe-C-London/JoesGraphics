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

fun <T> CompletableFuture<T>.asPublisher(): Flow.Publisher<T> {
    val publisher = Publisher<T>()
    this.exceptionally { it.printStackTrace(); throw it; }
        .thenAccept { publisher.submit(it) }
    return publisher
}

fun <T> CompletableFuture<T>.asPublisher(init: T): Flow.Publisher<T> {
    val publisher = Publisher(init)
    this.exceptionally { it.printStackTrace(); throw it; }
        .thenAccept { publisher.submit(it) }
    return publisher
}

fun <T, R> Flow.Publisher<T>.map(func: (T) -> R): Flow.Publisher<R> {
    val publisher = Publisher<R>()
    subscribe(
        Subscriber {
            try {
                publisher.submit(func(it))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    )
    return publisher
}

fun <T, R> Flow.Publisher<out List<T>>.mapElements(func: (T) -> R): Flow.Publisher<List<R>> = map { it.map(func) }

fun <K, V, R> Flow.Publisher<out Map<K, V>>.mapKeys(func: (K) -> R): Flow.Publisher<Map<R, V>> = map { map -> map.mapKeys { func(it.key) } }

fun <T, U, R> Flow.Publisher<T>.merge(other: Flow.Publisher<out U>, func: (T, U) -> R): Flow.Publisher<R> {
    data class Wrapper<T>(val item: T)
    val publisher = Publisher<R>()
    val pair = MutablePair<Wrapper<T>?, Wrapper<U>?>(null, null)
    val onUpdate = {
        val left = pair.left
        val right = pair.right
        if (left != null && right != null) {
            publisher.submit(func(left.item, right.item))
        }
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

fun <T, R> List<Flow.Publisher<out T>>.mapReduce(identity: R, onValueAdd: (R, T) -> R, onValueRemove: (R, T) -> R): Flow.Publisher<R> {
    data class Wrapper(val item: T)
    val publisher = Publisher<R>()
    val list: MutableList<Wrapper?> = this.map { null }.toMutableList()
    var value = identity
    this.forEachIndexed { index, pub ->
        pub.subscribe(
            Subscriber { newVal ->
                synchronized(list) {
                    if (list[index]?.item != newVal) {
                        list[index]?.let { oldValWrap -> value = onValueRemove(value, oldValWrap.item) }
                        list[index] = Wrapper(newVal)
                        value = onValueAdd(value, newVal)
                        publisher.submit(value)
                    }
                }
            }
        )
    }
    return publisher
}

fun <T> List<Flow.Publisher<T>>.combine(): Flow.Publisher<List<T>> {
    data class Wrapper(val item: T)
    val publisher = Publisher<List<T>>()
    val list: MutableList<Wrapper?> = this.map { null }.toMutableList()
    this.forEachIndexed { index, pub ->
        pub.subscribe(
            Subscriber {
                synchronized(list) {
                    list[index] = Wrapper(it)
                    if (list.none { it == null }) {
                        publisher.submit(list.map { it!!.item })
                    }
                }
            }
        )
    }
    return publisher
}

fun <T, R> Flow.Publisher<T>.compose(func: (T) -> Flow.Publisher<out R>): Flow.Publisher<R> {
    val ret = Publisher<R>()
    var currSubscription: Flow.Subscription? = null
    this.subscribe(
        Subscriber<T> { t ->
            val nestedPublisher = func(t)
            synchronized(ret) {
                currSubscription?.cancel()
                nestedPublisher.subscribe(object : Flow.Subscriber<R> {
                    private var thisSubscription: Flow.Subscription? = null

                    override fun onSubscribe(subscription: Flow.Subscription) {
                        synchronized(ret) {
                            currSubscription = subscription
                            thisSubscription = subscription
                            subscription.request(1)
                        }
                    }

                    override fun onNext(item: R) {
                        synchronized(ret) {
                            if (thisSubscription === currSubscription) {
                                ret.submit(item)
                                currSubscription!!.request(1)
                            }
                        }
                    }

                    override fun onError(throwable: Throwable) {
                        throwable.printStackTrace()
                    }

                    override fun onComplete() {
                    }
                })
            }
        }
    )
    return ret
}