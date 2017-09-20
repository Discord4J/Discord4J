/*
 * This file is part of Discord4J.
 *
 * Discord4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Discord4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J.  If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.common.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.DiscordEntity;
import discord4j.common.jackson.Possible;
import discord4j.common.jackson.PossibleOptional;

import java.util.Optional;

/**
 * Represents a Message Object as defined by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/resources/channel#message-object">Message Object</a>
 */
@DiscordEntity
public class MessageEntity {

	private String id;
	@JsonProperty("channel_id")
	private String channelId;
	private UserEntity author;
	private String content;
	private String timestamp;
	@JsonProperty("edited_timestamp")
	private Optional<String> editedTimestamp;
	private boolean tts;
	@JsonProperty("mention_everyone")
	private boolean mentionEveryone;
	private UserEntity[] mentions;
	@JsonProperty("mention_roles")
	private String[] mentionRoles;
	private AttachmentEntity[] attachments;
	private EmbedEntity[] embeds;
	private Possible<ReactionEntity[]> reactions = Possible.absent();
	private PossibleOptional<String> nonce = PossibleOptional.absent();
	private boolean pinned;
	@JsonProperty("webhook_id")
	private Possible<String> webhookId = Possible.absent();
	private int type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public UserEntity getAuthor() {
		return author;
	}

	public void setAuthor(UserEntity author) {
		this.author = author;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

	public Optional<String> getEditedTimestamp() {
		return editedTimestamp;
	}

	public void setEditedTimestamp(Optional<String> editedTimestamp) {
		this.editedTimestamp = editedTimestamp;
	}

	public boolean isTts() {
		return tts;
	}

	public void setTts(boolean tts) {
		this.tts = tts;
	}

	public boolean isMentionEveryone() {
		return mentionEveryone;
	}

	public void setMentionEveryone(boolean mentionEveryone) {
		this.mentionEveryone = mentionEveryone;
	}

	public UserEntity[] getMentions() {
		return mentions;
	}

	public void setMentions(UserEntity[] mentions) {
		this.mentions = mentions;
	}

	public String[] getMentionRoles() {
		return mentionRoles;
	}

	public void setMentionRoles(String[] mentionRoles) {
		this.mentionRoles = mentionRoles;
	}

	public AttachmentEntity[] getAttachments() {
		return attachments;
	}

	public void setAttachments(AttachmentEntity[] attachments) {
		this.attachments = attachments;
	}

	public EmbedEntity[] getEmbeds() {
		return embeds;
	}

	public void setEmbeds(EmbedEntity[] embeds) {
		this.embeds = embeds;
	}

	public Possible<ReactionEntity[]> getReactions() {
		return reactions;
	}

	public void setReactions(Possible<ReactionEntity[]> reactions) {
		this.reactions = reactions;
	}

	public PossibleOptional<String> getNonce() {
		return nonce;
	}

	public void setNonce(PossibleOptional<String> nonce) {
		this.nonce = nonce;
	}

	public boolean isPinned() {
		return pinned;
	}

	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}

	public Possible<String> getWebhookId() {
		return webhookId;
	}

	public void setWebhookId(Possible<String> webhookId) {
		this.webhookId = webhookId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
