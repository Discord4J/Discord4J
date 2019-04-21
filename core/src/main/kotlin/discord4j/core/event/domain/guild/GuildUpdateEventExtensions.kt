package discord4j.core.event.domain.guild

import discord4j.core.`object`.entity.Guild
import discord4j.core.event.domain.guild.GuildUpdateEvent
import discord4j.core.grab


fun GuildUpdateEvent.old(): Guild? = old.grab()
