package sx.blah.discord.api.internal.json.objects;

/**
 * Represents a message json object.
 */
public class MessageObject {
	/**
	 * The id of the message.
	 */
	public String id;
	/**
	 * The type of the message.
	 */
	public int type;
	/**
	 * The channel id for the channel this message was sent in.
	 */
	public String channel_id;
	/**
	 * The author of the message.
	 */
	public UserObject author;
	/**
	 * The content of the message.
	 */
	public String content;
	/**
	 * The timestamp of when the message was sent.
	 */
	public String timestamp;
	/**
	 * The timestamp of when the message was last edited.
	 */
	public String edited_timestamp;
	/**
	 * Whether the message should be read with tts.
	 */
	public boolean tts;
	/**
	 * Whether the message mentions everyone.
	 */
	public boolean mention_everyone;
	/**
	 * The users the message mentions.
	 */
	public UserObject[] mentions;
	/**
	 * The roles the message mentions.
	 */
	public String[] mention_roles;
	/**
	 * The attachments on the message.
	 */
	public AttachmentObject[] attachments;
	/**
	 * The embeds in the message.
	 */
	public EmbedObject[] embeds;
	/**
	 * The nonce of the message.
	 */
	public String nonce;
	/**
	 * Whether the message is pinned.
	 */
	public boolean pinned;
	/**
	 * The reactions on the message.
	 */
	public ReactionObject[] reactions;
	/**
	 * The id of the webhook that sent the message.
	 */
	public String webhook_id;

	/**
	 * Represents a json message attachment object.
	 */
	public static class AttachmentObject {
		/**
		 * The id of the attachment.
		 */
		public String id;
		/**
		 * The name of the attached file.
		 */
		public String filename;
		/**
		 * The size of the attached file.
		 */
		public int size;
		/**
		 * The url of the attached file.
		 */
		public String url;
		/**
		 * The proxy url of the attached file.
		 */
		public String proxy_url;
		/**
		 * The height of the attached file if it's an image.
		 */
		public int height;
		/**
		 * The width of the attached file if it's an image.
		 */
		public int width;
	}

	/**
	 * Represents a json reaction object.
	 */
	public static class ReactionObject {
		/**
		 * The number of this reaction on the message.
		 */
		public int count;
		/**
		 * Whether the self user has reacted with this reaction.
		 */
		public boolean me;
		/**
		 * The reaction emoji.
		 */
		public ReactionEmojiObject emoji;
	}
}
