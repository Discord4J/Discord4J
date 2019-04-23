package discord4j.core.event.domain.channel

import discord4j.core.`object`.entity.Category
import discord4j.core.grab


fun CategoryUpdateEvent.old(): Category? = old.grab()
