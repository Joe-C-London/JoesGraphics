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
    fun testUnsubscribeMapReducePublisher() {
        val mainPublishers = listOf(
            Publisher("A"),
            Publisher("B"),
            Publisher("C"),
            Publisher("D"),
            Publisher("E"),
        )
        val mergedPublisher = mainPublishers.mapReduce(
            0,
            { a, b -> a + b.length },
            { a, b -> a - b.length },
        )
        assertEquals(0, mainPublishers.sumOf { it.numSubscriptions })

        var value1 = 0
        val subscriber1 = Subscriber<Int> { value1 = it }
        mergedPublisher.subscribe(subscriber1)
        assertEquals(5, mainPublishers.sumOf { it.numSubscriptions })
        assertEquals(5, value1)

        var value2 = 0
        val subscriber2 = Subscriber<Int> { value2 = it }
        mergedPublisher.subscribe(subscriber2)
        assertEquals(5, mainPublishers.sumOf { it.numSubscriptions })
        assertEquals(5, value2)

        subscriber1.unsubscribe()
        mainPublishers[0].submit("AA")
        assertEquals(5, mainPublishers.sumOf { it.numSubscriptions })
        assertEquals(5, value1)
        assertEquals(6, value2)

        subscriber2.unsubscribe()
        mainPublishers[1].submit("BB")
        assertEquals(0, mainPublishers.sumOf { it.numSubscriptions })
        assertEquals(5, value1)
        assertEquals(6, value2)

        var value3 = 0
        val subscriber3 = Subscriber<Int> { value3 = it }
        mergedPublisher.subscribe(subscriber3)
        assertEquals(5, mainPublishers.sumOf { it.numSubscriptions })
        assertEquals(5, value1)
        assertEquals(6, value2)
        assertEquals(7, value3)
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
}
