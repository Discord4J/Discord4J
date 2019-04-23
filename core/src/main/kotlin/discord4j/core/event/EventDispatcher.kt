package discord4j.core.event

import discord4j.core.event.domain.Event
import discord4j.core.infinite
import kotlinx.coroutines.channels.ReceiveChannel
import kotlin.reflect.KClass

fun <T : Event> EventDispatcher.on(clazz: KClass<T>): ReceiveChannel<T> = on(clazz.java).infinite()
