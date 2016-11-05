package sx.blah.discord.api.internal.json.event;

import sx.blah.discord.api.internal.json.objects.ReactionEmojiObject;

public class ReactionEventResponse {

	public String user_id;
	public String message_id;
	public ReactionEmojiObject emoji;
	public String channel_id;

}
