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
package discord4j.core.event.domain.message;

import discord4j.core.DiscordClient;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Dispatched when a message is updated.
 * <p>
 * This event includes both normal message editing as well as the following behavior regarding embeds:
 * When a message with a link is sent, it does not initially contain its embed. When Discord creates the embed, this
 * event is fired with it added to the embeds list.
 * <p>
 * This event is dispatched by Discord.
 *
 * @see <a href="https://discordapp.com/developers/docs/topics/gateway#message-update">Message Update</a>
 */
public class MessageUpdateEvent extends MessageEvent {

    private final long messageId;
    private final long channelId;
    @Nullable
    private final Long guildId;

    @Nullable
    private final Message old;

    private final boolean contentChanged;
    @Nullable
    private final String currentContent;
    private final boolean embedsChanged;
    private final List<Embed> currentEmbeds;

    public MessageUpdateEvent(DiscordClient client, long messageId, long channelId, @Nullable Long guildId,
                              @Nullable Message old, boolean contentChanged, @Nullable String currentContent,
                              boolean embedsChanged, List<Embed> currentEmbeds) {
        super(client);
        this.messageId = messageId;
        this.channelId = channelId;
        this.guildId = guildId;
        this.old = old;
        this.contentChanged = contentChanged;
        this.currentContent = currentContent;
        this.embedsChanged = embedsChanged;
        this.currentEmbeds = currentEmbeds;
    }

    /**
     * Gets the Snowflake ID of the message that has been updated in this event.
     * @return THe ID of the message.
     */
    public Snowflake getMessageId() {
        return Snowflake.of(messageId);
    }

    /**
     * Gets the Message that has been updated in this event.
     * @return The message that has been updated.
     */
    public Mono<Message> getMessage() {
        return getClient().getMessageById(getChannelId(), getMessageId());
    }

    /**
     * Gets the Snowflake ID of the channel containing the updated Message.
     * @return The ID of the channel containing the updated Message.
     */
    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    /**
     * Gets the Channel containing the updated Message in this event.
     * @return The Channel containing the updated Message.
     */
    public Mono<MessageChannel> getChannel() {
        return getClient().getChannelById(getChannelId()).cast(MessageChannel.class);
    }

    /**
     * The Snowflake ID of the Guild containing the updated Message in this event.
     * @return The ID of the guild containing the updated Message.
     */
    public Optional<Snowflake> getGuildId() {
        return Optional.ofNullable(guildId).map(Snowflake::of);
    }

    /**
     * Gets the Guild containing the updated Message in this event.
     * @return The Guild containing the updated Message.
     */
    public Mono<Guild> getGuild() {
        return Mono.justOrEmpty(getGuildId()).flatMap(getClient()::getGuildById);
    }

    /**
     * Gets the old version of the updated Message. This may not be available if messages are not stored.
     * @return The old version of the updated Message.
     */
    public Optional<Message> getOld() {
        return Optional.ofNullable(old);
    }

    /**
     * gets whether or not the content of the message has been changed in this event.
     * @return Whether or not the content of the message has been changed.
     */
    public boolean isContentChanged() {
        return contentChanged;
    }

    /**
     * Gets the current, new, version of the message's content in this event.
     * @return The current version of the Message's content.
     */
    public Optional<String> getCurrentContent() {
        return Optional.ofNullable(currentContent);
    }

    /**
     * Gets whether or not the embed in the message has been changed in this event.
     * @return Whether or not the embed in the message has been changed.
     */
    public boolean isEmbedsChanged() {
        return embedsChanged;
    }

    /**
     * Gets the current, new, version of the message's embed in this event.
     * @return The current version of the Message's embed.
     */
    public List<Embed> getCurrentEmbeds() {
        return currentEmbeds;
    }

    @Override
    public String toString() {
        return "MessageUpdateEvent{" +
                "messageId=" + messageId +
                ", channelId=" + channelId +
                ", guildId=" + guildId +
                ", old=" + old +
                ", contentChanged=" + contentChanged +
                ", currentContent='" + currentContent + '\'' +
                ", embedsChanged=" + embedsChanged +
                ", currentEmbeds=" + currentEmbeds +
                '}';
    }
}
