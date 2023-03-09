package com.joecollins.pubsub

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Flow

fun <T> T.asOneTimePublisher(): Flow.Publisher<T> {
    val publisher = Publisher<T>()
    publisher.submit(this)
    return publisher
}

fun <T> CompletableFuture<T>.asPublisher(): Flow.Publisher<T> {
    val publisher = Publisher<T>()
    this.exceptionally { it.printStackTrace(); throw it; }
        .thenAccept { publisher.submit(it) }
    return publisher
}

fun <T> CompletableFuture<T>.asPublisher(init: T): Flow.Publisher<T> {
    val publisher = Publisher(init)
    this.exceptionally { it.printStackTrace(); throw it; }
        .thenAccept { publisher.submit(it) }
    return publisher
}

fun <T, R> Flow.Publisher<T>.map(func: (T) -> R): Flow.Publisher<R> {
    return MappingPublisher(this, func)
}

fun <T, R> Flow.Publisher<T>.map(func: (T) -> R, executor: ExecutorService): Flow.Publisher<R> {
    return AsyncMappingPublisher(this, func, executor)
}

fun <T, R> Flow.Publisher<out List<T>>.mapElements(func: (T) -> R): Flow.Publisher<List<R>> = map { it.map(func) }

fun <K, V, R> Flow.Publisher<out Map<K, V>>.mapKeys(func: (K) -> R): Flow.Publisher<Map<R, V>> = map { map -> map.mapKeys { func(it.key) } }

fun <T, U, R> Flow.Publisher<T>.merge(other: Flow.Publisher<out U>, func: (T, U) -> R): Flow.Publisher<R> {
    return MergedPublisher(this, other, func)
}

fun <T, R> List<Flow.Publisher<out T>>.mapReduce(identity: R, onValueAdd: (R, T) -> R, onValueRemove: (R, T) -> R): Flow.Publisher<R> {
    return MapReducePublisher(this, identity, onValueAdd, onValueRemove)
}

fun <T> List<Flow.Publisher<out T>>.combine(): Flow.Publisher<List<T>> {
    return CombinedPublisher(this)
}

fun <T, R> Flow.Publisher<T>.compose(func: (T) -> Flow.Publisher<out R>): Flow.Publisher<R> {
    return ComposedPublisher(this, func)
}
