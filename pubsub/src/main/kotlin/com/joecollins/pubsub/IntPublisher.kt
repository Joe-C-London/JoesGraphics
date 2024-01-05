package com.joecollins.pubsub

class IntPublisher(private var current: Int = 0) : AbstractPublisher<Int>() {

    init {
        submit(current)
    }

    fun increment(amount: Int) {
        current += amount
        submit(current)
    }

    fun set(amount: Int) {
        current = amount
        submit(current)
    }

    override fun afterSubscribe() { }

    override fun afterUnsubscribe() { }
}
