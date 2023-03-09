package com.joecollins.pubsub

import java.util.concurrent.Flow

internal class ComposedPublisher<T, R>(
    private val publisher: Flow.Publisher<T>,
    private val func: (T) -> Flow.Publisher<out R>,
) : AbstractPublisher<R>() {

    private lateinit var mainSubscriber: Subscriber<T>
    private var subSubscriber: Subscriber<R>? = null

    override fun afterSubscribe() {
        val me = this
        if (numSubscriptions == 1) {
            mainSubscriber = Subscriber { t ->
                val subPublisher = func(t)
                synchronized(me) {
                    subSubscriber?.unsubscribe()
                    subSubscriber = Subscriber { r ->
                        submit(r)
                    }
                    subPublisher.subscribe(subSubscriber)
                }
            }
            publisher.subscribe(mainSubscriber)
        }
    }

    override fun afterUnsubscribe() {
        if (numSubscriptions == 0) {
            mainSubscriber.unsubscribe()
            subSubscriber?.unsubscribe()
            subSubscriber = null
        }
    }
}
