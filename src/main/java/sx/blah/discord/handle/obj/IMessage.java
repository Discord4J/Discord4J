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

package sx.blah.discord.handle.obj;

import com.vdurmont.emoji.Emoji;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.handle.impl.obj.ReactionEmoji;
import sx.blah.discord.util.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Represents a discord message.
 */
public interface IMessage extends IDiscordObject<IMessage> {

	/**
	 * The maximum length of a discord message.
	 */
	int MAX_MESSAGE_LENGTH = 2000;

	/**
	 * Gets the string content of the message.
	 *
	 * @return The content of the message
	 */
	String getContent();

	/**
	 * Gets the channel that this message belongs to.
	 *
	 * @return The channel.
	 */
	IChannel getChannel();

	/**
	 * Gets the user who authored this message.
	 *
	 * @return The author.
	 */
	IUser getAuthor();

	/**
	 * Gets the timestamp for when this message was sent/edited.
	 *
	 * @return The timestamp.
	 */
	LocalDateTime getTimestamp();

	/**
	 * Gets the users mentioned in this message.
	 *
	 * @return The users mentioned.
	 */
	List<IUser> getMentions();

	/**
	 * Gets the roles mentioned in this message.
	 *
	 * @return The roles mentioned.
	 */
	List<IRole> getRoleMentions();

	/**
	 * Gets the channels mentioned in this message.
	 *
	 * @return The channels mentioned.
	 */
	List<IChannel> getChannelMentions();

	/**
	 * Gets the attachments in this message.
	 *
	 * @return The attachments.
	 */
	List<Attachment> getAttachments();

	/**
	 * Gets the embeds in this message.
	 *
	 * @return The embeds.
	 * @deprecated Use {@link #getEmbeds()} instead.
	 */
	@Deprecated
	List<IEmbed> getEmbedded();

	/**
	 * Gets the embeds in this message.
	 *
	 * @return The embeds.
	 */
	List<IEmbed> getEmbeds();

	/**
	 * Adds an "@mention," to the author of the referenced Message
	 * object before your content
	 *
	 * @param content Message to send.
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	IMessage reply(String content);

	/**
	 * Adds an "@mention," to the author of the referenced Message
	 * object before your content.
	 *
	 * @param content Message content to send.
	 * @param embed The embed object
	 * @return The message object representing the sent message
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage reply(String content, EmbedObject embed);

	/**
	 * Edits the message. NOTE: Discord only supports editing YOUR OWN messages!
	 *
	 * @param content The new content for the message to contain.
	 * @return The new message (this).
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 */
	IMessage edit(String content);

	/**
	 * Edits the message with an embed object. NOTE: Discord only supports editing YOUR OWN messages!
	 *
	 * @param content The new content for the message to contain.
	 * @param embed The embed object
	 * @return The new message (this).
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage edit(String content, EmbedObject embed);

	/**
	 * Edits the message with only an embed object. NOTE: Discord only supports editing YOUR OWN messages!
	 *
	 * @param embed The embed object
	 * @return The new message (this).
	 * @throws MissingPermissionsException
	 * @throws DiscordException
	 *
	 * @see EmbedBuilder
	 */
	IMessage edit(EmbedObject embed);

	/**
	 * Returns whether this message mentions everyone through @everyone.
	 *
	 * @return True if it mentions everyone, false if otherwise.
	 */
	boolean mentionsEveryone();

	/**
	 * Returns whether this message mentions the online users through @here.
	 *
	 * @return True if it mentions all the online users, false if otherwise.
	 */
	boolean mentionsHere();

	/**
	 * Deletes the message.
	 *
	 * @throws MissingPermissionsException
	 * @throws RateLimitException
	 * @throws DiscordException
	 */
	void delete();

	/**
	 * Gets the time that this message was last edited.
	 *
	 * @return The edited timestamp.
	 */
	Optional<LocalDateTime> getEditedTimestamp();

	/**
	 * Returns whether this message has been pinned on its channel or not.
	 *
	 * @return True if pinned, false is otherwise.
	 */
	boolean isPinned();

	/**
	 * Gets the guild this message is from.
	 *
	 * @return The guild.
	 */
	IGuild getGuild();

	/**
	 * Gets formatted content. All user, channel, and role mentions are converted to a readable form.
	 *
	 * @return The formatted content.
	 */
	String getFormattedContent();

	/**
	 * Gets the reactions for this message.
	 *
	 * @return A list of reactions
	 */
	List<IReaction> getReactions();

	/**
	 * Gets a reaction by a custom guild emoji.
	 *
	 * @param emoji The emoji of the reaction to find.
	 * @return The reaction with the provided emoji.
	 *
	 * @deprecated Use {@link #getReactionByEmoji(IEmoji)} instead.
	 */
	@Deprecated
	IReaction getReactionByIEmoji(IEmoji emoji);

	/**
	 * Gets a reaction by its custom guild emoji.
	 *
	 * @param emoji The emoji of the reaction to find.
	 * @return The reaction with the provided emoji.
	 */
	IReaction getReactionByEmoji(IEmoji emoji);

	/**
	 * Gets a reaction by the ID of its guild emoji.
	 *
	 * @param id The ID of the emoji of the reaction to find.
	 * @return The reaction with the provided ID.
	 */
	IReaction getReactionByID(long id);

	/**
	 * Gets a reaction by its unicode character emoji.
	 *
	 * @param unicode The unicode character.
	 * @return The reaction with the provided emoji.
	 */
	IReaction getReactionByUnicode(Emoji unicode);

	/**
	 * Gets a reaction by its unicode character emoji.
	 *
	 * @param unicode The unicode character.
	 * @return The reaction with the provided emoji.
	 */
	IReaction getReactionByUnicode(String unicode);

	/**
	 * Gets a reaction by its unicode character emoji.
	 *
	 * @param name The unicode character.
	 * @return The reaction with the provided emoji.
	 *
	 * @deprecated Use {@link #getReactionByUnicode(String)} instead.
	 */
	@Deprecated
	IReaction getReactionByName(String name);

	/**
	 * Gets a reaction by its emoji.
	 *
	 * @param emoji The emoji of the reaction to find.
	 * @return The reaction with the provided emoji.
	 */
	IReaction getReactionByEmoji(ReactionEmoji emoji);

	/**
	 * Adds a reaction to the message.
	 * Gets the emoji to react with from the passed reaction.
	 *
	 * @param reaction The reaction to get the emoji to react with from.
	 */
	void addReaction(IReaction reaction);

	/**
	 * Adds a reaction to the message.
	 *
	 * @param emoji The emoji to react with.
	 */
	void addReaction(IEmoji emoji);

	/**
	 * Adds a reaction to the message.
	 *
	 * @param emoji The emoji to react with.
	 */
	void addReaction(Emoji emoji);

	/**
	 * Adds a reaction to the message.
	 * This method accepts a string in three forms:
	 * <ul>
	 *     <li>A unicode emoji alias (e.g. ":thinking:")</li>
	 *     <li>A unicode emoji character (e.g. "\u1F914")</li>
	 *     <li>A custom guild emoji mention (e.g. "<:rainblob:304759070680809474>")</li>
	 * </ul>
	 *
	 * @throws IllegalArgumentException If the passed emoji does not match any of the allowed formats or it matches the
	 * format of a unicode alias and a corresponding emoji could not be found.
	 *
	 * @param emoji The emoji to react with.
	 *
	 * @deprecated Each form of accepted parameter to this method has its own method.
	 * Use {@link #addReaction(ReactionEmoji)} or {@link #addReaction(Emoji)} instead.
	 */
	@Deprecated
	void addReaction(String emoji);

	/**
	 * Adds a reaction to the message.
	 *
	 * @param emoji The emoji to react with.
	 */
	void addReaction(ReactionEmoji emoji);

	/**
	 * Removes a reaction from the message.
	 *
	 * @param reaction The reaction to remove.
	 *
	 * @deprecated This is an overload for {@link #removeReaction(IUser, IReaction)} with
	 * {@link IDiscordClient#getOurUser()}. Use that instead.
	 */
	@Deprecated
	void removeReaction(IReaction reaction);

	/**
	 * Removes a reaction from the message for the given user.
	 *
	 * @param user The user to remove the reaction for.
	 * @param reaction The reaction to remove.
	 */
	void removeReaction(IUser user, IReaction reaction);

	/**
	 * Removes a reaction from the message for the given user.
	 *
	 * @param user The user to remove the reaction for.
	 * @param emoji The emoji for the reaction to be removed.
	 */
	void removeReaction(IUser user, ReactionEmoji emoji);

	/**
	 * Removes a reaction from the message for the given user.
	 *
	 * @param user The user to remove the reaction for.
	 * @param emoji The emoji for the reaction to be removed.
	 */
	void removeReaction(IUser user, IEmoji emoji);

	/**
	 * Removes a reaction from the message for the given user.
	 *
	 * @param user The user to remove the reaction for.
	 * @param emoji The emoji for the reaction to be removed.
	 */
	void removeReaction(IUser user, Emoji emoji);

	/**
	 * Removes a reaction from the message for the given user.
	 *
	 * @param user The user to remove the reaction for.
	 * @param emoji The emoji for the reaction to be removed.
	 */
	void removeReaction(IUser user, String emoji);

	/**
	 * Removes all of the reactions on the message.
	 */
	void removeAllReactions();

	/**
	 * This creates a new {@link MessageTokenizer} instance with this message instance.
	 *
	 * @return A new tokenizer.
	 */
	MessageTokenizer tokenize();

	/**
	 * Checks to see is this message deleted.
	 *
	 * @return True if this message is deleted
	 */
	boolean isDeleted();

	/**
	 * Gets the ID of the webhook that sent this message. May be null.
	 *
	 * @return The webhook ID.
	 * @deprecated Use {@link #getWebhookLongID()} instead
	 */
	@Deprecated
	default String getWebhookID() {
		return Long.toUnsignedString(getWebhookLongID());
	}

	/**
	 * Gets the ID of the webhook that sent this message. May be null.
	 *
	 * @return The webhook ID.
	 */
	long getWebhookLongID();

	/**
	 * Represents an attachment included in the message.
	 */
	class Attachment implements IIDLinkedObject {

		/**
		 * The file name of the attachment.
		 */
		protected final String filename;

		/**
		 * The size, in bytes of the attachment.
		 */
		protected final int filesize;

		/**
		 * The attachment id.
		 */
		protected final long id;

		/**
		 * The download link for the attachment.
		 */
		protected final String url;

		public Attachment(String filename, int filesize, long id, String url) {
			this.filename = filename;
			this.filesize = filesize;
			this.id = id;
			this.url = url;
		}

		/**
		 * Gets the file name for the attachment.
		 *
		 * @return The file name of the attachment.
		 */
		public String getFilename() {
			return filename;
		}

		/**
		 * Gets the size of the attachment.
		 *
		 * @return The size, in bytes of the attachment.
		 */
		public int getFilesize() {
			return filesize;
		}

		/**
		 * Gets the id of the attachment.
		 *
		 * @return The attachment id.
		 * @deprecated Use {@link #getLongID()} or {@link #getStringID()} instead
		 */
		@Deprecated
		public String getId() {
			return getStringID();
		}

		/**
		 * Gets the id of the attachment.
		 *
		 * @return The attachment id.
		 */
		@Override
		public long getLongID() {
			return id;
		}

		/**
		 * Gets the direct link to the attachment.
		 *
		 * @return The download link for the attachment.
		 */
		public String getUrl() {
			return url;
		}
	}

}
