package com.joecollins.pubsub

import java.util.concurrent.Flow

internal class ComposedPublisher<T, R>(
    private val publisher: Flow.Publisher<T>,
    private val func: (T) -> Flow.Publisher<out R>,
) : AbstractPublisher<R>() {

    private lateinit var mainSubscriber: Subscriber<T>
    private var subSubscriber: Subscriber<R>? = null

    private var topCompleted = false
    private var subCompleted = false

    override fun afterSubscribe() {
        if (numSubscriptions == 1) {
            mainSubscriber = Subscriber({ t ->
                onTopUpdated(t)
            }, {
                onTopCompleted()
            }, {
                error(it)
            })
            publisher.subscribe(mainSubscriber)
        }
    }

    private fun onTopUpdated(t: T) {
        try {
            val subPublisher = func(t)
            synchronized(this) {
                subSubscriber?.unsubscribe()
                subCompleted = false
                subSubscriber = Subscriber({ r ->
                    onSubUpdated(r)
                }, {
                    onSubCompleted()
                }, {
                    error(it)
                })
                subPublisher.subscribe(subSubscriber)
            }
        } catch (e: Exception) {
            error(e)
        }
    }

    private fun onTopCompleted() {
        synchronized(this) {
            topCompleted = true
            if (subCompleted) {
                complete()
            }
        }
    }

    private fun onSubUpdated(r: R) {
        synchronized(this) {
            submit(r)
        }
    }

    private fun onSubCompleted() {
        synchronized(this) {
            subCompleted = true
            if (topCompleted) {
                complete()
            }
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
