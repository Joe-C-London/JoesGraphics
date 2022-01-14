package com.joecollins.pubsub

import java.awt.EventQueue
import java.util.concurrent.Flow

class Subscriber<T>(private val next: (T) -> Unit) : Flow.Subscriber<T> {
    private var subscription: Flow.Subscription? = null

    override fun onSubscribe(subscription: Flow.Subscription) {
        this.subscription = subscription
        this.subscription?.request(1)
    }

    override fun onNext(item: T) {
        next(item)
        this.subscription?.request(1)
    }

    override fun onError(throwable: Throwable) {
        throwable.printStackTrace()
    }

    override fun onComplete() {
    }

    companion object {
        fun <T> eventQueueWrapper(func: (T) -> Unit): (T) -> Unit {
            data class Wrapper(val item: T)
            val lock = Object()
            var wrapper: Wrapper? = null
            return { item ->
                synchronized(lock) {
                    val submit = wrapper == null
                    wrapper = Wrapper(item)
                    if (submit) {
                        EventQueue.invokeLater {
                            synchronized(lock) {
                                func(wrapper!!.item)
                                wrapper = null
                            }
                        }
                    }
                }
            }
        }
    }
}
