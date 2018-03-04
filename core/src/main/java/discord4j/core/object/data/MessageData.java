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
package discord4j.core.object.data;

import discord4j.common.json.response.EmbedResponse;
import discord4j.common.json.response.ReactionResponse;

import javax.annotation.Nullable;

public class MessageData {

	private final long id;
	private final long channelId;
	private final long author;
	@Nullable
	private final String content;
	private final String timestamp;
	@Nullable
	private final String editedTimestamp;
	private final boolean tts;
	private final boolean mentionEveryone;
	private final long[] mentions;
	private final long[] mentionRoles;
	private final AttachmentData[] attachments;
	private final EmbedResponse[] embeds;
	private final ReactionResponse[] reactions;
	@Nullable
	private final Long nonce;
	private final boolean pinned;
	@Nullable
	private final Long webhookId;
	private final int type;

	public MessageData(long id, long channelId, long author, @Nullable String content, String timestamp,
			@Nullable String editedTimestamp, boolean tts, boolean mentionEveryone, long[] mentions,
			long[] mentionRoles, AttachmentData[] attachments, EmbedResponse[] embeds, ReactionResponse[] reactions,
			@Nullable Long nonce, boolean pinned, @Nullable Long webhookId, int type) {
		this.id = id;
		this.channelId = channelId;
		this.author = author;
		this.content = content;
		this.timestamp = timestamp;
		this.editedTimestamp = editedTimestamp;
		this.tts = tts;
		this.mentionEveryone = mentionEveryone;
		this.mentions = mentions;
		this.mentionRoles = mentionRoles;
		this.attachments = attachments;
		this.embeds = embeds;
		this.reactions = reactions;
		this.nonce = nonce;
		this.pinned = pinned;
		this.webhookId = webhookId;
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public long getChannelId() {
		return channelId;
	}

	public long getAuthor() {
		return author;
	}

	@Nullable
	public String getContent() {
		return content;
	}

	public String getTimestamp() {
		return timestamp;
	}

	@Nullable
	public String getEditedTimestamp() {
		return editedTimestamp;
	}

	public boolean isTts() {
		return tts;
	}

	public boolean isMentionEveryone() {
		return mentionEveryone;
	}

	public long[] getMentions() {
		return mentions;
	}

	public long[] getMentionRoles() {
		return mentionRoles;
	}

	public AttachmentData[] getAttachments() {
		return attachments;
	}

	public EmbedResponse[] getEmbeds() {
		return embeds;
	}

	public ReactionResponse[] getReactions() {
		return reactions;
	}

	@Nullable
	public Long getNonce() {
		return nonce;
	}

	public boolean isPinned() {
		return pinned;
	}

	@Nullable
	public Long getWebhookId() {
		return webhookId;
	}

	public int getType() {
		return type;
	}
}
