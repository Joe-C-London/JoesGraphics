package com.joecollins.pubsub

import com.joecollins.utils.ExecutorUtils
import java.util.concurrent.Flow

class Subscriber<T>(
    private val next: (T) -> Unit,
    private val completed: () -> Unit = {},
    private val error: (Throwable) -> Unit = { it.printStackTrace() },
) : Flow.Subscriber<T> {

    constructor(next: (T) -> Unit) : this(next, {})

    private lateinit var subscription: Flow.Subscription

    override fun onSubscribe(subscription: Flow.Subscription) {
        this.subscription = subscription
        this.subscription.request(1)
    }

    override fun onNext(item: T) {
        next(item)
        this.subscription.request(1)
    }

    override fun onError(throwable: Throwable) {
        error(throwable)
    }

    override fun onComplete() {
        completed()
    }

    fun unsubscribe() {
        subscription.cancel()
    }

    companion object {
        fun <T> eventQueueWrapper(func: (T) -> Unit): (T) -> Unit {
            data class Wrapper(val item: T)
            val lock = Any()
            var wrapper: Wrapper? = null
            return { item ->
                synchronized(lock) {
                    val submit = wrapper == null
                    wrapper = Wrapper(item)
                    if (submit) {
                        ExecutorUtils.sendToEventQueue {
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
