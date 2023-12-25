package com.joecollins.pubsub

import java.util.concurrent.Flow

internal class CombinedPublisher<T>(
    private val publishers: List<Flow.Publisher<out T>>,
) : AbstractPublisher<List<T>>() {

    private data class Wrapper<T>(var value: T)

    private lateinit var subscribers: List<Subscriber<T>>

    private lateinit var list: MutableList<Wrapper<T>?>
    private val publishersCompleted = publishers.map { false }.toMutableList()

    override fun afterSubscribe() {
        val me = this
        if (numSubscriptions == 1) {
            list = publishers.map { null }.toMutableList()
            subscribers = publishers.mapIndexed { index, publisher ->
                val subscriber = Subscriber<T>({ newVal ->
                    synchronized(me) {
                        list[index] = Wrapper(newVal)
                        if (list.none { it == null }) {
                            me.submit(list.map { it!!.value })
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
