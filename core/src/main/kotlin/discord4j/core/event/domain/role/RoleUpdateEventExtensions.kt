package discord4j.core.event.domain.role

import discord4j.core.`object`.entity.Role
import discord4j.core.event.domain.role.RoleUpdateEvent
import discord4j.core.grab


fun RoleUpdateEvent.old(): Role? = old.grab()
