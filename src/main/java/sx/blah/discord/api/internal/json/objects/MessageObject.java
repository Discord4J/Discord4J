package sx.blah.discord.api.internal.json.objects;

public class MessageObject {
	public String id;
	public int type;
	public String channel_id;
	public UserObject author;
	public String content;
	public String timestamp;
	public String edited_timestamp;
	public boolean tts;
	public boolean mention_everyone;
	public UserObject[] mentions;
	public String[] mention_roles;
	public AttachmentObject[] attachments;
	public EmbedObject[] embeds;
	public String nonce;
	public boolean pinned;

	public static class AttachmentObject {
		public String id;
		public String filename;
		public int size;
		public String url;
		public String proxy_url;
		public int height;
		public int width;
	}

	public static class EmbedObject {
		public String title;
		public String type;
		public String description;
		public String url;
		public ThumbnailObject thumbnail;
		public ProviderObject provider;
	}

	public static class ThumbnailObject {
		public String url;
		public String proxy_url;
		public int height;
		public int width;
	}

	public static class ProviderObject {
		public String name;
		public String url;
	}
}
