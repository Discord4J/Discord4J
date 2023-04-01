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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Discord4J. If not, see <http://www.gnu.org/licenses/>.
 */
package discord4j.core.object.entity;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.DiscordObject;
import discord4j.core.object.Embed;
import discord4j.core.object.component.MessageComponent;
import discord4j.discordjson.json.ForumThreadMessageParamsData;
import discord4j.rest.util.AllowedMentions;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Entity representing thread message
 */
public final class ForumThreadMessage implements DiscordObject {

    private final GatewayDiscordClient client;

    private final ForumThreadMessageParamsData data;

    public ForumThreadMessage(GatewayDiscordClient client, ForumThreadMessageParamsData data) {
        this.client = client;
        this.data = data;
    }

    /**
     * Gets the optional content for this forum thread message
     *
     * @return The message content, wrapped in an {@link java.util.Optional<String>}
     */
    public Optional<String> getContent() {
        return data.content().toOptional();
    }

    /**
     * Gets the optional embeds list for this forum thread message
     *
     * @return An {@link Optional<List>} embed list
     */
    public Optional<List<Embed>> getEmbeds() {
        return data.embeds().toOptional()
            .map(list -> list.stream()
                .map(embedData -> new Embed(client, embedData))
                .collect(Collectors.toList()));
    }

    /**
     * Gets allowed mentions object for this forum thread message
     *
     * @return The {@link AllowedMentions} object, wrapped in an {@link Optional}
     */
    public Optional<List<AllowedMentions>> getAllowedMentions() {
        return data.allowedMentions().toOptional()
            .map(list -> list.stream()
                .map(AllowedMentions::from)
                .collect(Collectors.toList()));
    }

    /**
     * Gets message components used in this forum thread message
     *
     * @return An {@link Optional} list of message components
     */
    public Optional<List<MessageComponent>> getComponents() {
        return data.components().toOptional()
            .map(list -> list.stream()
                .map(MessageComponent::fromData)
                .collect(Collectors.toList()));
    }

    /**
     * Gets sticker ids used in this forum thread message
     *
     * @return An {@link Optional} list of sticker identifiers in {@link Snowflake} format
     */
    public Optional<List<Snowflake>> getStickerIds() {
        return data.stickerIds().toOptional()
            .map(list -> list.stream()
                .map(Snowflake::of)
                .collect(Collectors.toList()));
    }

    /**
     * Gets flags associated to the thread forum message
     *
     * @return An {@link Optional} {@link EnumSet} representing the message flags
     */
    public Optional<EnumSet<Message.Flag>> getFlags() {
        return data.flags().toOptional()
            .map(Message.Flag::of);
    }

    @Override
    public GatewayDiscordClient getClient() {
        return client;
    }
}
