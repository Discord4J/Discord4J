package discord4j.core.event.domain.guild

import discord4j.core.`object`.entity.Guild
import discord4j.core.event.domain.guild.GuildDeleteEvent
import discord4j.core.grab


fun GuildDeleteEvent.guild(): Guild? = guild.grab()
