package discord4j.core.event.domain.role

import discord4j.core.`object`.entity.Role
import discord4j.core.grab


fun RoleUpdateEvent.nullableOld(): Role? = old.grab()
