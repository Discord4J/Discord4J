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

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class MessageUpdateEvent extends MessageEvent {

    private final long messageId;
    private final long channelId;
    private final long guildId;

    @Nullable
    private final Message old;

    private final boolean contentChanged;
    @Nullable
    private final String currentContent;
    private final boolean embedsChanged;
    private final List<Embed> currentEmbeds;

    public MessageUpdateEvent(DiscordClient client, long messageId, long channelId, long guildId,
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

    public Snowflake getMessageId() {
        return Snowflake.of(messageId);
    }

    public Mono<Message> getMessage() {
        return getClient().getMessageById(getChannelId(), getMessageId());
    }

    public Snowflake getChannelId() {
        return Snowflake.of(channelId);
    }

    public Mono<MessageChannel> getChannel() {
        return getClient().getMessageChannelById(getChannelId());
    }

    // FIXME This should be Optional!
    public Snowflake getGuildId() {
        return Snowflake.of(guildId);
    }

    public Mono<Guild> getGuild() {
        return getClient().getGuildById(getGuildId());
    }

    public Optional<Message> getOld() {
        return Optional.ofNullable(old);
    }

    public boolean isContentChanged() {
        return contentChanged;
    }

    public Optional<String> getCurrentContent() {
        return Optional.ofNullable(currentContent);
    }

    public boolean isEmbedsChanged() {
        return embedsChanged;
    }

    public List<Embed> getCurrentEmbeds() {
        return currentEmbeds;
    }
}
