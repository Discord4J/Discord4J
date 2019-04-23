package discord4j.core.`object`.presence


import discord4j.core.grab

/**
 * Grabs the activity for this presence.
 *
 * @return The nullable [Activity].
 */
fun Presence.activity(): Activity? = activity.grab()
