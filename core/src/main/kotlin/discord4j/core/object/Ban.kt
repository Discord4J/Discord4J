package discord4j.core.`object`


import discord4j.core.grab


/**
 * Gets the reason for the ban.
 *
 * @return The nullable reason for the ban.
 */
fun Ban.nullableReason(): String? = reason.grab()
