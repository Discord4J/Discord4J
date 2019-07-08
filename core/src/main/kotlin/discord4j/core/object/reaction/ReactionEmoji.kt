package discord4j.core.`object`.reaction


import discord4j.core.grab

/**
 * Gets the [ReactionEmoji.Custom] that represents this emoji.
 *
 * @return The nullable [ReactionEmoji.Custom] that represents this emoji.
 */
fun ReactionEmoji.nullableCustom(): ReactionEmoji.Custom? = asCustomEmoji().grab()

/**
 * Gets the [ReactionEmoji.Unicode] that represents this emoji.
 *
 * @return The nullable [ReactionEmoji.Unicode] that represents this emoji.
 */
fun ReactionEmoji.nullableUnicode(): ReactionEmoji.Unicode? = asUnicodeEmoji().grab()
