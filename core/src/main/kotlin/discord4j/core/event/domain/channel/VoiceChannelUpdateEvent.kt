package discord4j.core.event.domain.channel

import discord4j.core.`object`.entity.VoiceChannel
import discord4j.core.grab


fun VoiceChannelUpdateEvent.old(): VoiceChannel? = old.grab()
