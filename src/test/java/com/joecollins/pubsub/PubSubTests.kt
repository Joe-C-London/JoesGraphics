package com.joecollins.pubsub

import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.Flow
import java.util.concurrent.SubmissionPublisher
import java.util.concurrent.TimeUnit

class PubSubTests {

    @Test
    fun testPubsubGetUpdateAfterSubscribe() {
        var output: String? = null
        val publisher = Publisher<String>()
        val subscriber = Subscriber<String> { output = it }
        publisher.subscribe(subscriber)

        publisher.submit("TEST")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST"))

        publisher.submit("TEST 2")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST 2"))
    }

    @Test
    fun testPubsubGetUpdateBeforeSubscribe() {
        var output: String? = null
        val publisher = Publisher<String>()
        val subscriber = Subscriber<String> { output = it }

        publisher.submit("TEST")
        publisher.subscribe(subscriber)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST"))

        publisher.submit("TEST 2")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST 2"))
    }

    @Test
    fun testPublishNull() {
        var output: String? = null
        val publisher = Publisher<String?>()
        val subscriber = Subscriber<String?> { output = it }
        publisher.subscribe(subscriber)

        publisher.submit("TEST")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST"))

        publisher.submit(null)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsNull())
    }

    @Test
    fun testSubscriberInterop() {
        var output: String? = null
        val publisher = SubmissionPublisher<String>()
        val subscriber = Subscriber<String> { output = it }
        publisher.subscribe(subscriber)

        publisher.submit("TEST")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST"))

        publisher.submit("TEST 2")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST 2"))
    }

    @Test
    fun testPublisherInterop() {
        var output: String? = null
        val publisher = Publisher<String>()
        val subscriber = object : Flow.Subscriber<String> {
            private var subscription: Flow.Subscription? = null

            override fun onSubscribe(subscription: Flow.Subscription) {
                this.subscription = subscription
                this.subscription!!.request(1)
            }

            override fun onNext(item: String?) {
                output = item
                this.subscription!!.request(1)
            }

            override fun onError(throwable: Throwable) {
                throw throwable
            }

            override fun onComplete() {
                Assert.fail()
            }
        }
        publisher.subscribe(subscriber)

        publisher.submit("TEST")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST"))

        publisher.submit("TEST 2")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST 2"))
    }

    @Test
    fun testPublisherCancelInterop() {
        var output: String? = null
        val publisher = Publisher<String>()
        val subscriber = object : Flow.Subscriber<String> {
            private var subscription: Flow.Subscription? = null

            override fun onSubscribe(subscription: Flow.Subscription) {
                this.subscription = subscription
                this.subscription!!.request(1)
            }

            override fun onNext(item: String?) {
                output = item
                this.subscription!!.cancel()
            }

            override fun onError(throwable: Throwable) {
                throw throwable
            }

            override fun onComplete() {
                Assert.fail()
            }
        }
        publisher.subscribe(subscriber)

        publisher.submit("TEST")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST"))

        publisher.submit("TEST 2")
        Thread.sleep(500)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST"))
    }

    @Test
    fun testFixedPublisher() {
        var output: String? = null
        val publisher = "TEST".asOneTimePublisher()
        val subscriber = Subscriber<String> { output = it }
        publisher.subscribe(subscriber)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST"))
    }

    @Test
    fun testPublisherMap() {
        var output: Int? = null
        val publisher = SubmissionPublisher<String>()
        val subscriber = Subscriber<Int> { output = it }
        publisher.map { it.length }.subscribe(subscriber)

        publisher.submit("TEST")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual(4))

        publisher.submit("TEST 2")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual(6))
    }

    @Test
    fun testPublisherMerge() {
        var output: String? = null
        val publisher1 = SubmissionPublisher<String>()
        val publisher2 = SubmissionPublisher<String>()
        val subscriber = Subscriber<String> { output = it }
        publisher1.merge(publisher2) { a, b -> a + b }.subscribe(subscriber)

        publisher1.submit("A")
        publisher2.submit("B")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("AB"))

        publisher1.submit("1")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("1B"))

        publisher2.submit("2")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("12"))
    }
}
