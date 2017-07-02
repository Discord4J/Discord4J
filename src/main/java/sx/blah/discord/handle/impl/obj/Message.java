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

package sx.blah.discord.handle.impl.obj;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.IShard;
import sx.blah.discord.api.internal.DiscordClientImpl;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.api.internal.json.objects.EmbedObject;
import sx.blah.discord.api.internal.json.requests.MessageRequest;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.*;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Message implements IMessage {

	/**
	 * The ID of the message. Used for message updating.
	 */
	protected final long id;

	/**
	 * The actual message (what you see
	 * on your screen, the content).
	 */
	protected volatile String content;

	/**
	 * The User who sent the message.
	 */
	protected final User author;

	/**
	 * The ID of the channel the message was sent in.
	 */
	protected final Channel channel;

	/**
	 * The time the message was received.
	 */
	protected volatile LocalDateTime timestamp;

	/**
	 * The time (if it exists) that the message was edited.
	 */
	protected volatile LocalDateTime editedTimestamp;

	/**
	 * The list of users mentioned by this message.
	 */
	protected volatile List<Long> mentions;

	/**
	 * The list of roles mentioned by this message.
	 */
	protected volatile List<Long> roleMentions;

	/**
	 * The attachments, if any, on the message.
	 */
	protected volatile List<Attachment> attachments;

	/**
	 * The embeds, if any, on the message.
	 */
	protected volatile List<Embed> embeds;

	/**
	 * Whether the message mentions everyone.
	 */
	protected volatile boolean mentionsEveryone;

	/**
	 * Whether the message mentions all online users.
	 */
	protected volatile boolean mentionsHere;

	/**
	 * Whether the everyone mention is valid (has permission).
	 */
	protected volatile boolean everyoneMentionIsValid;

	/**
	 * Whether the message has been pinned to its channel or not.
	 */
	protected volatile boolean isPinned;

	/**
	 * The list of channels mentioned by this message.
	 */
	protected final List<IChannel> channelMentions;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	/**
	 * The formatted content. It's cached until the content changes, and this gets re-set to null. This is
	 * only made when a call to getFormattedContent is made.
	 */
	protected volatile String formattedContent = null;

	/**
	 * The list of reactions
	 */
	protected volatile List<IReaction> reactions;

	/**
	 * The ID of the webhook that sent this message
	 */
	protected final long webhookID;

	/**
	 * The pattern for matching channel mentions.
	 */
	private static final Pattern CHANNEL_PATTERN = Pattern.compile("<#([0-9]+)>");

	/**
	 * Cached value of isDeleted()
	 */
	private volatile boolean deleted = false;

	public Message(IDiscordClient client, long id, String content, IUser user, IChannel channel,
				   LocalDateTime timestamp, LocalDateTime editedTimestamp, boolean mentionsEveryone,
				   List<Long> mentions, List<Long> roleMentions, List<Attachment> attachments,
				   boolean pinned, List<Embed> embeds, List<IReaction> reactions, long webhookID) {
		this.client = client;
		this.id = id;
		setContent(content);
		this.author = (User) user;
		this.channel = (Channel) channel;
		this.timestamp = timestamp;
		this.editedTimestamp = editedTimestamp;
		this.mentions = mentions;
		this.roleMentions = roleMentions;
		this.attachments = attachments;
		this.isPinned = pinned;
		this.channelMentions = new ArrayList<>();
		this.embeds = embeds;
		this.everyoneMentionIsValid = mentionsEveryone;
		this.reactions = reactions;
		this.webhookID = webhookID;

		setChannelMentions();
	}

	@Override
	public String getContent() {
		return content;
	}

	/**
	 * Sets the CACHED content of the message.
	 *
	 * @param content The new message content.
	 */
	public void setContent(String content) {
		this.content = content;
		this.formattedContent = null; // Force re-update later

		if (content != null) {
			this.mentionsEveryone = content.contains("@everyone");
			this.mentionsHere = content.contains("@here");
		}
	}

	/**
	 * Sets the CACHED mentions in this message.
	 *
	 * @param mentions     The new user mentions.
	 * @param roleMentions The new role mentions.
	 */
	public void setMentions(List<Long> mentions, List<Long> roleMentions) {
		this.mentions = mentions;
		this.roleMentions = roleMentions;
	}

	/**
	 * Populates the channel mention list.
	 */
	public void setChannelMentions() {
		if (content != null) {
			channelMentions.clear();
			Matcher matcher = CHANNEL_PATTERN.matcher(content);

			while (matcher.find()) {
				long mentionedID = Long.parseUnsignedLong(matcher.group(1));
				IChannel mentioned = client.getChannelByID(mentionedID);

				if (mentioned != null) {
					channelMentions.add(mentioned);
				}
			}
		}
	}

	/**
	 * Sets the CACHED attachments in this message.
	 *
	 * @param attachments The new attachements.
	 */
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	/**
	 * Sets the CACHED embed list in this message.
	 *
	 * @param embeds The new embeds.
	 */
	public void setEmbeds(List<Embed> embeds) {
		this.embeds = embeds;
	}

	@Override
	public IChannel getChannel() {
		return channel;
	}

	@Override
	public IUser getAuthor() {
		return author;
	}

	@Override
	public long getLongID() {
		return id;
	}

	/**
	 * Sets the CACHED version of the message timestamp.
	 *
	 * @param timestamp The timestamp.
	 */
	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	@Override
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	@Override
	public List<IUser> getMentions() {
		if (mentionsEveryone)
			return channel.getGuild().getUsers();
		return mentions.stream()
				.map(client::getUserByID)
				.collect(Collectors.toList());
	}

	@Override
	public List<IRole> getRoleMentions() {
		return roleMentions.stream()
				.map(m -> getGuild().getRoleByID(m))
				.collect(Collectors.toList());
	}

	@Override
	public List<IChannel> getChannelMentions() {
		return channelMentions;
	}

	@Override
	public List<Attachment> getAttachments() {
		return attachments;
	}

	@Override
	@Deprecated
	public List<IEmbed> getEmbedded() {
		return getEmbeds();
	}

	@Override
	public List<IEmbed> getEmbeds() {
		List<IEmbed> copy = new ArrayList<>(embeds.size());
		copy.addAll(embeds);
		return copy;
	}

	@Override
	public IMessage reply(String content) {
		return reply(content, null);
	}

	@Override
	public IMessage reply(String content, EmbedObject embed) {
		return getChannel().sendMessage(String.format("%s, %s", this.getAuthor(), content), embed, false);
	}

	@Override
	public IMessage edit(String content) {
		return edit(content, null);
	}

	@Override
	public IMessage edit(EmbedObject embed) {
		return edit(null, embed);
	}

	@Override
	public IMessage edit(String content, EmbedObject embed) {
		getShard().checkReady("edit message");
		if (!this.getAuthor().equals(client.getOurUser()))
			throw new MissingPermissionsException("Cannot edit other users' messages!", EnumSet.noneOf(Permissions.class));
		if (isDeleted())
			throw new DiscordException("Cannot edit deleted messages!");

		if (embed != null) {
			PermissionUtils.requirePermissions(getChannel(), client.getOurUser(), Permissions.EMBED_LINKS);
		}

		((DiscordClientImpl) client).REQUESTS.PATCH.makeRequest(
				DiscordEndpoints.CHANNELS + channel.getStringID() + "/messages/" + id,
				new MessageRequest(content, embed, false));

		return this;
	}

	/**
	 * Gets the raw list of mentioned user ids.
	 *
	 * @return Mentioned user list.
	 * @deprecated Use {@link #getRawMentionsLong()} instead
	 */
	@Deprecated
	public List<String> getRawMentions() {
		return getRawMentionsLong().stream().map(Long::toUnsignedString).collect(Collectors.toList());
	}

	/**
	 * Gets the raw list of mentioned user ids.
	 *
	 * @return Mentioned user list.
	 */
	public List<Long> getRawMentionsLong() {
		return mentions;
	}

	/**
	 * Gets the raw list of mentioned role ids.
	 *
	 * @return Mentioned role list.
	 * @deprecated Use {@link #getRawRoleMentionsLong()} instead
	 */
	@Deprecated
	public List<String> getRawRoleMentions() {
		return getRawRoleMentionsLong().stream().map(Long::toUnsignedString).collect(Collectors.toList());
	}

	/**
	 * Gets the raw list of mentioned role ids.
	 *
	 * @return Mentioned role list.
	 */
	public List<Long> getRawRoleMentionsLong() {
		return roleMentions;
	}

	@Override
	public boolean mentionsEveryone() {
		return everyoneMentionIsValid && mentionsEveryone;
	}

	@Override
	public boolean mentionsHere() {
		return everyoneMentionIsValid && mentionsHere;
	}

	/**
	 * CACHES whether the message mentions everyone.
	 *
	 * @param mentionsEveryone True to mention everyone false if otherwise.
	 */
	public void setMentionsEveryone(boolean mentionsEveryone) {
		this.mentionsEveryone = mentionsEveryone;
	}

	@Override
	public void delete() {
		getShard().checkReady("delete message");
		if (!getAuthor().equals(client.getOurUser())) {
			if (channel.isPrivate())
				throw new DiscordException("Cannot delete the other person's message in a private channel!");

			PermissionUtils.requirePermissions(getChannel(), client.getOurUser(), Permissions.MANAGE_MESSAGES);
		}

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(DiscordEndpoints.CHANNELS + channel.getStringID() + "/messages/" + id);
	}

	@Override
	public Optional<LocalDateTime> getEditedTimestamp() {
		return Optional.ofNullable(editedTimestamp);
	}

	/**
	 * This sets the CACHED edited timestamp.
	 *
	 * @param editedTimestamp The new timestamp.
	 */
	public void setEditedTimestamp(LocalDateTime editedTimestamp) {
		this.editedTimestamp = editedTimestamp;
	}

	@Override
	public boolean isPinned() {
		return isPinned;
	}

	/**
	 * This sets the CACHED isPinned value.
	 *
	 * @param pinned Whether the message is pinned.
	 */
	public void setPinned(boolean pinned) {
		isPinned = pinned;
	}

	@Override
	public IMessage copy() {
		return new Message(client, id, content, author, channel, timestamp, editedTimestamp, everyoneMentionIsValid,
				mentions, roleMentions, attachments, isPinned, embeds, reactions, webhookID);
	}

	@Override
	public IGuild getGuild() {
		return getChannel().getGuild();
	}

	@Override
	public String getFormattedContent() {
		if (content == null)
			return null;

		if (formattedContent == null) {
			String currentContent = content;

			for (IUser u : getMentions())
				currentContent = currentContent.replace(u.mention(false), "@" + u.getName())
						.replace(u.mention(true), "@" + u.getDisplayName(getGuild()));

			for (IChannel ch : getChannelMentions())
				currentContent = currentContent.replace(ch.mention(), "#" + ch.getName());

			for (IRole r : getRoleMentions())
				currentContent = currentContent.replace(r.mention(), "@" + r.getName());

			formattedContent = currentContent;
		}

		return formattedContent;
	}

	public void setReactions(List<IReaction> reactions) {
		this.reactions = reactions;
	}

	@Override
	public List<IReaction> getReactions() {
		return reactions;
	}

	@Override
	public IReaction getReactionByIEmoji(IEmoji emoji) {
		if (emoji == null)
			return null;
		return reactions.stream().filter(r -> r != null && r.isCustomEmoji() && r.getCustomEmoji() != null &&
				r.getCustomEmoji().equals(emoji)).findFirst().orElse(null);
	}

	@Override
	public IReaction getReactionByUnicode(String name) {
		if (name == null)
			return null;
		return reactions.stream().filter(r -> r != null && !r.isCustomEmoji() && r.toString().equals(name)).findFirst()
				.orElse(null);
	}

	@Override
	public IReaction getReactionByUnicode(Emoji emoji) {
		return getReactionByUnicode(emoji.getUnicode());
	}

	@Deprecated
	@Override
	public IReaction getReactionByName(String name) {
		return getReactionByUnicode(name);
	}

	@Override
	public void removeAllReactions() {
		PermissionUtils.requirePermissions(getChannel(), client.getOurUser(), Permissions.MANAGE_MESSAGES);

		((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(
				String.format(DiscordEndpoints.REACTIONS, this.getChannel().getStringID(), this.getStringID()));
	}

	@Override
	public void addReaction(IReaction reaction) {
		if (reaction == null)
			throw new NullPointerException("Reaction argument cannot be null.");

		if (!reaction.getMessage().equals(this))
			throw new DiscordException("Reaction argument's message does not match this one.");

		if (reaction.isCustomEmoji())
			addReaction(reaction.getCustomEmoji());
		else
			addReaction(reaction.toString());
	}

	@Override
	public void addReaction(IEmoji emoji) {
		addReaction(emoji.toString());
	}

	@Override
	public void addReaction(String emoji) {
		String toEncode;

		if (DiscordUtils.CUSTOM_EMOJI_PATTERN.matcher(emoji).matches()) {
			// custom emoji
			toEncode = emoji.replace("<", "").replace(">", "");
		} else if (DiscordUtils.EMOJI_ALIAS_PATTERN.matcher(emoji).matches()) {
			// Unicode alias
			String alias = emoji.replace(":", "");
			Emoji e = EmojiManager.getForAlias(alias);

			if (e == null) {
				throw new IllegalArgumentException("Tried to lookup alias " + alias +
						" in emoji-java, got nothing. Please use the Unicode value (ex: ☑).");
			}

			toEncode = e.getUnicode();
		} else if (EmojiManager.isEmoji(emoji)) {
			// as-is
			toEncode = emoji;
		} else {
			throw new IllegalArgumentException("Emoji argument (" + emoji +
					") is malformed. Please use either Unicode (ex: ☑), an IEmoji toString (<:name:id>), or an alias " +
					"(ex: :ballot_box_with_check:).");
		}

		if (this.getReactionByUnicode(emoji) == null)
			PermissionUtils.requirePermissions(getChannel(), client.getOurUser(), Permissions.ADD_REACTIONS);

		try {
			((DiscordClientImpl) client).REQUESTS.PUT.makeRequest(
					String.format(DiscordEndpoints.REACTIONS_USER, getChannel().getStringID(), getStringID(),
							URLEncoder.encode(toEncode, "UTF-8").replace("+", "%20").replace("%3A", ":"), "@me"));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void addReaction(Emoji emoji) {
		addReaction(emoji.getUnicode());
	}

	@Override
	public void removeReaction(IUser user, IReaction reaction) {
		IMessage message = reaction.getMessage();
		if (!this.equals(message))
			throw new DiscordException("Reaction argument's message does not match this one.");

		if (!user.equals(client.getOurUser())) {
			PermissionUtils.requirePermissions(message.getChannel(), client.getOurUser(), Permissions.MANAGE_MESSAGES);
		}

		try {
			((DiscordClientImpl) client).REQUESTS.DELETE.makeRequest(
					String.format(DiscordEndpoints.REACTIONS_USER, message.getChannel().getStringID(), message.getStringID(),
							reaction.isCustomEmoji()
									? (reaction.getCustomEmoji().getName() + ":" + reaction.getCustomEmoji().getStringID())
									: URLEncoder.encode(reaction.toString(), "UTF-8").replace("+", "%20"),
							user.getLongID() == client.getOurUser().getLongID() ? "@me" : user.getStringID()));
		} catch (UnsupportedEncodingException e) {
			Discord4J.LOGGER.error(LogMarkers.HANDLE, "Discord4J Internal Exception", e);
		}
	}

	@Override
	public void removeReaction(IReaction reaction) {
		removeReaction(client.getOurUser(), reaction);
	}

	@Override
	public long getWebhookLongID(){
		return webhookID;
	}

	@Override
	public MessageTokenizer tokenize() {
		return new MessageTokenizer(this);
	}

	@Override
	public boolean isDeleted() {
		return deleted;
	}

	/**
	 * Sets the CACHED deleted value.
	 *
	 * @param deleted The value to assign into the cache.
	 */
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public IDiscordClient getClient() {
		return client;
	}

	@Override
	public IShard getShard() {
		return getChannel().getShard();
	}

	@Override
	public String toString() {
		return content;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object other) {
		return DiscordUtils.equals(this, other);
	}
}
