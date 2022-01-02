package com.joecollins.pubsub

import com.joecollins.graphics.utils.BoundResult
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNull
import org.junit.Assert
import org.junit.Test
import java.util.LinkedList
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

    @Test
    fun testPublishInSequence() {
        val output = LinkedList<Int>()
        val publisher = Publisher<Int>()
        val subscriber = Subscriber<Int> { output.add(it) }
        publisher.subscribe(subscriber)

        val limit = 1000
        (0..limit).forEach { publisher.submit(it) }
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.size }, IsEqual(limit + 1))
        Assert.assertEquals((0..limit).toList(), output)
    }

    @Test
    fun testPublishInSequenceAllAtOnce() {
        val output = LinkedList<Int>()
        val publisher = Publisher<Int>()
        val limit = 1000
        val subscriber = object : Flow.Subscriber<Int> {
            override fun onSubscribe(subscription: Flow.Subscription) {
                subscription.request(limit + 1L)
            }

            override fun onNext(item: Int) {
                output.add(item)
            }

            override fun onError(throwable: Throwable) {
                throw throwable
            }

            override fun onComplete() {
                Assert.fail("Shouldn't be completing")
            }
        }
        publisher.subscribe(subscriber)

        (0..limit).forEach { publisher.submit(it) }
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.size }, IsEqual(limit + 1))
        Assert.assertEquals((0..limit).toList(), output)
    }

    @Test
    fun testItemInCtor() {
        var output: String? = null
        val publisher = Publisher("TEST")
        val subscriber = Subscriber<String> { output = it }
        publisher.subscribe(subscriber)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS).pollDelay(1, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual("TEST"))
    }

    @Test
    fun testMapElements() {
        var output: List<Int>? = null
        val publisher = Publisher<List<String>>()
        val subscriber = Subscriber<List<Int>> { output = it }
        publisher.mapElements { it.length }.subscribe(subscriber)

        publisher.submit(listOf("TEST", "TEST 2"))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual(listOf(4, 6)))
    }

    @Test
    fun testListCombiner() {
        var output: List<Int>? = null
        val publishers = (0..10).map { Publisher(it) }
        val subscriber = Subscriber<List<Int>> { output = it }
        publishers.combine().subscribe(subscriber)

        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output }, IsEqual((0..10).toList()))
    }

    @Test
    fun testMapReducePublisher() {
        val boundValue: BoundResult<Int> = BoundResult()
        val publishers = listOf(Publisher(1), Publisher(2), Publisher(3))
        publishers.mapReduce(0, { a: Int, v: Int -> a + v }, { a: Int, v: Int -> a - v })
            .subscribe(Subscriber { boundValue.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ boundValue.value }, IsEqual(6))
        publishers[0].submit(4)
        publishers[1].submit(5)
        publishers[2].submit(6)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ boundValue.value }, IsEqual(15))
    }
}
