package sx.blah.discord.handle.impl.obj;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import sx.blah.discord.Discord4J;
import sx.blah.discord.api.internal.DiscordEndpoints;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.api.internal.DiscordUtils;
import sx.blah.discord.handle.impl.events.MessageUpdateEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.json.requests.MessageRequest;
import sx.blah.discord.json.responses.MessageResponse;
import sx.blah.discord.util.HTTP429Exception;
import sx.blah.discord.api.internal.Requests;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class Message implements IMessage {

	/**
	 * The ID of the message. Used for message updating.
	 */
	protected final String id;

	/**
	 * The actual message (what you see
	 * on your screen, the content).
	 */
	protected String content;

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
	protected LocalDateTime timestamp;

	/**
	 * The time (if it exists) that the message was edited.
	 */
	protected Optional<LocalDateTime> editedTimestamp;

	/**
	 * The list of users mentioned by this message.
	 */
	protected List<String> mentions;

	/**
	 * The attachments, if any, on the message.
	 */
	protected List<Attachment> attachments;

	/**
	 * Whether the
	 */
	protected boolean mentionsEveryone;

	/**
	 * The client that created this object.
	 */
	protected final IDiscordClient client;

	public Message(IDiscordClient client, String id, String content, IUser user, IChannel channel,
				   LocalDateTime timestamp, Optional<LocalDateTime> editedTimestamp, boolean mentionsEveryone,
				   List<String> mentions, List<Attachment> attachments) {
		this.client = client;
		this.id = id;
		this.content = content;
		this.author = (User) user;
		this.channel = (Channel) channel;
		this.timestamp = timestamp;
		this.editedTimestamp = editedTimestamp;
		this.mentions = mentions;
		this.attachments = attachments;
		this.mentionsEveryone = mentionsEveryone;
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
	}

	/**
	 * Sets the CACHED mentions in this message.
	 *
	 * @param mentions The new mentions.
	 */
	public void setMentions(List<String> mentions) {
		this.mentions = mentions;
	}

	/**
	 * Sets the CACHED attachments in this message.
	 *
	 * @param attachments The new attachements.
	 */
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
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
	public String getID() {
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
				.map(m -> client.getUserByID(m))
				.collect(Collectors.toList());
	}

	@Override
	public List<Attachment> getAttachments() {
		return attachments;
	}

	@Override
	public void reply(String content) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		getChannel().sendMessage(String.format("%s, %s", this.getAuthor(), content));
	}

	@Override
	public IMessage edit(String content) throws MissingPermissionsException, HTTP429Exception, DiscordException {
		if (!this.getAuthor().equals(client.getOurUser()))
			throw new MissingPermissionsException("Cannot edit other users' messages!");
		if (client.isReady()) {
//			content = DiscordUtils.escapeString(content);

			MessageResponse response = DiscordUtils.GSON.fromJson(Requests.PATCH.makeRequest(DiscordEndpoints.CHANNELS+channel.getID()+"/messages/"+id,
					new StringEntity(DiscordUtils.GSON.toJson(new MessageRequest(content, new String[0], false)), "UTF-8"),
					new BasicNameValuePair("authorization", client.getToken()),
					new BasicNameValuePair("content-type", "application/json")), MessageResponse.class);

			IMessage oldMessage = new Message(client, this.id, this.content, author, channel, timestamp, editedTimestamp, mentionsEveryone, mentions, attachments);
			DiscordUtils.getMessageFromJSON(client, channel, response);
			//Event dispatched here because otherwise there'll be an NPE as for some reason when the bot edits a message,
			// the event chain goes like this:
			//Original message edited to null, then the null message edited to the new content
			client.getDispatcher().dispatch(new MessageUpdateEvent(oldMessage, this));

		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
		}
		return this;
	}

	/**
	 * Gets the raw list of mentioned user ids.
	 *
	 * @return Mentioned user list.
	 */
	public List<String> getRawMentions() {
		return mentions;
	}

	@Override
	public boolean mentionsEveryone() {
		return mentionsEveryone;
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
	public void delete() throws MissingPermissionsException, HTTP429Exception, DiscordException {
		if (!getAuthor().equals(client.getOurUser()))
			DiscordUtils.checkPermissions(client, getChannel(), EnumSet.of(Permissions.MANAGE_MESSAGES));

		if (client.isReady()) {
			Requests.DELETE.makeRequest(DiscordEndpoints.CHANNELS+channel.getID()+"/messages/"+id,
					new BasicNameValuePair("authorization", client.getToken()));
		} else {
			Discord4J.LOGGER.error("Bot has not signed in yet!");
		}
	}

	@Override
	public Optional<LocalDateTime> getEditedTimestamp() {
		return editedTimestamp;
	}

	/**
	 * This sets the CACHED edited timestamp.
	 *
	 * @param editedTimestamp The new timestamp.
	 */
	public void setEditedTimestamp(Optional<LocalDateTime> editedTimestamp) {
		this.editedTimestamp = editedTimestamp;
	}

	@Override
	public LocalDateTime getCreationDate() {
		return DiscordUtils.getSnowflakeTimeFromID(id);
	}

	@Override
	public IGuild getGuild() {
		return getChannel().getGuild();
	}

	@Override
	public IDiscordClient getClient() {
		return client;
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
		if (other == null)
			return false;

		return this.getClass().isAssignableFrom(other.getClass()) && ((IMessage) other).getID().equals(getID());
	}
}
