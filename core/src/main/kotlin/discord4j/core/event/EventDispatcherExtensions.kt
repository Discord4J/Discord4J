package discord4j.core.event

import discord4j.core.event.EventDispatcher
import discord4j.core.event.domain.Event
import kotlinx.coroutines.channels.ReceiveChannel
import discord4j.core.infinite
import kotlin.reflect.KClass

fun <T : Event> EventDispatcher.on(clazz: KClass<T>): ReceiveChannel<T> = on(clazz.java).infinite()
