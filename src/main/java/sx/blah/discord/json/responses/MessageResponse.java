package sx.blah.discord.json.responses;

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
	 * Any embedded content in the message (note: this only gets added in a MESSAGE_UPDATE event) TODO: Actually implement into java somehow
	 */
	public EmbedResponse[] embeds;
	
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
	 * Represents embeded data
	 */
	public static class EmbedResponse {
		
		/**
		 * The author of the embed link, nullable.
		 */
		public AuthorResponse author;
		
		/**
		 * A description of the embed link, nullable.
		 */
		public String description;
		
		/**
		 * The embed link provider, nullable.
		 */
		public ProviderResponse provider;
		
		/**
		 * The thumbnail of the embed link, nullable.
		 */
		public ThumbnailResponse thumbnail;
		
		/**
		 * The title of the embed link, nullable.
		 */
		public String title;
		
		/**
		 * The type of embed link, nullable.
		 * Ex. "article",null,"video","html","link","image","xivdb","rich","json","text","photo", etc.
		 */
		public String type;
		
		/**
		 * The url of the embed link.
		 */
		public String url;
		
		/**
		 * The video for the embed link, nullable.
		 */
		public VideoResponse video;
		
		/**
		 * Represents an author object.
		 */
		public static class AuthorResponse {
			
			/**
			 * The name of the author.
			 */
			public String name;
			
			/**
			 * The url of the author, nullable.
			 */
			public String url;
		}
		
		/**
		 * Represents a provider object.
		 */
		public static class ProviderResponse {
			
			/**
			 * The name of the data provider.
			 */
			public String name;
			
			/**
			 * The provider's url, nullable.
			 */
			public String url;
		}
		
		/**
		 * Represents a thumbnail object.
		 */
		public static class ThumbnailResponse {
			
			/**
			 * The height, in pixels of the thumbnail.
			 */
			public int height;
			
			/**
			 * The width, in pixels of the thumbnail.
			 */
			public int width;
			
			/**
			 * The discord hosted thumbnail image.
			 */
			public String proxy_url;
			
			/**
			 * The direct link to the image.
			 */
			public String url;
		}
		
		//FIXME
		public static class VideoResponse {
			
		}
	}
	
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
		 * The url for used for the embed.
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
