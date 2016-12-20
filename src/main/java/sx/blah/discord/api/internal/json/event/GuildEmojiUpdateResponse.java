package sx.blah.discord.api.internal.json.event;

import sx.blah.discord.api.internal.json.objects.EmojiObject;

/**
 * The response when the emoji list for a guild updates.
 */
public class GuildEmojiUpdateResponse {

	/**
	 * The guild involved.
	 */
	public String guild_id;

	/**
	 * The emoji objects.
	 */
	public EmojiObject[] emojis;

}
