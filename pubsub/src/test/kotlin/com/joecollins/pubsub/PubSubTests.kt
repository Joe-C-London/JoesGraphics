package com.joecollins.pubsub

import org.awaitility.Awaitility
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.IllegalStateException
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.LinkedList
import java.util.concurrent.CountDownLatch
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
        var latch = CountDownLatch(1)
        val subscriber = Subscriber<String> {
            output = it
            latch.countDown()
        }
        publisher.subscribe(subscriber)

        latch = CountDownLatch(1)
        publisher.submit("TEST")
        latch.await()
        assertEquals("TEST", output)

        latch = CountDownLatch(1)
        publisher.submit("TEST 2")
        latch.await()
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

            override fun onError(throwable: Throwable): Unit = throw throwable

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

            override fun onError(throwable: Throwable): Unit = throw throwable

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
        var completed = false
        val publisher = "TEST".asOneTimePublisher()
        val subscriber = Subscriber<String>({ output = it }, { completed = true })
        publisher.subscribe(subscriber)
        assertEquals("TEST", output)
        assertTrue(completed)
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
        Awaitility.await().timeout(10, TimeUnit.SECONDS).until(
            { output },
            Matchers.equalTo("AB"),
        )

        publisher1.submit("1")
        Awaitility.await().timeout(10, TimeUnit.SECONDS).until(
            { output },
            Matchers.equalTo("1B"),
        )

        publisher2.submit("2")
        Awaitility.await().timeout(10, TimeUnit.SECONDS).until(
            { output },
            Matchers.equalTo("12"),
        )
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

            override fun onError(throwable: Throwable): Unit = throw throwable

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

    @Test
    fun testUnsubscribeRemovesSubscription() {
        val publisher = Publisher(5)
        var value = 0
        val subscriber = Subscriber<Int> { value = it }
        publisher.subscribe(subscriber)
        assertEquals(1, publisher.numSubscriptions)
        assertEquals(5, value)

        publisher.submit(6)
        assertEquals(1, publisher.numSubscriptions)
        assertEquals(6, value)

        subscriber.unsubscribe()
        publisher.submit(7)
        assertEquals(0, publisher.numSubscriptions)
        assertEquals(6, value)
    }

    @Test
    fun testUnsubscribeFromMappedPublisher() {
        val mainPublisher = Publisher(5)
        val mappedPublisher = mainPublisher.map { "A".repeat(it) }
        assertEquals(0, mainPublisher.numSubscriptions)

        var value1 = ""
        val subscriber1 = Subscriber<String> { value1 = it }
        mappedPublisher.subscribe(subscriber1)
        assertEquals(1, mainPublisher.numSubscriptions)
        assertEquals("AAAAA", value1)

        var value2 = ""
        val subscriber2 = Subscriber<String> { value2 = it }
        mappedPublisher.subscribe(subscriber2)
        assertEquals(1, mainPublisher.numSubscriptions)
        assertEquals("AAAAA", value2)

        subscriber1.unsubscribe()
        mainPublisher.submit(3)
        assertEquals(1, mainPublisher.numSubscriptions)
        assertEquals("AAAAA", value1)
        assertEquals("AAA", value2)

        subscriber2.unsubscribe()
        mainPublisher.submit(1)
        assertEquals(0, mainPublisher.numSubscriptions)
        assertEquals("AAAAA", value1)
        assertEquals("AAA", value2)

        var value3 = ""
        val subscriber3 = Subscriber<String> { value3 = it }
        mappedPublisher.subscribe(subscriber3)
        assertEquals(1, mainPublisher.numSubscriptions)
        assertEquals("AAAAA", value1)
        assertEquals("AAA", value2)
        assertEquals("A", value3)

        subscriber3.unsubscribe()
        mainPublisher.submit(2)
        assertEquals(0, mainPublisher.numSubscriptions)
        assertEquals("AAAAA", value1)
        assertEquals("AAA", value2)
        assertEquals("A", value3)
    }

    @Test
    fun testUnsubscribeMergedPublisher() {
        val mainPublisher1 = Publisher("A")
        val mainPublisher2 = Publisher("B")
        val mergedPublisher = mainPublisher1.merge(mainPublisher2) { a, b -> a + b }
        assertEquals(0, mainPublisher1.numSubscriptions)
        assertEquals(0, mainPublisher2.numSubscriptions)

        var value1 = ""
        val subscriber1 = Subscriber<String> { value1 = it }
        mergedPublisher.subscribe(subscriber1)
        assertEquals(1, mainPublisher1.numSubscriptions)
        assertEquals(1, mainPublisher2.numSubscriptions)
        assertEquals("AB", value1)

        var value2 = ""
        val subscriber2 = Subscriber<String> { value2 = it }
        mergedPublisher.subscribe(subscriber2)
        assertEquals(1, mainPublisher1.numSubscriptions)
        assertEquals(1, mainPublisher2.numSubscriptions)
        assertEquals("AB", value2)

        subscriber1.unsubscribe()
        mainPublisher1.submit("AA")
        assertEquals(1, mainPublisher1.numSubscriptions)
        assertEquals(1, mainPublisher2.numSubscriptions)
        assertEquals("AB", value1)
        assertEquals("AAB", value2)

        subscriber2.unsubscribe()
        mainPublisher2.submit("BB")
        assertEquals(0, mainPublisher1.numSubscriptions)
        assertEquals(0, mainPublisher2.numSubscriptions)
        assertEquals("AB", value1)
        assertEquals("AAB", value2)

        var value3 = ""
        val subscriber3 = Subscriber<String> { value3 = it }
        mergedPublisher.subscribe(subscriber3)
        assertEquals(1, mainPublisher1.numSubscriptions)
        assertEquals(1, mainPublisher2.numSubscriptions)
        assertEquals("AB", value1)
        assertEquals("AAB", value2)
        assertEquals("AABB", value3)
    }

    @Test
    fun testUnsubscribeCombinePublisher() {
        val mainPublishers = listOf(
            Publisher("A"),
            Publisher("B"),
            Publisher("C"),
            Publisher("D"),
            Publisher("E"),
        )
        val mergedPublisher = mainPublishers.combine()
        assertEquals(0, mainPublishers.sumOf { it.numSubscriptions })

        var value1 = emptyList<String>()
        val subscriber1 = Subscriber<List<String>> { value1 = it }
        mergedPublisher.subscribe(subscriber1)
        assertEquals(5, mainPublishers.sumOf { it.numSubscriptions })
        assertEquals(listOf("A", "B", "C", "D", "E"), value1)

        var value2 = emptyList<String>()
        val subscriber2 = Subscriber<List<String>> { value2 = it }
        mergedPublisher.subscribe(subscriber2)
        assertEquals(5, mainPublishers.sumOf { it.numSubscriptions })
        assertEquals(listOf("A", "B", "C", "D", "E"), value2)

        subscriber1.unsubscribe()
        mainPublishers[0].submit("AA")
        assertEquals(5, mainPublishers.sumOf { it.numSubscriptions })
        assertEquals(listOf("A", "B", "C", "D", "E"), value1)
        assertEquals(listOf("AA", "B", "C", "D", "E"), value2)

        subscriber2.unsubscribe()
        mainPublishers[1].submit("BB")
        assertEquals(0, mainPublishers.sumOf { it.numSubscriptions })
        assertEquals(listOf("A", "B", "C", "D", "E"), value1)
        assertEquals(listOf("AA", "B", "C", "D", "E"), value2)

        var value3 = emptyList<String>()
        val subscriber3 = Subscriber<List<String>> { value3 = it }
        mergedPublisher.subscribe(subscriber3)
        assertEquals(5, mainPublishers.sumOf { it.numSubscriptions })
        assertEquals(listOf("A", "B", "C", "D", "E"), value1)
        assertEquals(listOf("AA", "B", "C", "D", "E"), value2)
        assertEquals(listOf("AA", "BB", "C", "D", "E"), value3)
    }

    @Test
    fun testUnsubscribeCompose() {
        val subPublishers = listOf(
            Publisher("A"),
            Publisher("B"),
            Publisher("C"),
        )
        val mainPublisher = Publisher(0)
        val composedPublisher = mainPublisher.compose { subPublishers[it] }
        assertEquals(0, mainPublisher.numSubscriptions)
        assertEquals(listOf(0, 0, 0), subPublishers.map { it.numSubscriptions })

        var value1 = ""
        val subscriber1 = Subscriber<String> { value1 = it }
        composedPublisher.subscribe(subscriber1)
        assertEquals("A", value1)
        assertEquals(1, mainPublisher.numSubscriptions)
        assertEquals(listOf(1, 0, 0), subPublishers.map { it.numSubscriptions })

        subPublishers[0].submit("AA")
        var value2 = ""
        val subscriber2 = Subscriber<String> { value2 = it }
        composedPublisher.subscribe(subscriber2)
        assertEquals("AA", value1)
        assertEquals("AA", value2)
        assertEquals(1, mainPublisher.numSubscriptions)
        assertEquals(listOf(1, 0, 0), subPublishers.map { it.numSubscriptions })

        subscriber1.unsubscribe()
        mainPublisher.submit(1)
        assertEquals("AA", value1)
        assertEquals("B", value2)
        assertEquals(1, mainPublisher.numSubscriptions)
        assertEquals(listOf(0, 1, 0), subPublishers.map { it.numSubscriptions })

        subscriber2.unsubscribe()
        mainPublisher.submit(2)
        assertEquals("AA", value1)
        assertEquals("B", value2)
        assertEquals(0, mainPublisher.numSubscriptions)
        assertEquals(listOf(0, 0, 0), subPublishers.map { it.numSubscriptions })

        var value3 = ""
        val subscriber3 = Subscriber<String> { value3 = it }
        composedPublisher.subscribe(subscriber3)
        assertEquals("C", value3)
        assertEquals(1, mainPublisher.numSubscriptions)
        assertEquals(listOf(0, 0, 1), subPublishers.map { it.numSubscriptions })
    }

    @Test
    fun testCompletedPublisher() {
        val publisher = Publisher("A")
        var value = ""
        var completed = false
        publisher.subscribe(
            Subscriber({
                value = it
            }, {
                completed = true
            }),
        )
        assertEquals("A", value)
        assertFalse(completed)
        assertEquals(1, publisher.numSubscriptions)

        publisher.submit("B")
        assertEquals("B", value)
        assertFalse(completed)
        assertEquals(1, publisher.numSubscriptions)

        publisher.complete()
        assertEquals("B", value)
        assertTrue(completed)
        assertEquals(0, publisher.numSubscriptions)

        var value2 = ""
        var completed2 = false
        publisher.subscribe(
            Subscriber({
                value2 = it
            }, {
                completed2 = true
            }),
        )
        assertEquals("B", value2)
        assertTrue(completed2)
        assertEquals(0, publisher.numSubscriptions)

        assertThrows(IllegalStateException::class.java) { publisher.submit("C") }
    }

    @Test
    fun testCompletedCombinedPublisher() {
        val publishers = listOf(
            Publisher("A"),
            Publisher("B"),
            Publisher("C"),
        )
        val combinedPublisher = publishers.combine() as AbstractPublisher<List<String>>

        var value = emptyList<String>()
        var completed = false
        combinedPublisher.subscribe(
            Subscriber({
                value = it
            }, {
                completed = true
            }),
        )
        assertEquals(listOf("A", "B", "C"), value)
        assertFalse(completed)
        assertEquals(1, combinedPublisher.numSubscriptions)

        publishers[0].complete()
        assertFalse(completed)
        assertEquals(1, combinedPublisher.numSubscriptions)

        publishers[2].complete()
        assertFalse(completed)
        assertEquals(1, combinedPublisher.numSubscriptions)

        publishers[1].complete()
        assertTrue(completed)
        assertEquals(0, combinedPublisher.numSubscriptions)
    }

    @Test
    fun testCompletedComposedPublisher() {
        val publishers = listOf(
            Publisher("A"),
            Publisher("B"),
            Publisher("C"),
        )
        val topPublisher = Publisher(0)
        val composedPublisher = topPublisher.compose { publishers[it] } as AbstractPublisher<String>

        var value = ""
        var completed = false
        composedPublisher.subscribe(
            Subscriber({
                value = it
            }, {
                completed = true
            }),
        )
        assertEquals("A", value)
        assertFalse(completed)
        assertEquals(1, composedPublisher.numSubscriptions)

        publishers[0].complete()
        assertFalse(completed)
        assertEquals(1, composedPublisher.numSubscriptions)

        topPublisher.submit(1)
        assertEquals("B", value)
        assertFalse(completed)
        assertEquals(1, composedPublisher.numSubscriptions)

        publishers[1].submit("BB")
        assertEquals("BB", value)
        assertFalse(completed)
        assertEquals(1, composedPublisher.numSubscriptions)

        topPublisher.complete()
        assertFalse(completed)
        assertEquals(1, composedPublisher.numSubscriptions)

        publishers[1].submit("BBB")
        assertEquals("BBB", value)
        assertFalse(completed)
        assertEquals(1, composedPublisher.numSubscriptions)

        publishers[1].complete()
        assertTrue(completed)
        assertEquals(0, composedPublisher.numSubscriptions)
    }

    @Test
    fun testCompletedMappingPublisher() {
        val publisher = Publisher("A")
        val mappedPublisher = publisher.map { it.length } as AbstractPublisher<Int>

        var value = 0
        var completed = false
        mappedPublisher.subscribe(
            Subscriber({
                value = it
            }, {
                completed = true
            }),
        )
        assertEquals(1, value)
        assertFalse(completed)
        assertEquals(1, mappedPublisher.numSubscriptions)

        publisher.complete()
        assertTrue(completed)
        assertEquals(0, mappedPublisher.numSubscriptions)
    }

    @Test
    fun testCompletedMergedPublisher() {
        val left = Publisher("A")
        val right = Publisher("B")
        val merged1 = left.merge(right) { a, b -> a + b } as AbstractPublisher<String>
        val merged2 = right.merge(left) { a, b -> a + b } as AbstractPublisher<String>

        var value1 = ""
        var completed1 = false
        merged1.subscribe(
            Subscriber({
                value1 = it
            }, {
                completed1 = true
            }),
        )
        assertEquals("AB", value1)
        assertFalse(completed1)
        assertEquals(1, merged1.numSubscriptions)

        var value2 = ""
        var completed2 = false
        merged2.subscribe(
            Subscriber({
                value2 = it
            }, {
                completed2 = true
            }),
        )
        assertEquals("BA", value2)
        assertFalse(completed2)
        assertEquals(1, merged2.numSubscriptions)

        left.complete()
        assertFalse(completed1)
        assertEquals(1, merged1.numSubscriptions)
        assertFalse(completed2)
        assertEquals(1, merged2.numSubscriptions)

        right.complete()
        assertTrue(completed1)
        assertEquals(0, merged1.numSubscriptions)
        assertTrue(completed2)
        assertEquals(0, merged2.numSubscriptions)
    }

    @Test
    fun testTimePublisher() {
        val instant = Instant.now()
        val publisher = TimePublisher.forClock(Clock.fixed(instant, ZoneId.systemDefault()))
        var output: Instant? = null
        publisher.subscribe(Subscriber { output = it })
        assertEquals(instant, output)
    }

    @Test
    fun testIntPublisher() {
        val publisher = IntPublisher(0)
        var output: Int? = null
        publisher.subscribe(Subscriber { output = it })
        assertEquals(0, output)

        publisher.increment(10)
        assertEquals(10, output)

        publisher.increment(15)
        assertEquals(25, output)

        publisher.set(-5)
        assertEquals(-5, output)

        publisher.increment(5)
        assertEquals(0, output)
    }
}
