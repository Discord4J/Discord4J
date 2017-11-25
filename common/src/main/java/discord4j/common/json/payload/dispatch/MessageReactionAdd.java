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
package discord4j.common.json.payload.dispatch;

import com.fasterxml.jackson.annotation.JsonProperty;
import discord4j.common.jackson.UnsignedJson;
import discord4j.common.json.response.EmojiResponse;

public class MessageReactionAdd implements Dispatch {

	@JsonProperty("user_id")
	@UnsignedJson
	private long userId;
	@JsonProperty("channel_id")
	@UnsignedJson
	private long channelId;
	@JsonProperty("message_id")
	@UnsignedJson
	private long messageId;
	private EmojiResponse emoji;

	public long getUserId() {
		return userId;
	}

	public long getChannelId() {
		return channelId;
	}

	public long getMessageId() {
		return messageId;
	}

	public EmojiResponse getEmoji() {
		return emoji;
	}

	@Override
	public String toString() {
		return "MessageReactionAdd[" +
				"userId=" + userId +
				", channelId=" + channelId +
				", messageId=" + messageId +
				", emoji=" + emoji +
				']';
	}
}
