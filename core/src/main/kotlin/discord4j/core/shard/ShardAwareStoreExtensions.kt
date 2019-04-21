package discord4j.core.shard


import discord4j.core.shard.ShardAwareStore
import discord4j.core.await
import discord4j.core.awaitNull
import discord4j.core.unit
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import reactor.util.function.Tuple2
import java.io.Serializable

suspend fun <K : Comparable<K>, V : Serializable> ShardAwareStore<K, V>.awaitSave(key: K, value: V): Unit =
    save(key, value).unit()

suspend fun <K : Comparable<K>, V : Serializable> ShardAwareStore<K, V>.awaitSave(
    entryStream: Publisher<Tuple2<K, V>>
): Unit = save(entryStream).unit()

suspend fun <K : Comparable<K>, V : Serializable> ShardAwareStore<K, V>.awaitFind(id: K): V? = find(id).awaitNull()

suspend fun <K : Comparable<K>, V : Serializable> ShardAwareStore<K, V>.awaitFindInRange(start: K, end: K): List<V> =
    findInRange(start, end).await()

suspend fun <K : Comparable<K>, V : Serializable> ShardAwareStore<K, V>.awaitCount(): Long = count().await()

suspend fun <K : Comparable<K>, V : Serializable> ShardAwareStore<K, V>.awaitDelete(ids: List<K>): Unit =
    delete(Flux.fromIterable(ids)).unit()

suspend fun <K : Comparable<K>, V : Serializable> ShardAwareStore<K, V>.awaiDeleteInRange(start: K, end: K): Unit =
    deleteInRange(start, end).unit()

suspend fun <K : Comparable<K>, V : Serializable> ShardAwareStore<K, V>.awaitDeleteAll(): Unit = deleteAll().unit()

suspend fun <K : Comparable<K>, V : Serializable> ShardAwareStore<K, V>.awaitKeys(): List<K> = keys().await()

suspend fun <K : Comparable<K>, V : Serializable> ShardAwareStore<K, V>.awaitValues(): List<V> = values().await()

suspend fun <K : Comparable<K>, V : Serializable> ShardAwareStore<K, V>.awaitInvalidate(): Unit = invalidate().unit()
