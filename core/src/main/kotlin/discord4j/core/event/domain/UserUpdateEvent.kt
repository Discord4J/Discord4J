package discord4j.core.event.domain

import discord4j.core.`object`.entity.User
import discord4j.core.grab


fun UserUpdateEvent.nullableOld(): User? = old.grab()
