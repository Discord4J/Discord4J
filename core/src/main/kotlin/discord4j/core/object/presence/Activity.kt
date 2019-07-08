package discord4j.core.`object`.presence

import discord4j.core.`object`.util.Snowflake
import discord4j.core.grab
import java.time.Instant


/**
 * Grabs the streaming URL.
 *
 * @return The nullable streaming URL.
 */
fun Activity.nullableStreamingUrl(): String? = streamingUrl.grab()

/**
 * Grabs the start time.
 *
 * @return The nullable start time.
 */
fun Activity.nullableStart(): Instant? = start.grab()

/**
 * Grabs the end time.
 *
 * @return The nullable end time.
 */
fun Activity.nullableEnd(): Instant? = end.grab()

/**
 * Grabs the application ID.
 *
 * @return The nullable application ID.
 */
fun Activity.nullableApplicationId(): Snowflake? = applicationId.grab()

/**
 * Grabs the details of what the player is doing.
 *
 * @return The nullable detail of what the player is doing.
 */
fun Activity.nullableDetails(): String? = details.grab()

/**
 * Grabs the party status.
 *
 * @return The nullable party status.
 */
fun Activity.nullableState(): String? = state.grab()

/**
 * Grabs the party ID.
 *
 * @return The nullable party ID.
 */
fun Activity.nullablePartyId(): String? = partyId.grab()

/**
 * Grabs the ID of the large image.
 *
 * @return The nullable ID of the large image.
 */
fun Activity.nullableLargeImageId(): String? = largeImageId.grab()

/**
 * Grabs the text of the large image.
 *
 * @return The nullable text of the large image.
 */
fun Activity.nullableLargeText(): String? = largeText.grab()

/**
 * Grabs the ID of the small image.
 *
 * @return The nullable ID of the small image.
 */
fun Activity.nullableSmallImageId(): String? = smallImageId.grab()

/**
 * Grabs the text of the small image.
 *
 * @return The nullable text of the small image.
 */
fun Activity.nullableSmallText(): String? = smallText.grab()
