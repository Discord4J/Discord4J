package sx.blah.discord.json;

/**
 * The generic response when either receiving, sending or editing messages.
 */
public class MessageResponse {
	
	/**
	 * Unique id. Has no real purpose and can be left null.
	 */
	public String nonce;
	
	/**
	 * The attachments linked in the message
	 */
	public AttachmentResponse[] attachments;
	
	/**
	 * Whether this message should use tts
	 */
	public boolean tts;
	
	/**
	 * Any embedded content in the message (note: this only gets added in a MESSAGE_UPDATE event
	 * FIXME: Needs to be able to handle all possible embed "types"
	 */
//	public EmbedResponse[] embeds;
	
	/**
	 * The timestamp for the message
	 */
	public String timestamp;
	
	/**
	 * Whether to @mention everyone
	 */
	public boolean mention_everyone;
	
	/**
	 * The message id
	 */
	public String id;
	
	/**
	 * The timestamp of when the message was last edited, this will be null if the message hasn't been edited
	 */
	public String edited_timestamp;
	
	/**
	 * The author of the message
	 */
	public UserResponse author;
	
	/**
	 * The content of the message
	 */
	public String content;
	
	/**
	 * The id for the channel this message belongs in
	 */
	public String channel_id;
	
	/**
	 * The users mentioned in the message
	 */
	public UserResponse[] mentions;
	
	/**
	 * A representation of a file attachment
	 */
	public static class AttachmentResponse {
		
		/**
		 * The name of the file
		 */
		public String filename;
		
		/**
		 * The size of the file in bytes
		 */
		public int size;
		
		/**
		 * The url for the thumbnail? FIXME
		 */
		public String proxy_url;
		
		/**
		 * The attachment id
		 */
		public String id;
		
		/**
		 * The url for the actual file attachment
		 */
		public String url;
	}
}
