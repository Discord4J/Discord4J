package discord4j.core

import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactive.openSubscription
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*

internal suspend fun <T> Mono<T>.awaitNull(): T? = awaitFirstOrNull()
internal suspend fun <T> Mono<T>.await(): T = awaitSingle()
internal suspend fun <T> Flux<T>.await(): List<T> = collectList().await()
internal fun <T> Flux<T>.infinite(): ReceiveChannel<T> = openSubscription()
internal fun <T> Optional<T>.grab(): T? = orElse(null)
internal suspend fun Mono<Void>.unit(): Unit = awaitNull().let {}
internal fun OptionalInt.grab(): Int? = takeIf { isPresent }?.asInt
internal fun OptionalLong.grab(): Long? = takeIf { isPresent }?.asLong
