package discord4j.core.event.domain.channel

import discord4j.core.`object`.entity.TextChannel
import discord4j.core.grab


fun TextChannelUpdateEvent.old(): TextChannel? = old.grab()
