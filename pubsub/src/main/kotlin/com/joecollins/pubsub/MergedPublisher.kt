package com.joecollins.pubsub

import java.util.concurrent.Flow

internal class MergedPublisher<T, U, R>(
    private val left: Flow.Publisher<T>,
    private val right: Flow.Publisher<U>,
    private val func: (T, U) -> R,
) : AbstractPublisher<R>() {

    private data class Wrapper<T>(val value: T)

    private lateinit var leftSubscriber: Subscriber<T>
    private lateinit var rightSubscriber: Subscriber<U>

    private var leftValue: Wrapper<T>? = null
    private var rightValue: Wrapper<U>? = null

    override fun afterSubscribe() {
        val me = this
        if (numSubscriptions == 1) {
            leftSubscriber = Subscriber {
                synchronized(me) {
                    leftValue = Wrapper(it)
                    onUpdate()
                }
            }
            rightSubscriber = Subscriber {
                synchronized(me) {
                    rightValue = Wrapper(it)
                    onUpdate()
                }
            }
            left.subscribe(leftSubscriber)
            right.subscribe(rightSubscriber)
        }
    }

    private fun onUpdate() {
        val l = leftValue
        val r = rightValue
        if (l != null && r != null) {
            submit(func(l.value, r.value))
        }
    }

    override fun afterUnsubscribe() {
        if (numSubscriptions == 0) {
            leftSubscriber.unsubscribe()
            rightSubscriber.unsubscribe()
            leftValue = null
            rightValue = null
        }
    }
}
