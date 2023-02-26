package com.joecollins.pubsub

import com.joecollins.graphics.utils.BoundResult
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.LinkedList
import java.util.concurrent.Flow
import java.util.concurrent.SubmissionPublisher

class PubSubTests {

    @Test
    fun testPubsubGetUpdateAfterSubscribe() {
        var output: String? = null
        val publisher = Publisher<String>()
        val subscriber = Subscriber<String> { output = it }
        publisher.subscribe(subscriber)

        publisher.submit("TEST")
        assertEquals("TEST", output)

        publisher.submit("TEST 2")
        assertEquals("TEST 2", output)
    }

    @Test
    fun testPubsubGetUpdateBeforeSubscribe() {
        var output: String? = null
        val publisher = Publisher<String>()
        val subscriber = Subscriber<String> { output = it }

        publisher.submit("TEST")
        publisher.subscribe(subscriber)
        assertEquals("TEST", output)

        publisher.submit("TEST 2")
        assertEquals("TEST 2", output)
    }

    @Test
    fun testPublishNull() {
        var output: String? = null
        val publisher = Publisher<String?>()
        val subscriber = Subscriber<String?> { output = it }
        publisher.subscribe(subscriber)

        publisher.submit("TEST")
        assertEquals("TEST", output)

        publisher.submit(null)
        assertNull(output)
    }

    @Test
    fun testSubscriberInterop() {
        var output: String? = null
        val publisher = SubmissionPublisher<String>()
        val subscriber = Subscriber<String> { output = it }
        publisher.subscribe(subscriber)

        publisher.submit("TEST")
        Thread.sleep(100)
        assertEquals("TEST", output)

        publisher.submit("TEST 2")
        Thread.sleep(100)
        assertEquals("TEST 2", output)
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
                Assertions.fail<Unit>()
            }
        }
        publisher.subscribe(subscriber)

        publisher.submit("TEST")
        assertEquals("TEST", output)

        publisher.submit("TEST 2")
        assertEquals("TEST 2", output)
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
                Assertions.fail<Unit>()
            }
        }
        publisher.subscribe(subscriber)

        publisher.submit("TEST")
        assertEquals("TEST", output)

        publisher.submit("TEST 2")
        assertEquals("TEST", output)
    }

    @Test
    fun testFixedPublisher() {
        var output: String? = null
        val publisher = "TEST".asOneTimePublisher()
        val subscriber = Subscriber<String> { output = it }
        publisher.subscribe(subscriber)
        assertEquals("TEST", output)
    }

    @Test
    fun testPublisherMap() {
        var output: Int? = null
        val publisher = SubmissionPublisher<String>()
        val subscriber = Subscriber<Int> { output = it }
        publisher.map { it.length }.subscribe(subscriber)

        publisher.submit("TEST")
        Thread.sleep(100)
        assertEquals(4, output)

        publisher.submit("TEST 2")
        Thread.sleep(100)
        assertEquals(6, output)
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
        Thread.sleep(100)
        assertEquals("AB", output)

        publisher1.submit("1")
        Thread.sleep(100)
        assertEquals("1B", output)

        publisher2.submit("2")
        Thread.sleep(100)
        assertEquals("12", output)
    }

    @Test
    fun testPublishInSequence() {
        val output = LinkedList<Int>()
        val publisher = Publisher<Int>()
        val subscriber = Subscriber<Int> { output.add(it) }
        publisher.subscribe(subscriber)

        val limit = 1000
        (0..limit).forEach { publisher.submit(it) }
        assertEquals((0..limit).toList(), output)
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
                Assertions.fail<Unit>("Shouldn't be completing")
            }
        }
        publisher.subscribe(subscriber)

        (0..limit).forEach { publisher.submit(it) }
        assertEquals((0..limit).toList(), output)
    }

    @Test
    fun testItemInCtor() {
        var output: String? = null
        val publisher = Publisher("TEST")
        val subscriber = Subscriber<String> { output = it }
        publisher.subscribe(subscriber)
        assertEquals("TEST", output)
    }

    @Test
    fun testMapElements() {
        var output: List<Int>? = null
        val publisher = Publisher<List<String>>()
        val subscriber = Subscriber<List<Int>> { output = it }
        publisher.mapElements { it.length }.subscribe(subscriber)

        publisher.submit(listOf("TEST", "TEST 2"))
        assertEquals(listOf(4, 6), output)
    }

    @Test
    fun testListCombiner() {
        var output: List<Int>? = null
        val publishers = (0..10).map { Publisher(it) }
        val subscriber = Subscriber<List<Int>> { output = it }
        publishers.combine().subscribe(subscriber)
        assertEquals((0..10).toList(), output)
    }

    @Test
    fun testMapReducePublisher() {
        val boundValue: BoundResult<Int> = BoundResult()
        val publishers = listOf(Publisher(1), Publisher(2), Publisher(3))
        publishers.mapReduce(0, { a, v -> a + v }, { a, v -> a - v })
            .subscribe(Subscriber { boundValue.value = it })
        assertEquals(6, boundValue.value)

        publishers[0].submit(4)
        publishers[1].submit(5)
        publishers[2].submit(6)
        assertEquals(15, boundValue.value)
    }

    @Test
    fun testComposePublisher() {
        var output: Int? = null
        val innerPublisher1 = Publisher(7)
        val outerPublisher = Publisher(innerPublisher1)
        outerPublisher.compose { it }.subscribe(Subscriber { output = it })
        assertEquals(7, output)

        innerPublisher1.submit(42)
        assertEquals(42, output)

        val innerPublisher2 = Publisher(12)
        outerPublisher.submit(innerPublisher2)
        assertEquals(12, output)

        innerPublisher1.submit(1)
        Thread.sleep(500)
        assertEquals(12, output)

        innerPublisher2.submit(27)
        assertEquals(27, output)

        innerPublisher1.submit(3)
        Thread.sleep(500)
        assertEquals(27, output)
    }
}
