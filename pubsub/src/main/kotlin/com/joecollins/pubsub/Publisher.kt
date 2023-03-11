package com.joecollins.pubsub

class Publisher<T>() : AbstractPublisher<T>() {

    constructor(firstPublication: T) : this() {
        submit(firstPublication)
    }

    public override fun submit(item: T) {
        super.submit(item)
    }

    public override fun complete() {
        super.complete()
    }

    override fun afterSubscribe() {
    }

    override fun afterUnsubscribe() {
    }
}
