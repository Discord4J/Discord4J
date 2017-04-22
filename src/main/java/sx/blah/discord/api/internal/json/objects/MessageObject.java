/*
 *     This file is part of Discord4J.
 *
 *     Discord4J is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     Discord4J is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */

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
	public Boolean pinned;
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
