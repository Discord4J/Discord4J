package discord4j.core.event.domain

import discord4j.core.`object`.entity.Guild
import discord4j.core.`object`.entity.TextChannel
import discord4j.core.await


suspend fun WebhooksUpdateEvent.guild(): Guild = guild.await()
suspend fun WebhooksUpdateEvent.channel(): TextChannel = channel.await()
