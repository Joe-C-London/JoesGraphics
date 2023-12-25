package com.joecollins.pubsub

import java.util.concurrent.Flow

internal class MapReducePublisher<T, R>(
    private val publishers: List<Flow.Publisher<out T>>,
    private val identity: R,
    private val onValueAdded: (R, T) -> R,
    private val onValueRemoved: (R, T) -> R,
) : AbstractPublisher<R>() {

    private data class Wrapper<T>(var value: T)

    private lateinit var subscribers: List<Subscriber<T>>

    private lateinit var value: Wrapper<R>
    private lateinit var list: MutableList<Wrapper<T>?>
    private val publishersCompleted = publishers.map { false }.toMutableList()

    override fun afterSubscribe() {
        val me = this
        if (numSubscriptions == 1) {
            value = Wrapper(identity)
            list = publishers.map { null }.toMutableList()
            subscribers = publishers.mapIndexed { index, publisher ->
                val subscriber = Subscriber<T>({ newVal ->
                    synchronized(me) {
                        val oldVal = list[index]?.value
                        if (oldVal != newVal) {
                            try {
                                oldVal?.let { value.value = onValueRemoved(value.value, it) }
                                list[index] = Wrapper(newVal)
                                newVal?.let { value.value = onValueAdded(value.value, it) }
                                me.submit(value.value)
                            } catch (e: Exception) {
                                error(e)
                            }
                        }
                    }
                }, {
                    synchronized(me) {
                        publishersCompleted[index] = true
                        if (publishersCompleted.all { it }) {
                            me.complete()
                        }
                    }
                }, {
                    error(it)
                })
                publisher.subscribe(subscriber)
                subscriber
            }
        }
    }

    override fun afterUnsubscribe() {
        if (numSubscriptions == 0) {
            subscribers.forEach { it.unsubscribe() }
        }
    }
}
